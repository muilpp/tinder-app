package com.tinderapp.presenter;

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
import com.tinderapp.model.api_data.Photo;
import com.tinderapp.model.api_data.UserProfileDTO;
import com.tinderapp.view.HomeActivity;
import com.tinderapp.view.UserProfileActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlocksRecyclerAdapter extends RecyclerView.Adapter<BlocksRecyclerAdapter.BlocksViewHolder> {
    private List<String> blockList;
    private String mUserToken;
    private Activity mActivity;
    private TinderAPI tinderAPI;
    private final static String TAG = BlocksRecyclerAdapter.class.getName();

    public BlocksRecyclerAdapter(List<String> blockList, Activity parentActivity, String userToken) {
        this.blockList = blockList;
        mUserToken = userToken;
        mActivity = parentActivity;
        tinderAPI = TinderRetrofit.getTokenInstanceWithContentType(mUserToken);
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
                Call<ResponseBody> call = tinderAPI.getUserProfile(blockList.get(position));

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseStr = response.body().string();
                                if (responseStr.contains("recs timeout") || responseStr.contains("recs exhausted") || responseStr.contains("error")) {
                                    Toast.makeText(mActivity, "Something weird happened", Toast.LENGTH_LONG).show();
                                    ((HomeActivity)mActivity).removeFragments();
                                } else {
                                    UserProfileDTO userProfile = new Gson().fromJson(responseStr, UserProfileDTO.class);

                                    Intent intent = new Intent(mActivity, UserProfileActivity.class);
                                    intent.putExtra(BuildConfig.USER_NAME, userProfile.getResult().getName());
                                    intent.putExtra(BuildConfig.BIO, userProfile.getResult().getBio());
                                    intent.putExtra(BuildConfig.BIRTH_DATE, userProfile.getResult().getBirth_date());
                                    intent.putExtra(BuildConfig.DISTANCE, userProfile.getResult().getDistance());
                                    intent.putExtra(BuildConfig.USER_ID, userProfile.getResult().getId());
                                    intent.putExtra(BuildConfig.USER_TOKEN, mUserToken);
                                    intent.putExtra(BuildConfig.IS_BLOCKED_USER, true);
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
                                    intent.putExtra(BuildConfig.HIDE_LIKE_ICONS, true);
                                    intent.putExtra(BuildConfig.IS_BLOCKED_USER, true);

                                    mActivity.startActivity(intent);
                                }
                            } else {
                                Log.i(TAG, response.errorBody().string());
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

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
