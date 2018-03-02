package decoster.findus.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import decoster.findus.Manifest;
import decoster.findus.R;
import decoster.findus.backgroundP2p.P2PHandler;
import decoster.findus.backgroundP2p.WifiDirectBroadcastReceiver;
import decoster.findus.controller.CustomAdapter;
import decoster.findus.controller.DataLocationManager;
import decoster.findus.model.Peer;
import decoster.findus.utility.Utilities;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Constants
    public final static String TAG = "MAIN";
    public final static int SOCKET_PORT = 8888;
    private final static int UPDATE_TIME = 1000;
    private final Random random = new Random();

    //Google map api
    private GoogleMap mMap;

    //P2P communication handler
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private P2PHandler p2PHandler;
    private IntentFilter mIntentFilter;

    //GPS log manager
    private DataLocationManager myLocationDataManager;

    //device's user datas
    private Peer userPeer;
    private ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    //UI elements
    private Switch switchAB;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tmpUserId = sharedPref.getString("userID", null);
        if (tmpUserId == null) {
            finish();
        } else {
            userPeer = new Peer(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), tmpUserId);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        peers.put("fd", new Peer("fd", "sdgsd", new LatLng(0, 0), 0, null, null));
        peers.put("fd1", new Peer("fd1", "sdgsd20", new LatLng(50, 20), 0, null, null));
        peers.put("fd3", new Peer("fd3", "sdgsd453", new LatLng(40, 60), 0, null, null));
        //Initalize background processes

        initBackProcess();


    }

    //START IMPLEMENTED METHODS

    @Override
    protected void onResume() {
        super.onResume();
        //startBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopBroadcast();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //startBroadcast();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopBroadcast();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        switchAB = (Switch) menu.findItem(R.id.switchId).getActionView().findViewById(R.id.toggleBtn1);

        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT).show();
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
            case R.id.searchItem:
                if (mRecyclerView.getVisibility() == View.VISIBLE) {
                    mRecyclerView.setVisibility(View.GONE);

                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
                return true;
            case R.id.changeState:
                createEditDialog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
            //mMap.setMyLocationEnabled(true);
        }

        initUI();
    }

    @Override
    public void onBackPressed() {
    }
    //END IMPLEMENTED METHODS


    /**
     * Initalize all background processes:
     * -WifiDirectBroadcast receiver
     * -P2Phandler
     * -GPS data manager
     */
    private void initBackProcess() {

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


        myLocationDataManager = new DataLocationManager(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        myLocationDataManager.init();


        myLocationDataManager.start();

    }

    private void initUI() {
        //UI elements initialization
        FloatingActionButton repositionCamera = (FloatingActionButton) this.findViewById(R.id.centerCamera);
        repositionCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (userPeer.getMarker() != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(userPeer.getPosition()));
                }
            }
        });

        FloatingActionButton focusMarker = (FloatingActionButton) this.findViewById(R.id.centerMarkers);
        focusMarker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (userPeer != null) {
                    LatLngBounds.Builder builderBound = new LatLngBounds.Builder();
                    for (Peer peer : peers.values()) {
                        builderBound.include(peer.getPosition());
                    }
                    LatLngBounds bound = builderBound.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bound, 20);
                    mMap.animateCamera(cu);
                }
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.listsUsers);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(peers, mMap);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //mRecyclerView.addItemDecoration(new RecyclerViewDecorator(this));
        for (Peer peer : peers.values()) {

            Marker tmp = createMarker(peer.getId(), peer.getUserId(), peer.getTimestamp(), peer.getPosition());
            peer.setMarker(tmp);

        }

        progressBar.setVisibility(View.GONE);

    }

    /**
     * Stop receiving broadcast intent
     */
    private void stopBroadcast() {
        unregisterReceiver(mReceiver);
    }

    /**
     * Start receiving broadcast intent
     */
    private void startBroadcast() {
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, p2PHandler);

        registerReceiver(mReceiver, mIntentFilter);
    }

    /**
     * Handle permission using Nammu librairy
     */
    private void permission() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    /**
     * Create a customize Dialog that ask the user to change its visible user id and its
     * current mood state
     */
    private void createEditDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle(R.string.customDial_title);
        //alertDialog.setMessage(R.string.customDial_msg);
        final LayoutInflater inflater = getLayoutInflater();
        View input = inflater.inflate(R.layout.custom_layout, null);
        alertDialog.setView(input); // uncomment this line

        final EditText txt = (EditText) input.findViewById(R.id.editComment);

        alertDialog.setPositiveButton(MapsActivity.this.getResources().getString(R.string.yes), (dialog, which) -> {
            String tmp = txt.getText().toString();
            String finalComment = tmp.matches("") ? userPeer.getStatus() : tmp;

            userPeer.setStatus(finalComment);
            dialog.dismiss();

        });
        alertDialog.setNegativeButton(MapsActivity.this.getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }


    private Marker createMarker(String id, String title, Long timestamp, LatLng point) {

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(random.nextFloat() * 360.0f);
        Marker marker = mMap.addMarker(new MarkerOptions().position(point).icon(bitmapDescriptor).title(title).snippet(Utilities.getDateToString(timestamp)));
        return marker;
    }

    /**
     * Concatenate the current user state, given by its Id, last submitted position and timestamp
     * with the maintained list of heard User infos, as JSONArray
     *
     * @return the formed JSONArray to be broadcast through wifi
     */
    public JSONArray getInfoToSend() {
        JSONArray res = new JSONArray();
        res.put(userPeer.toJson());
        for (Peer peer : peers.values()) {
            res.put(peer.toJson());
        }
        return res;
    }

    /**
     * Intermediary method that execute an analyzing process for each object within the passed
     * JSONarray
     *
     * @param arrays The data to be analyzed
     */
    public void handleJSONArrayUpdate(JSONArray arrays) {
        for (int i = 0; i < arrays.length(); i++) {
            try {
                addOrSetPeersMarker(arrays.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Create a new user or modify an existing one given a JSONObject submitted by another peer
     *
     * @param data The JsonObject to be analyzed
     */
    private void addOrSetPeersMarker(JSONObject data) {


        try {
            long timestamp = data.getLong("timestamp");
            String id = data.getString("id");
            String userId = data.getString("userId");
            LatLng pos = new LatLng(data.getDouble("lat"), data.getDouble("lon"));

            Peer user = peers.get(id);

            //User exists, modify it
            if (user != null) {
                if (user.getTimestamp() < timestamp) {
                    //update info
                    user.setPosition(pos);
                    user.setTimestamp(timestamp);
                }
            }
            //User does not exist: create one
            else {
                Marker marker = createMarker(id, userId, timestamp, pos);
                Peer peer = new Peer(id, userId, pos, timestamp, null, marker);
                peers.put(id, peer);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * Callback function executed when Gps manager fire a new position event
     * Modify or create the current user state
     *
     * @param pos the position received from the callback
     */
    public void setPersoMarker(Location pos) {

        userPeer.updateLocation(pos);
        LatLng point = new LatLng(pos.getLatitude(), pos.getLongitude());
        if (userPeer.getMarker() == null) {

            MarkerOptions a = new MarkerOptions().position(point);
            userPeer.setMarker(mMap.addMarker(a));
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPeer.getPosition(), 17.0f));
        } else {
            userPeer.setPosition(point);

        }


    }

}
