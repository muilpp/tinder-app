package com.tinderapp.view;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.Match;
import com.tinderapp.model.api_data.MatchDTO;
import com.tinderapp.model.api_data.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchesFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private final static String TAG = MatchesFragment.class.getName();
    private String mUserToken;
    private Snackbar mSnackbarMatchError;
    private DotLoader mDotLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_matches, container, false);
        mUserToken = this.getArguments().getString(BuildConfig.USER_TOKEN);
        initViews(rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserMatches(mUserToken);
    }

    private void initViews(View rootView) {
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.matches_recycler_view);

        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);
        showLoader();

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserMatches(mUserToken);
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        mSnackbarMatchError = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.error_get_matches), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLoader();
                        getUserMatches(mUserToken);
                    }
                });
    }

    private void getUserMatches(final String userToken) {
        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(userToken);

        MatchesDTO matchesDTO = new MatchesDTO();
        matchesDTO.setMatches("");

        Call<ResponseBody> call = tinderAPI.getMatches(matchesDTO);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseStr = response.body().string();
                        MatchDTO matches = new Gson().fromJson(responseStr, MatchDTO.class);

                        Collections.sort(matches.getMatchList());
                        MatchesRecyclerAdapter adapter = new MatchesRecyclerAdapter(matches.getMatchList());
                        mRecyclerView.setAdapter(adapter);
                        mSwipeLayout.setRefreshing(false);
                    } else {
                        mSnackbarMatchError.show();
                        Log.i(TAG, response.errorBody().string());
                        mSwipeLayout.setRefreshing(false);
                    }
                    hideLoader();
                } catch (IOException e) {
                    mSnackbarMatchError.show();
                    Log.e(TAG, e.getMessage(), e);
                    mSwipeLayout.setRefreshing(false);
                    hideLoader();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mSnackbarMatchError.show();
                mSwipeLayout.setRefreshing(false);
                Log.e(TAG, t.getMessage(), t);
                hideLoader();
            }
        });
    }

    public class MatchesRecyclerAdapter extends RecyclerView.Adapter<MatchesRecyclerAdapter.MatchViewHolder> {
        List<Match> matchList;

        MatchesRecyclerAdapter(List<Match> matchList) {
            this.matchList = matchList;
        }

        public class MatchViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView matchName;
            TextView lastMessage;
            ImageView matchPhoto;

            MatchViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.cv);
                matchName = (TextView) itemView.findViewById(R.id.person_name);
                lastMessage = (TextView) itemView.findViewById(R.id.last_message);
                matchPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            }
        }

        @Override
        public MatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_card_view, parent, false);
            return new MatchViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final MatchViewHolder holder, final int position) {
            holder.matchName.setText(matchList.get(position).getPerson().getName());
            final ArrayList<Message> messageList = new ArrayList<>(matchList.get(position).getMessageList());

            if (messageList.size() > 0) {
                holder.lastMessage.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.lastMessage.getLayoutParams();
                DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

                //The match sent the last message, show the left bubble
                if (matchList.get(position).getPerson().getId().equals(messageList.get(messageList.size()-1).getFrom())) {
                    holder.lastMessage.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bubble_left));
                    params.addRule(RelativeLayout.END_OF, R.id.person_photo);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
                } else {
                    holder.lastMessage.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bubble_right));
                    params.removeRule(RelativeLayout.END_OF);
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                    holder.lastMessage.setMaxWidth(displayMetrics.widthPixels / 2);
                }
                holder.lastMessage.setText(messageList.get(messageList.size()-1).getMessage());
            } else {
                holder.lastMessage.setVisibility(View.GONE);
                holder.lastMessage.setText("");
            }

            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chatActivityIntent = new Intent(getActivity(), ChatActivity.class);
                    chatActivityIntent.putParcelableArrayListExtra(BuildConfig.MESSAGE_LIST, messageList);
                    chatActivityIntent.putExtra(BuildConfig.USER_ID, matchList.get(position).getPerson().getId());
                    chatActivityIntent.putExtra(BuildConfig.MATCH_ID, matchList.get(position).getId());
                    chatActivityIntent.putExtra(BuildConfig.USER_IMAGE, matchList.get(position).getPerson().getPhotoList().get(0).getProcessedFiles().get(0).getUrl());
                    chatActivityIntent.putExtra(BuildConfig.USER_TOKEN, mUserToken);
                    startActivity(chatActivityIntent);
                }
            });
            Picasso.with(getActivity())
                    .load(matchList.get(position).getPerson().getPhotoList().get(0).getProcessedFiles().get(0).getUrl())
                    .into(holder.matchPhoto);
        }

        @Override
        public int getItemCount() {
            return matchList.size();
        }
    }

    public static class MatchesDTO {
        private String matches;

        public String getMatches() {
            return matches;
        }

        public void setMatches(String matches) {
            this.matches = matches;
        }
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