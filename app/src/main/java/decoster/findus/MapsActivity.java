package decoster.findus;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{
    public final static String TAG = "MAIN";
    public static final int SOCKET_PORT = 8888;
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
    private String stateComment;

    private ConcurrentHashMap<String, Marker> peersMarkers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Info> peersInfo = new ConcurrentHashMap<>();
    private Marker myMarker = null;

    private Switch switchAB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        FloatingActionButton repositionCamera = (FloatingActionButton) this.findViewById(R.id.centerCamera);
        repositionCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(myMarker != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myMarker.getPosition()));
                }
            }
        });
        mapFragment.setHasOptionsMenu(true);

        CreateEditDialog(true);
    }

    private void init() {

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

        myLocationDataManager.start();

    }
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        //startBroadcast();
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
        //stopBroadcast();
    }
    @Override
    protected  void onStart() {
        super.onStart();
        startBroadcast();
    }
    @Override
    protected  void onStop() {
        super.onStop();

    }
    @Override
    protected  void onDestroy() {
        super.onDestroy();
        stopBroadcast();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        switchAB = (Switch) menu.findItem(R.id.switchId)
                .getActionView().findViewById(R.id.toggleBtn1);

        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchId:
                return true;
            case R.id.changeState:
                CreateEditDialog(false);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    private void CreateEditDialog(final boolean isInitialization) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle(R.string.customDial_title);
        //alertDialog.setMessage(R.string.customDial_msg);
        final LayoutInflater inflater = getLayoutInflater();
        View input = inflater.inflate(R.layout.custom_layout, null);
        alertDialog.setView(input); // uncomment this line

        final EditText tx1 = (EditText) input.findViewById(R.id.editUserId);
        final EditText tx2 = (EditText) input.findViewById(R.id.editComment);
        String hint1 = isInitialization?MapsActivity.this.getResources().getString(R.string.userId_defaultHint) :userID;
        String hint2 = isInitialization?MapsActivity.this.getResources().getString(R.string.comment_defaultHint) :stateComment;
        tx1.setHint(hint1);
        tx2.setHint(hint2);

        alertDialog.setPositiveButton(MapsActivity.this.getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String tmp2= tx2.getText().toString();
                        String finalComment = tmp2.equals("")?stateComment:tmp2;

                        String tmp1= tx1.getText().toString();
                        String finalUserID = tmp1.equals("")?userID:tmp1;
                        if(tmp1 != null) {
                            userID= finalUserID;
                            stateComment= finalComment;
                            if(isInitialization) {

                            }
                            dialog.dismiss();
                        }

                    }
                });
        if(!isInitialization) {
            alertDialog.setNegativeButton(MapsActivity.this.getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }


        alertDialog.show();
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
    public void handleJSONArrayUpdate(JSONArray arrays) {
        for(int i = 0; i < arrays.length(); i++) {
            try {
                addOrSetPeersMarker(arrays.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void addOrSetPeersMarker(JSONObject data) {


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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }



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

    }

}
