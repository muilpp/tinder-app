package com.tinderapp.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import com.tinderapp.model.BaseApplication;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.apidata.FacebookTokenDTO;
import com.tinderapp.model.apidata.LocationDTO;
import com.tinderapp.model.apidata.TinderUser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private TinderUser mTinderUser;
    private NavigationView mNavigationView;
    private static final String TAG = HomeActivity.class.getName();
    private DotLoader mDotLoader;
//    private final FragmentManager mFragmentManager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initNavigationDrawer();
    }

    public TinderUser getTinderUser() {
        return mTinderUser;
    }

    public void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void addFragment(Fragment fragment, final int tagResource, Map<String, String> argsMap, boolean addToBackStack) {
        final Bundle args = new Bundle();

        for (Map.Entry<String,String> entry : argsMap.entrySet()) {
            args.putString(entry.getKey(), entry.getValue());
        }
        fragment.setArguments(args);

        mDrawerLayout.closeDrawers();

        if (addToBackStack) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, fragment, getString(tagResource))
                    .addToBackStack(null)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_content, fragment, getString(tagResource))
                    .commit();
        }
    }

    public void initNavigationDrawer() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.main_content);
        mDotLoader = (DotLoader) relativeLayout.findViewById(R.id.dot_loader);
        mNavigationView = (NavigationView)findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        showLoader();

        if (mTinderUser == null)
            getUserToken(BuildConfig.DEFAULT_TOKEN);
        else getUserToken(mTinderUser.getToken());

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                Map<String, String> argsMap = new HashMap<>();

                switch (id) {
                    case R.id.add_new_user:
                        addFragment(new UsersFragment(), R.string.menu_new_user, Collections.<String, String>emptyMap(), true);
                        break;

                    case R.id.recommendations:
                        argsMap.put(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        addFragment(new RecsFragment(), R.string.menu_new_user, argsMap, true);
                        break;

                    case R.id.matches:
                        argsMap.put(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        addFragment(new MatchesFragment(), R.string.menu_matches, argsMap, true);
                        break;

                    case R.id.possible_matches:
                        argsMap.put(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        addFragment(new PossibleMatchesFragment(), R.string.menu_possible_matches, argsMap, true);
                        break;

                    case R.id.change_location:
                        mDrawerLayout.closeDrawers();

                        Intent mapsIntent = new Intent(HomeActivity.this, ChangeLocationActivity.class);
                        mapsIntent.putExtra(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        startActivityForResult(mapsIntent, BuildConfig.REQUEST_CODE_CHANGE_LOCATION);
                        break;

                    case R.id.blocks:
                        argsMap.put(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                        argsMap.put(BuildConfig.USER_ID, mTinderUser.getUser().getId());
                        addFragment(new BlocksFragment(), R.string.menu_blocks, argsMap, true);
                        break;

                    case R.id.exit:
                        finish();
                        break;

                    default:
                        break;
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,R.string.drawer_open,R.string.drawer_close){};
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BuildConfig.REQUEST_CODE_CHANGE_LOCATION) {

            if (data != null) {
                double latitude = data.getDoubleExtra(BuildConfig.LAT, 0);
                double longitude = data.getDoubleExtra(BuildConfig.LON, 0);

                LocationDTO location = new LocationDTO(latitude, longitude);
                TinderAPI tinderAPI = new TinderRetrofit().getTokenInstanceWithContentType();

                Call<ResponseBody> call = tinderAPI.changeLocation(location);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseStr = response.body().string();

                                if (responseStr.contains("error"))
                                    Toast.makeText(HomeActivity.this, R.string.wait_location_change, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(HomeActivity.this, R.string.location_changed, Toast.LENGTH_LONG).show();
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
        TinderAPI tinderAPI = new TinderRetrofit().getRawInstance();

        FacebookTokenDTO facebookToken = new FacebookTokenDTO();
        facebookToken.setFacebookToken(token);
        Call<ResponseBody> call = tinderAPI.login(facebookToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        mTinderUser = new Gson().fromJson(response.body().string(), TinderUser.class);
                        BaseApplication.setUserToken(mTinderUser.getToken());

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

                        if (getIntent().getBooleanExtra(BuildConfig.SHOW_MATCHES, false)) {
                            //this happens after deleting a match, where we want to show the updated match list
                            final FragmentManager fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction().replace(R.id.main_content, new MatchesFragment()).commit();
                        } else {
                            //add recommendations the first time
                            Map<String, String> argsMap = new HashMap<>();
                            argsMap.put(BuildConfig.USER_TOKEN, mTinderUser.getToken());
                            addFragment(new RecsFragment(), R.string.menu_new_user, argsMap, false);
                        }
                    } else {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            showLoginErrorSnackbar(getString(R.string.error_token_login));
                            Log.i(TAG, response.errorBody().string());
                        } else {
                            showLoginErrorSnackbar(getString(R.string.error_login));
                            Log.i(TAG, response.errorBody().string());
                        }
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
                            showLoader();
                            getUserToken(BuildConfig.DEFAULT_TOKEN);
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

    private void unlockDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}