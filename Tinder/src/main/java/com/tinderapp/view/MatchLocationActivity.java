package com.tinderapp.view;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;

public class MatchLocationActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MatchLocationActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double lat = getIntent().getDoubleExtra(BuildConfig.MATCH_LATITUDE, Double.NaN);
        double lon = getIntent().getDoubleExtra(BuildConfig.MATCH_LONGITUDE, Double.NaN);
        String name = getIntent().getStringExtra(BuildConfig.USER_NAME);

        if (Double.isNaN(lat) || Double.isNaN(lon))
            Toast.makeText(this, "No location found this time, try again", Toast.LENGTH_LONG).show();
        else
            updateMapLocation(googleMap, lat, lon, name);
    }

    private void updateMapLocation(GoogleMap gMap, double latitude, double longitude, String title) {
        Log.i(TAG, "Poso location -> " + latitude + ", " + longitude);
        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(location).title(title);
        gMap.addMarker(markerOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));
    }
}
