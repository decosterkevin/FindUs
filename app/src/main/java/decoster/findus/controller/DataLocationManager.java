package decoster.findus.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import decoster.findus.activity.MapsActivity;

/**
 * Created by kevin on 22.02.18.
 */

public class DataLocationManager {
    private final MapsActivity mActivity;
    private final float MIN_DISTANCE_UPDATE = 1.0f;
    private final int MIN_TIME_UDPATE = 10;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private Context context;
    private boolean gpsRequiered = false;
    private Location myLastLocation = null;

    public DataLocationManager(MapsActivity mActivity, LocationManager locationManager) {
        this.locationManager = locationManager;
        context = mActivity.getApplicationContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mLocationRequest = new LocationRequest();
        this.mActivity = mActivity;

    }

    private void updateLocationRequest() {

        mLocationRequest.setInterval(MIN_TIME_UDPATE);
        mLocationRequest.setFastestInterval(MIN_TIME_UDPATE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_UPDATE);
    }

    public void init() {
        updateLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("MainActivity", "pos");
                        myLastLocation = location;
                        mActivity.setPersoMarker(myLastLocation);

                    }
                }
            }

            ;
        };
    }

    public Location getMyLastLocation() {
        return myLastLocation;
    }

    @SuppressLint("MissingPermission")
    public void start() {

        if (!gpsRequiered) {
            Log.d("MainActivity", "start GPS");
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            gpsRequiered = true;
        }
    }

    public void stop() {
        Log.d("MainActivity", "pause GPS");
        if (gpsRequiered) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            gpsRequiered = false;
        }
    }

}
