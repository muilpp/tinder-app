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
import com.tinderapp.model.api_data.BlocksDTO;
import com.tinderapp.presenter.BlocksRecyclerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlocksFragment extends Fragment {
    private String mUserToken, mUserID;
    private BlocksRecyclerAdapter adapter;
    private String TAG = BlocksFragment.class.getName();
    private RecyclerView mRecyclerView;
    private DotLoader mDotLoader;
    private Snackbar mSnackbarMatchError;
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_blocks, container, false);
        mUserToken = this.getArguments().getString(BuildConfig.USER_TOKEN);
        mUserID = this.getArguments().getString(BuildConfig.USER_ID);
        initViews(rootView);
        getBlocks();

        return rootView;
    }

    private void initViews(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.blocks_recycler_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);
        showLoader();

        mSnackbarMatchError = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), getString(R.string.error_get_blocks), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.try_again), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLoader();
                        getBlocks();
                    }
                });

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBlocks();
            }
        });
    }

    private void getBlocks() {
        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);

        MatchesFragment.MatchesDTO matchesDTO = new MatchesFragment.MatchesDTO();
        matchesDTO.setMatches("");

        Call<ResponseBody> call = tinderAPI.getMatches(matchesDTO);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseStr = response.body().string();
                        BlocksDTO blocksDTO = new Gson().fromJson(responseStr, BlocksDTO.class);

                        List<String> blockList = new ArrayList<>();
                        for (String blockID : blocksDTO.getBlockList()) {
                            blockList.add(blockID.replace(mUserID, ""));
                        }

                        if (adapter == null)
                            adapter = new BlocksRecyclerAdapter(blockList, getActivity(), mUserToken);
                        else {
                            adapter.updateData(blockList);
                            adapter.notifyDataSetChanged();
                        }

                        mRecyclerView.setAdapter(adapter);
                    } else {
                        Log.i(TAG, response.errorBody().string());
                        mSnackbarMatchError.show();
                    }
                    hideLoader();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    hideLoader();
                    mSnackbarMatchError.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
                hideLoader();
                mSnackbarMatchError.show();
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
        mSwipeLayout.setRefreshing(false);
    }
}
