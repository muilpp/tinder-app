package com.tinderapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.MatchDTO;
import com.tinderapp.presenter.MatchesRecyclerAdapter;

import java.io.IOException;
import java.util.Collections;

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
    private LinearLayoutManager mLayoutManager;
    private int mScrollPosition = 0;
    private MatchesRecyclerAdapter adapter;

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

    @Override
    public void onPause() {
        super.onPause();
        mScrollPosition = mLayoutManager.findFirstVisibleItemPosition();
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

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

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
                        MatchDTO matches = new Gson().fromJson(response.body().string(), MatchDTO.class);

                        Collections.sort(matches.getMatchList());

                        if (adapter == null)
                            adapter = new MatchesRecyclerAdapter(matches.getMatchList(), getActivity(), mUserToken);
                        else {
                            adapter.updateData(matches.getMatchList());
                            adapter.notifyDataSetChanged();
                        }

                        mRecyclerView.setAdapter(adapter);
                        mSwipeLayout.setRefreshing(false);

                        if (mScrollPosition > 0 && mScrollPosition < mRecyclerView.getAdapter().getItemCount())
                            mRecyclerView.scrollToPosition(mScrollPosition);
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