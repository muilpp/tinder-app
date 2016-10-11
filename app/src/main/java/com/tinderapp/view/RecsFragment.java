package com.tinderapp.view;

import android.app.Fragment;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.RecommendationsDTO;
import com.tinderapp.presenter.RecsAdapter;

import java.io.IOException;

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
                        showLoader();
                        getUserRecommendations();
                    }
                });

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
                            Toast.makeText(getActivity(), "Something weird happened", Toast.LENGTH_LONG).show();
                            ((HomeActivity)getActivity()).removeFragments();
                        } else {
                            RecommendationsDTO recs = new Gson().fromJson(responseStr, RecommendationsDTO.class);

                            if (recs != null && recs.getResult() != null & recs.getResult().size() > 0) {
                                RecsAdapter recsAdapter = new RecsAdapter(recs.getResult(), getActivity(), mUserToken);
                                mRecyclerView.setAdapter(recsAdapter);
                            } else Toast.makeText(getActivity(), "No recommendations this time", Toast.LENGTH_LONG).show();
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

    private void showLoader() {
        mDotLoader.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoader() {
        mDotLoader.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}