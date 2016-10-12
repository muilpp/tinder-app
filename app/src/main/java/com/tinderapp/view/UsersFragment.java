package com.tinderapp.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.FacebookTokenDTO;
import com.tinderapp.model.api_data.TinderUser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragment extends Fragment {
    private TinderUser mTinderUser;
    private final static String TAG = UsersFragment.class.getName();
    private NavigationView mNavigationView;
    private DotLoader mDotLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_users, container, false);

        TextView alternativeTv = (TextView) rootView.findViewById(R.id.alternative_fragment_tv);
        TextView defaultTv = (TextView) rootView.findViewById(R.id.default_fragment_tv);
        mNavigationView = (NavigationView)getActivity().findViewById(R.id.navigation_view);

        if (BuildConfig.ALTERNATIVE_TOKEN.length() > 0) {
            alternativeTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLoader();
                    getUserToken(BuildConfig.ALTERNATIVE_TOKEN);
                }
            });
        } else alternativeTv.setVisibility(View.GONE);

        defaultTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoader();
                getUserToken(BuildConfig.DEFAULT_TOKEN);
            }
        });

        RelativeLayout parentActivityLayout = (RelativeLayout)getActivity().findViewById(R.id.main_content);
        mDotLoader = (DotLoader) parentActivityLayout.findViewById(R.id.dot_loader);

        return rootView;
    }

    private void getUserToken(String token) {
        TinderAPI tinderAPI = TinderRetrofit.getRawInstance();

        FacebookTokenDTO facebookToken = new FacebookTokenDTO();
        facebookToken.setFacebookToken(token);
        Call<ResponseBody> call = tinderAPI.login(facebookToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        mTinderUser = new Gson().fromJson(response.body().string(), TinderUser.class);

                        View header = mNavigationView.getHeaderView(0);
                        ImageView imageView = (ImageView) header.findViewById(R.id.iv_user_image);
                        Picasso.with(getActivity())
                                .load(mTinderUser.getUser().getPhotoList().get(0).getProcessedFiles().get(0).getUrl())
                                .into(imageView);

                        TextView userNameTv = (TextView)header.findViewById(R.id.tv_user_name);
                        userNameTv.setText(mTinderUser.getUser().getName());

                        //Update the token in the home activity to make sure the proper user is used
                        ((HomeActivity)getActivity()).getTinderUser().setToken(mTinderUser.getToken());
                        ((HomeActivity)getActivity()).getTinderUser().setUser(mTinderUser.getUser());
                        ((HomeActivity)getActivity()).removeFragments();
                    } else {
                        Log.i(TAG, response.errorBody().string());
                    }

                    hideLoader();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    hideLoader();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
                hideLoader();
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