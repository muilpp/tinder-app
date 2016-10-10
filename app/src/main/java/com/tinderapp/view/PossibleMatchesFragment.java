package com.tinderapp.view;

import android.app.Fragment;
import android.os.Bundle;
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
import com.tinderapp.model.api_data.Result;
import com.tinderapp.presenter.RecsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PossibleMatchesFragment extends Fragment {
    private static final String TAG = PossibleMatchesFragment.class.getName();
    private List<Result> resultList = new ArrayList<>();
    private String mUserToken;
    private Snackbar mSnackbarRecsError;
    private DotLoader mDotLoader;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_possible_matches, container, false);
        initViews(rootView);
        mUserToken = this.getArguments().getString(BuildConfig.USER_TOKEN);
        getPossibleMatches();

        return rootView;
    }

    private void initViews(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.possible_matches_recycler_view);
        GridLayoutManager mLayoutManager = new GridLayoutManager(rootView.getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPossibleMatches();
            }
        });

        mSnackbarRecsError = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.error_get_possible_matches), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLoader();
                        getPossibleMatches();
                    }
                });

        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);
        showLoader();
    }

    private void getPossibleMatches() {
        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);
        Call<ResponseBody> call = tinderAPI.getRecommendations();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseStr = response.body().string();
                    if (responseStr.contains("recs timeout") || responseStr.contains("recs exhausted") || responseStr.contains("error")) {
                        Toast.makeText(getActivity(), "Something weird happened", Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    } else {
                        RecommendationsDTO recs = new Gson().fromJson(responseStr, RecommendationsDTO.class);

                        //first time
                        if (recs.getResult().size() > 0 && resultList.isEmpty()) {
                            resultList = recs.getResult();
                            getPossibleMatches();
                        } else {
                            // second time, check whether there are repeated elements
                            resultList.retainAll(recs.getResult());

                            if(resultList.isEmpty()) {
                                Log.i(TAG, "No possible matches this time, try again later");
                                Toast.makeText(getActivity(), "No possible matches this time, try again later", Toast.LENGTH_LONG).show();
                            } else {
                                RecsAdapter recsAdapter = new RecsAdapter(resultList, getActivity(), mUserToken);
                                mRecyclerView.setAdapter(recsAdapter);
                            }
                        }
                    }
                    hideLoader();
                    mSwipeLayout.setRefreshing(false);
                } catch (IOException e) {
                    hideLoader();
                    Log.e(TAG, e.getMessage(), e);
                    mSnackbarRecsError.show();
                    mSwipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideLoader();
                Log.e(TAG, t.getMessage(), t);
                mSnackbarRecsError.show();
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    private void showLoader() {
        mDotLoader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mDotLoader.setVisibility(View.GONE);
    }
}