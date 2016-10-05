package com.tinderapp.view;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.FacebookTokenDTO;
import com.tinderapp.model.api_data.LocationDTO;
import com.tinderapp.model.api_data.TinderUser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private TinderUser mTinderUser;
    private NavigationView mNavigationView;
    private final static String TAG = HomeActivity.class.getName();
    private DotLoader mDotLoader;
    private RelativeLayout menuLayout;
//    private TextView mContentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initViews();
        initNavigationDrawer();
    }

    public void initViews() {
        final FragmentManager fragmentManager = getFragmentManager();
        final Bundle args = new Bundle();

        RelativeLayout changeUserLayout = (RelativeLayout)findViewById(R.id.add_person_layout);
        changeUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                UsersFragment usersFragment = new UsersFragment();
                fragmentManager.beginTransaction().replace(R.id.main_content, usersFragment).commit();
            }
        });

        RelativeLayout recsLayout = (RelativeLayout)findViewById(R.id.recs_layout);
        recsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                args.putString(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                RecsFragment recsFragment = new RecsFragment();
                recsFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.main_content, recsFragment).commit();
            }
        });

        RelativeLayout matchesLayout = (RelativeLayout)findViewById(R.id.matches_layout);
        matchesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                args.putString(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                MatchesFragment matchesFragment = new MatchesFragment();
                matchesFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.main_content, matchesFragment).commit();
            }
        });

        RelativeLayout changeLocationLayout = (RelativeLayout)findViewById(R.id.change_location_layout);
        changeLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMenu();
                mDrawerLayout.closeDrawers();
                Intent mapsIntent = new Intent(HomeActivity.this, MapsActivity.class);
                mapsIntent.putExtra(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                startActivityForResult(mapsIntent, BuildConfig.REQUEST_CODE_CHANGE_LOCATION);
//                startActivity(mapsIntent);
            }
        });
    }

    public void initNavigationDrawer() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.main_content);
        mDotLoader = (DotLoader) relativeLayout.findViewById(R.id.dot_loader);
//        mContentTv = (TextView) relativeLayout.findViewById(R.id.content_tv);
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        menuLayout = (RelativeLayout)findViewById(R.id.menu_layout);

        showLoader();
        getUserToken(BuildConfig.MARC_TOKEN);

        final Bundle args = new Bundle();
        final FragmentManager fragmentManager = getFragmentManager();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.add_new_user:
                        UsersFragment usersFragment = new UsersFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, usersFragment).commit();
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.recommendations:
                        args.putString(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        RecsFragment recsFragment = new RecsFragment();
                        recsFragment.setArguments(args);
                        fragmentManager.beginTransaction().replace(R.id.main_content, recsFragment).commit();

                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.matches:
                        args.putString(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        MatchesFragment matchesFragment = new MatchesFragment();
                        matchesFragment.setArguments(args);
                        fragmentManager.beginTransaction().replace(R.id.main_content, matchesFragment).commit();
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.change_location:
                        mDrawerLayout.closeDrawers();

                        Intent mapsIntent = new Intent(HomeActivity.this, MapsActivity.class);
                        mapsIntent.putExtra(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        startActivityForResult(mapsIntent, BuildConfig.REQUEST_CODE_CHANGE_LOCATION);
//                        startActivity(mapsIntent);
                        break;

                    case R.id.exit:
                        finish();

                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BuildConfig.REQUEST_CODE_CHANGE_LOCATION) {
            showMenu();

            if (data != null) {
                double latitude = data.getDoubleExtra(BuildConfig.LAT, 0);
                double longitude = data.getDoubleExtra(BuildConfig.LON, 0);
                Log.i(TAG, "Arriba lat -> " + latitude);
                Log.i(TAG, "Arriba lon -> " + longitude);

                LocationDTO location = new LocationDTO(latitude, longitude);
                TinderAPI tinderAPI = TinderRetrofit.getTokenInstanceWithContentType(mTinderUser.getToken());

                Call<ResponseBody> call = tinderAPI.changeLocation(location);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseStr = response.body().string();

                                Log.i(TAG, "Reponse -> " + responseStr);

                                if (responseStr.contains("error"))
                                    Toast.makeText(HomeActivity.this, "Wait a little longer to perform another location change", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(HomeActivity.this, "Location changed successfully, go look for new matches here!", Toast.LENGTH_LONG).show();
                            } else {
                                showLoginErrorSnackbar(getString(R.string.change_location_error));
                                Log.i(TAG, response.errorBody().string());
                            }

                            hideLoader();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                            showLoginErrorSnackbar(getString(R.string.change_location_error));
                            hideLoader();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, t.getMessage(), t);
                        showLoginErrorSnackbar(getString(R.string.change_location_error));
                        hideLoader();
                    }
                });
            }
        }
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
                        Picasso.with(HomeActivity.this)
                                .load(mTinderUser.getUser().getPhotoList().get(0).getProcessedFiles().get(0).getUrl())
                                .into(imageView);

                        TextView userNameTv = (TextView)header.findViewById(R.id.tv_user_name);
                        userNameTv.setText(mTinderUser.getUser().getName());

                        NavigationView userProfileView = (NavigationView) findViewById(R.id.navigation_view);
                        userProfileView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.main_content)).commit();
                            }
                        });

                        showMenu();

                        if (getIntent().getBooleanExtra(BuildConfig.SHOW_MATCHES, false)) {
                            //this happens after deleting a match, where we want to show the updated match list
                            final FragmentManager fragmentManager = getFragmentManager();
                            final Bundle args = new Bundle();

                            args.putString(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                            MatchesFragment matchesFragment = new MatchesFragment();
                            matchesFragment.setArguments(args);
                            fragmentManager.beginTransaction().replace(R.id.main_content, matchesFragment).commit();

                            hideMenu();
                        }
                    } else {
                        showLoginErrorSnackbar(getString(R.string.error_login));
                        Log.i(TAG, response.errorBody().string());
                    }

                    hideLoader();
                    unlockDrawer();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    showLoginErrorSnackbar(getString(R.string.error_login));
                    hideLoader();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
                showLoginErrorSnackbar(getString(R.string.error_login));
                hideLoader();
            }
        });
    }

    private void showLoginErrorSnackbar(String message) {
        if (mTinderUser == null || mTinderUser.getToken() == null) {
            Snackbar.make(findViewById(R.id.coordinator_layout), message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.try_again), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getUserToken(BuildConfig.MARC_TOKEN);
                            showLoader();
                        }
                    }).show();
        }
    }

    private void showLoader() {
        mDotLoader.setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void hideLoader() {
        mDotLoader.setVisibility(View.GONE);
    }

    private void showMenu() {
        menuLayout.setVisibility(View.VISIBLE);
    }

    private void hideMenu() {
        menuLayout.setVisibility(View.GONE);
    }

    private void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}