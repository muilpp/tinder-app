package com.tinderapp.view;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.RecommendationsDTO;
import com.tinderapp.model.api_data.Result;
import com.tinderapp.presenter.RecsAdapter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private FloatingActionButton mFab;
    private int numLikes = 0, loopCounter = 0;

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
                        showLoader();
                        getUserRecommendations();
                    }
                });

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        getUserRecommendations();
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
                            Toast.makeText(getActivity(), R.string.error_sth_weird, Toast.LENGTH_LONG).show();
                            ((HomeActivity)getActivity()).removeFragments();
                        } else {
                            final RecommendationsDTO recs = new Gson().fromJson(responseStr, RecommendationsDTO.class);

                            if (recs != null && recs.getResult() != null & recs.getResult().size() > 0) {
                                RecsAdapter recsAdapter = new RecsAdapter(recs.getResult(), getActivity(), mUserToken);
                                mRecyclerView.setAdapter(recsAdapter);

                                mFab.setVisibility(View.VISIBLE);
                                mFab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(R.string.like_everyone_dialog_title)
                                                .setMessage(R.string.like_everyone_dialog_message)
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        sendLikeToEveryone(recs.getResult());
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                });
                            } else Toast.makeText(getActivity(), R.string.no_recs_this_time, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mSnackbarRecsError.show();
                        Log.i(TAG, response.errorBody().string());
                    }
                    mSwipeLayout.setRefreshing(false);
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

    private void sendLikeToEveryone(List<Result> recsList) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(recsList.size() / 3);

        final TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);
        for (final Result result : recsList) {

            Runnable likeEveryoneRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Call<ResponseBody> call = tinderAPI.sendLike(result.getId());

                        //execute them in the same thread because we're already out of the main one
                        Response<ResponseBody>  response = call.execute();

                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().string().toLowerCase().contains("_id"))
                                numLikes++;
                        } else {
                            Log.i(TAG, response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            };

            loopCounter++;
            //Scheduled 200ms after the last one bc sending all of them at the same time does not work (just got the response of half of them), not sure if it's Retrofit or Tinder API's fault
            final int loopTime = 200;
            scheduledExecutorService.schedule(likeEveryoneRunnable, loopCounter * loopTime, TimeUnit.MILLISECONDS);
        }

        try {
            scheduledExecutorService.shutdown();
            scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        ((HomeActivity) getActivity()).removeFragments();
        hideLoader();
        Toast.makeText(getActivity(), Integer.toString(numLikes) + getString(R.string.new_matches), Toast.LENGTH_LONG).show();
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