package com.tinderapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.tinderapp.model.BaseApplication;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.apidata.Photo;
import com.tinderapp.model.apidata.Result;
import com.tinderapp.model.apidata.SuperlikeDTO;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = UserProfileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseApplication.getEventBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseApplication.getEventBus().unregister(this);
    }

    public void initViews() {
        ImageView userImageView = (ImageView) findViewById(R.id.toolbar_image);

        final Result userProfile = getIntent().getParcelableExtra(BuildConfig.USER_PROFILE);
        final ArrayList<String> imageList = new ArrayList<>();
        for (Photo photo : userProfile.getPhotos()) {
            imageList.add(photo.getProcessedFiles().get(0).getUrl());
        }

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

        nameTv.setText(nameTv.getText() + " " + userProfile.getName());
        DateTime birthDateTime = new DateTime(userProfile.getBirthDate());
        final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy MMMM dd");
        birthDateTv.setText(birthDateTv.getText() + " " + birthDateTime.toString(fmt));
        distanceTv.setText(distanceTv.getText() + " " + userProfile.getDistance());

        final RelativeLayout igramLayout = (RelativeLayout) findViewById(R.id.igram_layout);
        if (userProfile.getInstagram() != null) {
            igramLayout.setVisibility(View.VISIBLE);
            igramTv.setText(userProfile.getInstagram().getUsername());

            igramLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Uri igramURI = Uri.parse(BuildConfig.IGRAM_BASE_URL+userProfile.getInstagram().getUsername());
                    Intent instagramIntent = new Intent(Intent.ACTION_VIEW, igramURI);
                    instagramIntent.setPackage(BuildConfig.IGRAM_PACKAGE);

                    if (isIntentAvailable(UserProfileActivity.this, instagramIntent)){
                        startActivity(instagramIntent);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, igramURI));
                    }
                }
            });
        } else igramLayout.setVisibility(View.GONE);

        DateTime lastConTime = new DateTime(userProfile.getLastConnection());
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM dd HH:mm");
        lastConnectionTv.setText(lastConnectionTv.getText() + " " +lastConTime.toString(formatter));
        bioTv.setText(bioTv.getText() + " " + userProfile.getBio());

        final boolean hideLikeIcons = getIntent().getBooleanExtra(BuildConfig.HIDE_LIKE_ICONS, false);
        final boolean isBlockedUser = getIntent().getBooleanExtra(BuildConfig.IS_BLOCKED_USER, false);

        final ImageView likeView = (ImageView) findViewById(R.id.like_image);
        final ImageView passView = (ImageView) findViewById(R.id.pass_image);
        final ImageView superLikeView = (ImageView) findViewById(R.id.superlike_image);
        final ImageView shareImageView = (ImageView) findViewById(R.id.share_image);

        if (hideLikeIcons) {
            likeView.setVisibility(View.GONE);
            superLikeView.setVisibility(View.GONE);
            shareImageView.setVisibility(View.GONE);

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
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        TinderAPI tinderAPI = new TinderRetrofit().getTokenInstance();
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
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {/*default behaviour, close dialog*/}
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
                    TinderAPI tinderAPI = new TinderRetrofit().getTokenInstance();
                    Call<ResponseBody> call = tinderAPI.sendLike(userProfile.getId());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful() && response.body() != null) {
                                    if (response.body().string().toLowerCase().contains("_id")) {
                                        Toast.makeText(getApplicationContext(), R.string.new_match, Toast.LENGTH_LONG).show();
                                    } else Toast.makeText(getApplicationContext(), R.string.not_a_match, Toast.LENGTH_LONG).show();
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
                    TinderAPI tinderAPI = new TinderRetrofit().getTokenInstance();
                    Call<ResponseBody> call = tinderAPI.sendPass(userProfile.getId());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Intent intent = new Intent();
                            intent.putExtra(BuildConfig.USER_ID, userProfile.getId());
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
                    TinderAPI tinderAPI = new TinderRetrofit().getTokenInstance();
                    Call<ResponseBody> call = tinderAPI.sendSuperLike(userProfile.getId());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            try {
                                //Sometimes the superlike request returns null for no reason
                                if (response.body() == null) {
                                    Toast.makeText(UserProfileActivity.this, R.string.error_superlike_not_performed, Toast.LENGTH_SHORT).show();
                                } else {
                                    String responseStr = response.body().string();

                                    SuperlikeDTO superlikeDTO = new Gson().fromJson(responseStr, SuperlikeDTO.class);

                                    if (responseStr.toLowerCase().contains("limit_exceeded")) {
                                        Toast.makeText(UserProfileActivity.this, R.string.superlike_exceeded, Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (superlikeDTO.getMatch())
                                            Toast.makeText(UserProfileActivity.this, R.string.new_match, Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(UserProfileActivity.this, R.string.not_a_match, Toast.LENGTH_SHORT).show();
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

            shareImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check this chick out");
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, userProfile.getId());
                    startActivity(Intent.createChooser(shareIntent,"Share via"));
                }
            });
        }
    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }
}