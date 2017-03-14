package com.tinderapp.model.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
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
import com.tinderapp.model.apidata.Result;
import com.tinderapp.model.apidata.UserProfileDTO;
import com.tinderapp.view.UserProfileActivity;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecsRecyclerAdapter extends RecyclerView.Adapter<RecsRecyclerAdapter.RecsViewHolder> {
    private static final String TAG = RecsRecyclerAdapter.class.getName();
    private List<Result> mRecsList;
    private Activity mActivity;
    private TinderAPI tinderAPI;

    public RecsRecyclerAdapter(List<Result> recsList, Activity parentActivity) {
        mRecsList = recsList;
        mActivity = parentActivity;
        tinderAPI = new TinderRetrofit().getTokenInstance();
    }

    public class RecsViewHolder extends RecyclerView.ViewHolder {
        CardView recsCardView;
        TextView userName;
        TextView userBirthDate;
        TextView userDistance;
        ImageView recsPhoto;

        RecsViewHolder(View itemView) {
            super(itemView);
            recsCardView = (CardView) itemView.findViewById(R.id.recs_card_view);
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

        if (!messageList.isEmpty()) {
            holder.userName.setText(messageList.get(position).getName());
            DateTime dateTime = new DateTime(mRecsList.get(position).getBirthDate());
            holder.userBirthDate.setText(Integer.toString(dateTime.getYear()));
            holder.userDistance.setText(mRecsList.get(position).getDistance() + " miles");
        }

        Picasso.with(mActivity)
                .load(mRecsList.get(position).getPhotos().get(0).getProcessedFiles().get(0).getUrl())
                .into(holder.recsPhoto);

        holder.recsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Call<ResponseBody> call = tinderAPI.getUserProfile(mRecsList.get(holder.getAdapterPosition()).getId());
                removeAt(holder.getAdapterPosition());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() || response.body() != null) {
                                UserProfileDTO userProfile = new Gson().fromJson(response.body().string(), UserProfileDTO.class);
                                Intent intent = new Intent(view.getContext(), UserProfileActivity.class);
                                intent.putExtra(BuildConfig.USER_PROFILE, userProfile.getResult());
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
