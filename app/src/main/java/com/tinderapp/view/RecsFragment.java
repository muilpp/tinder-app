package com.tinderapp.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.Photo;
import com.tinderapp.model.api_data.RecommendationsDTO;
import com.tinderapp.model.api_data.Result;
import com.tinderapp.model.api_data.UserProfileDTO;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecsFragment extends Fragment {
    private String mUserToken;
    private final static String TAG = RecsFragment.class.getName();
    private RecyclerView mRecyclerView;
    private TinderAPI tinderAPI;
    private SwipeRefreshLayout mSwipeLayout;
    private Snackbar mSnackbarRecsError;
    private DotLoader mDotLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recs, container, false);
        mUserToken = this.getArguments().getString(BuildConfig.USER_TOKEN);
        initViews(rootView);

        return rootView;
    }

    public void initViews(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv);

        GridLayoutManager mLayoutManager = new GridLayoutManager(rootView.getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserRecommendations();
            }
        });
        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);
        showLoader();

        mSnackbarRecsError = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.error_get_recs), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getUserRecommendations();
                    }
                });

        getUserRecommendations();
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.RecsViewHolder> {
        List<Result> recsList;

        RVAdapter(List<Result> recsList) {
            this.recsList = recsList;
        }

        public class RecsViewHolder extends RecyclerView.ViewHolder {

            TextView userName;
            TextView userBirthDate;
            TextView userDistance;
            ImageView recsPhoto;

            RecsViewHolder(View itemView) {
                super(itemView);
                userName = (TextView) itemView.findViewById(R.id.user_name);
                userBirthDate = (TextView) itemView.findViewById(R.id.user_birth_date);
                userDistance = (TextView) itemView.findViewById(R.id.user_distance);
                recsPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            }
        }

        @Override
        public RecsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recs_card_view, parent, false);
            return new RecsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final RecsViewHolder holder, final int position) {
            final ArrayList<Result> messageList = new ArrayList<>(recsList);

            if (messageList.size() > 0) {
                holder.userName.setText(messageList.get(position).getName());
                DateTime dateTime = new DateTime(recsList.get(position).getBirth_date());
                holder.userBirthDate.setText(Integer.toString(dateTime.getYear()));
                holder.userDistance.setText(recsList.get(position).getDistance() + " miles");
            }

            Picasso.with(getActivity())
                    .load(recsList.get(position).getPhotos().get(0).getProcessedFiles().get(0).getUrl())
                    .into(holder.recsPhoto);

            holder.recsPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Call<ResponseBody> call = tinderAPI.getUserProfile(recsList.get(position).getId());
                    removeAt(position);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful() || response.body() != null) {
                                    UserProfileDTO userProfile = new Gson().fromJson(response.body().string(), UserProfileDTO.class);

                                    Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                                    intent.putExtra(BuildConfig.USER_NAME, userProfile.getResult().getName());
                                    intent.putExtra(BuildConfig.BIO, userProfile.getResult().getBio());
                                    intent.putExtra(BuildConfig.BIRTH_DATE, userProfile.getResult().getBirth_date());
                                    intent.putExtra(BuildConfig.DISTANCE, userProfile.getResult().getDistance());
                                    intent.putExtra(BuildConfig.USER_ID, userProfile.getResult().getId());
                                    intent.putExtra(BuildConfig.USER_TOKEN, mUserToken);
//                                    intent.putExtra(BuildConfig.SCHOOL, userProfile.getResult().get);
                                    if (userProfile.getResult().getInstagram() != null)
                                        intent.putExtra(BuildConfig.IGRAM, userProfile.getResult().getInstagram().getUsername());

                                    intent.putExtra(BuildConfig.LAST_CONNECTION, userProfile.getResult().getLastConnection());
//                                    intent.putExtra(BuildConfig.JOB, "");
//                                    intent.putExtra("url", userProfile.getResult().getPhotos().get(0).getProcessedFiles().get(0).getUrl());
                                    //userProfile.getResult().getPhotos().get(0).getProcessedFiles().get(0).get

                                    ArrayList<String> imageList = new ArrayList<>();
                                    for (Photo photo : userProfile.getResult().getPhotos()) {
                                        imageList.add(photo.getProcessedFiles().get(0).getUrl());
                                    }

                                    intent.putStringArrayListExtra(BuildConfig.IMAGES_ARRAY, imageList);

                                    view.getContext().startActivity(intent);
                                } else Log.i(TAG, response.errorBody().string());
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, t.getMessage(), t);
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return recsList.size();
        }

        public void removeAt(int position) {
            recsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recsList.size());
        }
    }

    private void getUserRecommendations() {
        Call<ResponseBody> call = tinderAPI.getRecommendations();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseStr = response.body().string();
                        if (responseStr.contains("recs timeout") || responseStr.contains("recs exhausted") || responseStr.contains("error")) {
                            Toast.makeText(getActivity(), "Something weird happened", Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        } else {
                            RecommendationsDTO recs = new Gson().fromJson(responseStr, RecommendationsDTO.class);

                            if (recs != null && recs.getResult() != null & recs.getResult().size() > 0) {
                                RVAdapter adapter = new RVAdapter(recs.getResult());
                                mRecyclerView.setAdapter(adapter);
//                                mGridview.setAdapter(gridAdapter);
                            } else Toast.makeText(getActivity(), "No recommendations this time", Toast.LENGTH_LONG).show();
                        }
                        mSwipeLayout.setRefreshing(false);
                    } else {
                        mSnackbarRecsError.show();
                        Log.i(TAG, response.errorBody().string());
                        mSwipeLayout.setRefreshing(false);
                    }
                    hideLoader();
                } catch (IOException e) {
                    hideLoader();
                    mSnackbarRecsError.show();
                    Log.e(TAG, e.getMessage(), e);
                    mSwipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideLoader();
                mSnackbarRecsError.show();
                mSwipeLayout.setRefreshing(false);
                Log.e(TAG, t.getMessage(), t);
            }
        });
    }

    private void showLoader() {
        mDotLoader.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoader() {
        mDotLoader.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}