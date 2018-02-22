package decoster.findus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by kevin on 22.02.18.
 */

public class DataLocationManager {
    private final MapsActivity mActivity;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private Context context;
    private final float MIN_DISTANCE_UPDATE = 1.0f;
    private final int MIN_TIME_UDPATE = 10;
    private boolean gpsRequiered= false;
    private Location myLastLocation = null;

    public DataLocationManager(MapsActivity mActivity, LocationManager locationManager) {
        this.locationManager = locationManager;
        context =mActivity.getApplicationContext();
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

                    if(myLastLocation != null && myLastLocation.distanceTo(location) <= MIN_DISTANCE_UPDATE) {
                        myLastLocation = location;
                        mActivity.setPersoMarker(new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude()));

                    }
                }
            };
        };
    }

    public Location getMyLastLocation() {
        return myLastLocation;
    }

    @SuppressLint("MissingPermission")
    public void start()  {
        Log.d("MainActivity", "start GPS");
        if(!gpsRequiered){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, null);
            gpsRequiered = true;
        }
    }

    public void stop(){
        Log.d("MainActivity", "pause GPS");
        if(gpsRequiered) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            gpsRequiered = false;
        }
    }

}
