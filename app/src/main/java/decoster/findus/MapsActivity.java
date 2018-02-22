package decoster.findus;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;



import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private P2PHandler p2PHandler;
    private IntentFilter mIntentFilter;
    private DataLocationManager myLocationDataManager;
    private boolean onPause= true;
    private final int UPDATE_TIME= 1000;
    private String deviceID;
    private String userID;
    private HashMap<String, Marker> peersMarkers = new HashMap<>();
    private HashMap<String, Info> peersInfo = new HashMap<>();
    private Marker myMarker = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        p2PHandler = new P2PHandler(this, mManager, mChannel);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, p2PHandler);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        permission();


        myLocationDataManager = new DataLocationManager(this, (LocationManager)getSystemService(Context.LOCATION_SERVICE));
        myLocationDataManager.init();

       deviceID= Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        FloatingActionButton pauseFab = (FloatingActionButton) findViewById(R.id.fab);
        pauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starging
                if(onPause) {
                    onPause= false;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                    alertDialog.setTitle(R.string.confirmation_start);
                    alertDialog.setPositiveButton(MapsActivity.this.getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    myLocationDataManager.start();
                                    startBroadcast();
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.setNegativeButton(MapsActivity.this.getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    myLocationDataManager.stop();
                                    stopBroadcast();
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                //Stopping
                else {
                    onPause= true;
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                    alertDialog.setTitle(R.string.confirmation_pause);
                    alertDialog.setPositiveButton(MapsActivity.this.getResources().getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.setNegativeButton(MapsActivity.this.getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }

            }
        });
    }
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        startBroadcast();
    }

    private void stopBroadcast() {
        unregisterReceiver(mReceiver);
    }
    private void startBroadcast() {
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, p2PHandler);

        registerReceiver(mReceiver, mIntentFilter);
    }

    private void permission() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Nammu.askForPermission(this, ACCESS_FINE_LOCATION, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                }

                @Override
                public void permissionRefused() {
                    finish();
                }
            });
        }
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        stopBroadcast();
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }
    public JSONArray getInfoToSend() {
       JSONArray res = new JSONArray();
       Location lastLoc = myLocationDataManager.getMyLastLocation();
       JSONObject myJson = new Info(lastLoc.getTime() ,deviceID,new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()),userID ).toJson();
       res.put(myJson);
       for(Info info : peersInfo.values()) {
           res.put(info.toJson());
       }
       return res;
    }
    public void setPersoMarker(LatLng pos) {
        if(myMarker== null) {
            MarkerOptions a = new MarkerOptions()
                    .position(pos);
            myMarker = mMap.addMarker(a);
        }
        else {
            myMarker.setPosition(pos);

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    public void AddOrSetPeersMarker(JSONObject data) {


        try {
            long timestamp = data.getLong("timestamp");
            String id = data.getString("id");
            String userId = data.getString("userId");
            LatLng pos =new LatLng(data.getDouble("lat"), data.getDouble("lon"));
            String dateStr = Utilities.getDateToString(timestamp);

            if(peersInfo.containsKey(id)) {
                Info info = peersInfo.get(id);
                Marker m = peersMarkers.get(id);
                if(info.getTimestamp() < timestamp) {
                    //update info
                    info.setPosition(pos);
                    info.setTimestamp(timestamp);
                    //update marker
                    m.setPosition(pos);
                    m.setSnippet("last update " + dateStr);
                }

            }else{
                Info info = new Info(timestamp, id,  pos,  userId);
                peersInfo.put(id, info);

                MarkerOptions a = new MarkerOptions()
                        .position(pos)
                        .title(userId)
                        .snippet("last update " + dateStr);
                Marker tmp = mMap.addMarker(a);
                peersMarkers.put(id, tmp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

}