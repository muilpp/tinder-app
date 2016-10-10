package com.tinderapp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.Photo;
import com.tinderapp.model.api_data.Result;
import com.tinderapp.model.api_data.UserProfileDTO;
import com.tinderapp.view.UserProfileActivity;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecsAdapter extends RecyclerView.Adapter<RecsAdapter.RecsViewHolder> {
    private final static String TAG = RecsAdapter.class.getName();
    private List<Result> mRecsList;
    private Activity mActivity;
    private String mUserToken;
    private TinderAPI tinderAPI;

    public RecsAdapter(List<Result> recsList, Activity parentActivity, String userToken) {
        mRecsList = recsList;
        mActivity = parentActivity;
        mUserToken = userToken;
        tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);
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
        final ArrayList<Result> messageList = new ArrayList<>(mRecsList);

        if (messageList.size() > 0) {
            holder.userName.setText(messageList.get(position).getName());
            DateTime dateTime = new DateTime(mRecsList.get(position).getBirth_date());
            holder.userBirthDate.setText(Integer.toString(dateTime.getYear()));
            holder.userDistance.setText(mRecsList.get(position).getDistance() + " miles");
        }

        Picasso.with(mActivity)
                .load(mRecsList.get(position).getPhotos().get(0).getProcessedFiles().get(0).getUrl())
                .into(holder.recsPhoto);

        holder.recsPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Call<ResponseBody> call = tinderAPI.getUserProfile(mRecsList.get(position).getId());
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
        return mRecsList.size();
    }

    public void removeAt(int position) {
        mRecsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mRecsList.size());
    }
}
