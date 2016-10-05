package com.tinderapp.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.LikeDTO;
import com.tinderapp.model.api_data.LocationDTO;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String TAG = MapsActivity.class.getName();
    private Location mLocation;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocation = getLocation();
    }

    public Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.i(TAG, "No network provider enabled");
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }

                double latitude = 0, longitude = 0;
                if (isNetworkEnabled) {
                    Log.i(TAG, "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        Log.i(TAG, "GPS Enabled");

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return location;
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
        final GoogleMap gMap = googleMap;

        updateMapLocation(gMap, mLocation.getLatitude(), mLocation.getLongitude());

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(R.string.change_location_dialog_title)
                        .setMessage(R.string.change_location_dialog_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Latitude -> " + latLng.latitude);
                                Log.i(TAG, "Longitude -> " + latLng.longitude);
                                mMarker.remove();
                                updateMapLocation(gMap, latLng.latitude, latLng.longitude);

                                Intent intent = new Intent();
                                setResult(BuildConfig.REQUEST_CODE_CHANGE_LOCATION, intent);
                                intent.putExtra(BuildConfig.LAT, latLng.latitude);
                                intent.putExtra(BuildConfig.LON, latLng.longitude);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
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
        if (addresses.size() > 0) {
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
        gMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
    }
}