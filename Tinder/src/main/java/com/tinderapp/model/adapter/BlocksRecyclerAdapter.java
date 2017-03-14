package com.tinderapp.model.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.apidata.UserProfileDTO;
import com.tinderapp.view.UserProfileActivity;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlocksRecyclerAdapter extends RecyclerView.Adapter<BlocksRecyclerAdapter.BlocksViewHolder> {
    private List<String> blockList;
    private Activity mActivity;
    private TinderAPI tinderAPI;
    private static final String TAG = BlocksRecyclerAdapter.class.getName();

    public BlocksRecyclerAdapter(List<String> blockList, Activity parentActivity) {
        this.blockList = blockList;
        mActivity = parentActivity;
        tinderAPI = new TinderRetrofit().getTokenInstanceWithContentType();
    }

    public class BlocksViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView matchID;

        BlocksViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.blocks_card_view);
            matchID = (TextView) itemView.findViewById(R.id.match_id_tv);
        }
    }

    public void updateData(List<String> matchList) {
        this.blockList = matchList;
    }

    @Override
    public BlocksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.blocks_card_view, parent, false);
        return new BlocksViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BlocksViewHolder holder, final int position) {
        holder.matchID.setText(blockList.get(position));

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<ResponseBody> call = tinderAPI.getUserProfile(blockList.get(holder.getAdapterPosition()));

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseStr = response.body().string();
                                if (responseStr.contains("recs timeout") || responseStr.contains("recs exhausted") || responseStr.contains("error")) {
                                    Toast.makeText(mActivity, R.string.error_sth_weird, Toast.LENGTH_LONG).show();
                                } else {
                                    UserProfileDTO userProfile = new Gson().fromJson(responseStr, UserProfileDTO.class);

                                    Intent intent = new Intent(mActivity, UserProfileActivity.class);
                                    intent.putExtra(BuildConfig.USER_PROFILE, userProfile.getResult());
                                    intent.putExtra(BuildConfig.IS_BLOCKED_USER, true);
                                    intent.putExtra(BuildConfig.HIDE_LIKE_ICONS, true);
                                    intent.putExtra(BuildConfig.IS_BLOCKED_USER, true);

                                    mActivity.startActivity(intent);
                                }
                            } else {
                                Log.i(TAG, response.errorBody().string());
                                Toast.makeText(mActivity, R.string.profile_deleted, Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // unimplemented
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return blockList.size();
    }
}
