package com.tinderapp.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.Photo;
import com.tinderapp.model.api_data.Result;
import com.tinderapp.model.api_data.UserProfileDTO;
import com.tinderapp.view.UserProfileActivity;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GridviewAdapter extends ArrayAdapter<Result> {
    private final Context mContext;
    private List<Result> recsList;
    private TinderAPI tinderAPI;
    private final static String TAG = GridviewAdapter.class.getName();
    private String userToken;

    public GridviewAdapter(Context context, List<Result> values, String userToken) {
        super(context, R.layout.users_grid_layout, values);
        this.mContext = context;
        this.recsList = values;
        this.userToken = userToken;
        tinderAPI = TinderRetrofit.getTokenInstance(userToken);
    }

    @Override
    public int getCount() {
        return recsList.size();
    }

    @Override
    public Result getItem(int i) {
        return recsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.users_grid_layout, parent, false);
            holder = new ViewHolder();
            holder.userName = (TextView) convertView.findViewById(R.id.userName);
            holder.userBirthDate = (TextView) convertView.findViewById(R.id.userBirthDate);
            holder.userDistance = (TextView) convertView.findViewById(R.id.userDistance);
            holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
            convertView.setTag(holder);
            holder.userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Call<ResponseBody> call = tinderAPI.getUserProfile(recsList.get(position).getId());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful() || response.body() != null) {
                                    UserProfileDTO userProfile = new Gson().fromJson(response.body().string(), UserProfileDTO.class);

                                    Intent intent = new Intent(getContext(), UserProfileActivity.class);
                                    intent.putExtra(BuildConfig.USER_NAME, userProfile.getResult().getName());
                                    intent.putExtra(BuildConfig.BIO, userProfile.getResult().getBio());
                                    intent.putExtra(BuildConfig.BIRTH_DATE, userProfile.getResult().getBirth_date());
                                    intent.putExtra(BuildConfig.DISTANCE, userProfile.getResult().getDistance());
                                    intent.putExtra(BuildConfig.USER_ID, userProfile.getResult().getId());
                                    intent.putExtra(BuildConfig.USER_TOKEN, userToken);
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

                                    mContext.startActivity(intent);
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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userName.setText(recsList.get(position).getName());
        DateTime dateTime = new DateTime(recsList.get(position).getBirth_date());
//        holder.userBirthDate.setText(dateTime.getYear()+"-"+dateTime.getMonthOfYear()+"-"+dateTime.getDayOfMonth());
        holder.userBirthDate.setText(Integer.toString(dateTime.getYear()));
        holder.userDistance.setText(recsList.get(position).getDistance() + " miles");
        holder.position = position;

        Picasso.with(mContext)
                .load(recsList.get(position).getPhotos().get(0).getProcessedFiles().get(0).getUrl())
                .resize(100, 100)
                .centerCrop()
                .transform(new CircleTransform())
                .into(holder.userImage);

        holder.position = position;
        return convertView;
    }

    public static class ViewHolder {
        TextView userName;
        TextView userBirthDate;
        TextView userDistance;
        ImageView userImage;
        int position;
    }
}
