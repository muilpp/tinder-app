package com.tinderapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.RecommendationsDTO;
import com.tinderapp.presenter.GridviewAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationsFragment extends Fragment {
    private GridView mGridview;
    private SwipeRefreshLayout mSwipeLayout;
    private final static String TAG = RecommendationsFragment.class.getName();
    private String mUserToken;
    private Snackbar mSnackbarRecsError;
    private DotLoader mDotLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recommendations, container, false);
        mUserToken = this.getArguments().getString(BuildConfig.USER_TOKEN);
        initViews(rootView);

        return rootView;
    }

    private void initViews(View rootView) {
        Log.i(TAG, "Entro a init views");
        mGridview = (GridView) rootView.findViewById(R.id.recsGridView);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);
        showLoader();

        getUserRecommendations(mUserToken);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserRecommendations(mUserToken);
            }
        });

        mSnackbarRecsError = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.error_get_recs), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getUserRecommendations(mUserToken);
                    }
                });
    }

    private void getUserRecommendations(final String userToken) {
        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(userToken);

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
                                GridviewAdapter gridAdapter = new GridviewAdapter(getActivity(), recs.getResult(), userToken);
                                mGridview.setAdapter(gridAdapter);
                                mSwipeLayout.setRefreshing(false);
                            } else Toast.makeText(getActivity(), "No recommendations this time", Toast.LENGTH_LONG).show();
                        }
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
        mGridview.setVisibility(View.GONE);
    }

    private void hideLoader() {
        mDotLoader.setVisibility(View.GONE);
        mGridview.setVisibility(View.VISIBLE);
    }
}