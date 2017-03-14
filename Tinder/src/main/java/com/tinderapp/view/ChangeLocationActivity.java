package com.tinderapp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChangeLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = ChangeLocationActivity.class.getName();
    private Marker mMarker;
    private static final double BCN_LAT = 41.3873106;
    private static final double BCN_LON = 2.1598319;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
    public void onMapReady(final GoogleMap googleMap) {
        updateMapLocation(googleMap, BCN_LAT, BCN_LON);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                new AlertDialog.Builder(ChangeLocationActivity.this)
                        .setTitle(R.string.change_location_dialog_title)
                        .setMessage(R.string.change_location_dialog_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Latitude -> " + latLng.latitude);
                                Log.i(TAG, "Longitude -> " + latLng.longitude);
                                mMarker.remove();
                                updateMapLocation(googleMap, latLng.latitude, latLng.longitude);

                                Intent intent = new Intent();
                                setResult(BuildConfig.REQUEST_CODE_CHANGE_LOCATION, intent);
                                intent.putExtra(BuildConfig.LAT, latLng.latitude);
                                intent.putExtra(BuildConfig.LON, latLng.longitude);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private String getLocationAddress(double latitude, double longitude) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = Collections.emptyList();

        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.i(TAG, e.getMessage(), e);
        }

        String address = null;
        if (!addresses.isEmpty()) {
            address = addresses.get(0).getLocality();
        }

        return address;
    }

    private void updateMapLocation(GoogleMap gMap, double latitude, double longitude) {
        String address = getLocationAddress(latitude, longitude);
        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(location).title(address);
        mMarker = gMap.addMarker(markerOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f));
    }
}