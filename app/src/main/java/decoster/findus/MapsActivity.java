package decoster.findus;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
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
import android.widget.Switch;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Constants
    public final static String TAG = "MAIN";
    public final static int SOCKET_PORT = 8888;
    private final static int UPDATE_TIME = 1000;

    //Google map api
    private GoogleMap mMap;
    private Marker myMarker = null;

    //Intend broadcasting
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private P2PHandler p2PHandler;
    private IntentFilter mIntentFilter;

    //GPS log manager
    private DataLocationManager myLocationDataManager;

    //device's user datas
    private String deviceID;
    private String userID;
    private String stateComment;

    //Dataset
    private ConcurrentHashMap<String, Marker> peersMarkers = new ConcurrentHashMap<>();
    private List<Info> peersInfo = Collections.synchronizedList(new ArrayList<Info>());

    //UI elements
    private Switch switchAB;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initalize background processes
        init();

        //UI elements initialization
        FloatingActionButton repositionCamera = (FloatingActionButton) this.findViewById(R.id.centerCamera);
        repositionCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myMarker != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myMarker.getPosition()));
                }
            }
        });

        mapFragment.setHasOptionsMenu(true);

        peersInfo.add(new Info(1, "fd", new LatLng(0, 0), "sdgsd"));
        peersInfo.add(new Info(10, "fd1", new LatLng(50, 20), "sdgsd20"));
        peersInfo.add(new Info(1, "fd3", new LatLng(40, 60), "sdgsd453"));
        CreateEditDialog(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.listsUsers);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(peersInfo, mMap);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //mRecyclerView.addItemDecoration(new RecyclerViewDecorator(this));
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
        startBroadcast();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBroadcast();

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
                CreateEditDialog(false);
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
    }

    //END IMPLEMENTED METHODS


    /**
     * Initialize the marker onto the map fragment given the
     * initial database state
     * Use for debugging
     */
    private void initMarkers() {

    }

    /**
     * Initalize all background processes:
     * -WifiDirectBroadcast receiver
     * -P2Phandler
     * -GPS data manager
     */
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


        myLocationDataManager = new DataLocationManager(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        myLocationDataManager.init();

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        myLocationDataManager.start();

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
     *
     * @param isInitialization If true, the user cannot leave the userId field blank
     */
    private void CreateEditDialog(final boolean isInitialization) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle(R.string.customDial_title);
        //alertDialog.setMessage(R.string.customDial_msg);
        final LayoutInflater inflater = getLayoutInflater();
        View input = inflater.inflate(R.layout.custom_layout, null);
        alertDialog.setView(input); // uncomment this line

        final EditText tx1 = (EditText) input.findViewById(R.id.editUserId);
        final EditText tx2 = (EditText) input.findViewById(R.id.editComment);
        String hint1 = isInitialization ? MapsActivity.this.getResources().getString(R.string.userId_defaultHint) : userID;
        String hint2 = isInitialization ? MapsActivity.this.getResources().getString(R.string.comment_defaultHint) : stateComment;
        tx1.setHint(hint1);
        tx2.setHint(hint2);

        alertDialog.setPositiveButton(MapsActivity.this.getResources().getString(R.string.yes), (dialog, which) -> {
            String tmp2 = tx2.getText().toString();
            String finalComment = tmp2.equals("") ? stateComment : tmp2;

            String tmp1 = tx1.getText().toString();
            String finalUserID = tmp1.equals("") ? userID : tmp1;

            //if tmp1 not null, change the field
            //Could have use dataBinding
            if (tmp1 != null) {
                userID = finalUserID;
                stateComment = finalComment;
                if (isInitialization) {

                }
                dialog.dismiss();
            }

        });
        if (!isInitialization) {
            alertDialog.setNegativeButton(MapsActivity.this.getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        }

        alertDialog.show();
    }

    /**
     * Intermediary method that execute an analyzing process for each object within the passed
     * JSONarray
     *
     * @param arrays The data to be analyzed
     */
    private void handleJSONArrayUpdate(JSONArray arrays) {
        for (int i = 0; i < arrays.length(); i++) {
            try {
                addOrSetPeersMarker(arrays.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * Concatenate the current user state, given by its Id, last submitted position and timestamp
     * with the maintained list of heard User infos, as JSONArray
     *
     * @return the formed JSONArray to be broadcast through wifi
     */
    public JSONArray getInfoToSend() {
        JSONArray res = new JSONArray();
        Location lastLoc = myLocationDataManager.getMyLastLocation();
        JSONObject myJson = new Info(lastLoc.getTime(), deviceID, new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()), userID).toJson();
        res.put(myJson);
        for (Info info : peersInfo) {
            res.put(info.toJson());
        }
        return res;
    }


    /**
     * Create a new user or modify an existing one given a JSONObject submitted by another peer
     *
     * @param data The JsonObject to be analyzed
     */
    public void addOrSetPeersMarker(JSONObject data) {


        try {
            long timestamp = data.getLong("timestamp");
            String id = data.getString("id");
            String userId = data.getString("userId");
            LatLng pos = new LatLng(data.getDouble("lat"), data.getDouble("lon"));
            String dateStr = Utilities.getDateToString(timestamp);
            Info user = Utilities.getUser(id, peersInfo);

            //User exists, modify it
            if (user != null) {
                Marker m = peersMarkers.get(id);
                if (user.getTimestamp() < timestamp) {
                    //update info
                    user.setPosition(pos);
                    user.setTimestamp(timestamp);
                    //update marker
                    m.setPosition(pos);
                    m.setSnippet("last update " + dateStr);
                }

            }
            //User does not exist: create one
            else {
                Info info = new Info(timestamp, id, pos, userId);
                peersInfo.add(info);

                MarkerOptions a = new MarkerOptions().position(pos).title(userId).snippet("last update " + dateStr);
                Marker tmp = mMap.addMarker(a);
                peersMarkers.put(id, tmp);
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
    public void setPersoMarker(LatLng pos) {
        if (myMarker == null) {
            MarkerOptions a = new MarkerOptions().position(pos);
            myMarker = mMap.addMarker(a);
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myMarker.getPosition(), 17.0f));
        } else {
            myMarker.setPosition(pos);

        }


    }

}
