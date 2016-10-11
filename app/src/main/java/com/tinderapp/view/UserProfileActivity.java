package com.tinderapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.LikeDTO;
import com.tinderapp.model.api_data.SuperlikeDTO;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {
    private final static String TAG = UserProfileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
    }

    public void initViews() {
        ImageView userImageView = (ImageView) findViewById(R.id.toolbar_image);

        final ArrayList<String> imageList = getIntent().getStringArrayListExtra(BuildConfig.IMAGES_ARRAY);

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, ImageGalleryActivity.class);
                intent.putStringArrayListExtra(BuildConfig.IMAGES_ARRAY, imageList);
                startActivity(intent);
            }
        });

        Picasso.with(this)
                .load(imageList.get(0))
                .into(userImageView);

        TextView nameTv = (TextView) findViewById(R.id.name);
        TextView birthDateTv = (TextView) findViewById(R.id.birth_date);
        TextView distanceTv = (TextView) findViewById(R.id.distance);
        TextView igramTv = (TextView) findViewById(R.id.igram_profile_text);
        TextView jobTv = (TextView) findViewById(R.id.jobs);
        jobTv.setVisibility(View.GONE);
        TextView schoolTv = (TextView) findViewById(R.id.school);
        schoolTv.setVisibility(View.GONE);
        TextView lastConnectionTv = (TextView) findViewById(R.id.last_connection);
        TextView bioTv = (TextView) findViewById(R.id.bio);

        nameTv.setText(nameTv.getText() + " " + getIntent().getStringExtra(BuildConfig.USER_NAME));
        DateTime birthDateTime = new DateTime(getIntent().getStringExtra(BuildConfig.BIRTH_DATE));
        birthDateTv.setText(birthDateTv.getText() + " " + birthDateTime.getYear()+"-"+birthDateTime.getMonthOfYear()+"-"+birthDateTime.getDayOfMonth());
        distanceTv.setText(distanceTv.getText() + " " + getIntent().getStringExtra(BuildConfig.DISTANCE));

        final RelativeLayout igramLayout = (RelativeLayout) findViewById(R.id.igram_layout);
        if (getIntent().hasExtra(BuildConfig.IGRAM)) {
            igramLayout.setVisibility(View.VISIBLE);
            igramTv.setText(getIntent().getStringExtra(BuildConfig.IGRAM));

            igramLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Uri igramURI = Uri.parse(BuildConfig.IGRAM_BASE_URL+getIntent().getStringExtra(BuildConfig.IGRAM));
                    Intent insta = new Intent(Intent.ACTION_VIEW, igramURI);
                    insta.setPackage(BuildConfig.IGRAM_PACKAGE);

                    if (isIntentAvailable(UserProfileActivity.this, insta)){
                        startActivity(insta);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, igramURI));
                    }
                }
            });
        } else igramLayout.setVisibility(View.GONE);

        String lastConnection = getIntent().getStringExtra(BuildConfig.LAST_CONNECTION);
        DateTime lastConTime = new DateTime(lastConnection);
        String lastTime = lastConTime.getYear() +"-"+ lastConTime.getMonthOfYear() +"-"+ lastConTime.getDayOfMonth() +" "+ lastConTime.getHourOfDay() +":"+ lastConTime.getMinuteOfHour();
        lastConnectionTv.setText(lastConnectionTv.getText() + " " + lastTime);
        bioTv.setText(bioTv.getText() + " " + getIntent().getStringExtra(BuildConfig.BIO));

        final boolean hideLikeIcons = getIntent().getBooleanExtra(BuildConfig.HIDE_LIKE_ICONS, false);
        final boolean isBlockedUser = getIntent().getBooleanExtra(BuildConfig.IS_BLOCKED_USER, false);

        final ImageView likeView = (ImageView) findViewById(R.id.like_image);
        final ImageView passView = (ImageView) findViewById(R.id.pass_image);
        final ImageView superLikeView = (ImageView) findViewById(R.id.superlike_image);

        if (hideLikeIcons) {
            likeView.setVisibility(View.GONE);
            superLikeView.setVisibility(View.GONE);

            if (isBlockedUser)
                passView.setVisibility(View.GONE);
            else {
                passView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(UserProfileActivity.this)
                                .setTitle(R.string.delete_match_dialog_title)
                                .setMessage(R.string.delete_match_dialog_message)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(getIntent().getStringExtra(BuildConfig.USER_TOKEN));
                                        Call<ResponseBody> call = tinderAPI.deleteMatch(getIntent().getStringExtra(BuildConfig.MATCH_ID));

                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                Toast.makeText(getApplicationContext(), R.string.delete_match_ok, Toast.LENGTH_LONG).show();
                                                Intent getMatchesIntent = new Intent(UserProfileActivity.this, HomeActivity.class);
                                                getMatchesIntent.putExtra(BuildConfig.SHOW_MATCHES, true);
                                                finish();
                                                startActivity(getMatchesIntent);
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Toast.makeText(getApplicationContext(), R.string.delete_match_error, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        } else {
            likeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(getIntent().getStringExtra(BuildConfig.USER_TOKEN));
                    Call<ResponseBody> call = tinderAPI.sendLike(getIntent().getStringExtra(BuildConfig.USER_ID));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    String responseStr = response.body().string();
                                    boolean isMatch = false;

                                    if (responseStr.toLowerCase().contains("_id")) {
                                        LikeDTO likeDTO = new Gson().fromJson(responseStr, LikeDTO.class);
                                        Log.i(TAG, "Superlike ? " + likeDTO.getMatch().isSuperLike());
                                        isMatch = true;
                                    }

                                    Toast.makeText(getApplicationContext(), "Is match: " + isMatch, Toast.LENGTH_LONG).show();
                                } else {
                                    Log.i(TAG, response.errorBody().string());
                                }
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, t.getMessage(), t);
                        }
                    });
                }
            });

            passView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(getIntent().getStringExtra(BuildConfig.USER_TOKEN));
                    Call<ResponseBody> call = tinderAPI.sendPass(getIntent().getStringExtra(BuildConfig.USER_ID));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Intent intent = new Intent();
                            intent.putExtra(BuildConfig.USER_ID, getIntent().getStringExtra(BuildConfig.USER_ID));
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, t.getMessage(), t);
                        }
                    });
                }
            });

            superLikeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(getIntent().getStringExtra(BuildConfig.USER_TOKEN));
                    Call<ResponseBody> call = tinderAPI.sendSuperLike(getIntent().getStringExtra(BuildConfig.USER_ID));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            try {
                                Log.i(TAG, "Response -> " + response.body());

                                //Sometimes the superlike request returns null for no reason
                                if (response.body() == null) {
                                    Toast.makeText(UserProfileActivity.this, "Could not perform a superlike this time, try again later", Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "Could not perform a superlike this time, try again later");
                                } else {
                                    String responseStr = response.body().string();

                                    SuperlikeDTO superlikeDTO = new Gson().fromJson(responseStr, SuperlikeDTO.class);

                                    if (responseStr.toLowerCase().contains("limit_exceeded")) {
                                        Toast.makeText(UserProfileActivity.this, "Superlike limit exceeded", Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, "Superlike limit exceeded");
                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Super match ? " + superlikeDTO.getMatch(), Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, "Super match ? " + superlikeDTO.getMatch());
                                        finish();
                                    }
                                }
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
    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}