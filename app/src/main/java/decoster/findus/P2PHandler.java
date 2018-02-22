package decoster.findus;

import android.app.Activity;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 22.02.18.
 */

public class P2PHandler implements  WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener  {
    private MapsActivity mActivity;
    private int numberOfPeers;
    private int port = 8888;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Server mServer;
    private Client mClient;
    public P2PHandler(MapsActivity activity, WifiP2pManager mManager,WifiP2pManager.Channel mChannel) {

        this.mActivity = activity;
        this.mManager = mManager;
        this.mChannel =mChannel;

        mClient = new Client();
        mServer= null;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {


        List<WifiP2pDevice> devices = (new ArrayList<>());
        devices.addAll(wifiP2pDeviceList.getDeviceList());
        numberOfPeers = devices.size();
        for(WifiP2pDevice device: devices) {
            connect(device);
        }
        //do something with the device list
    }

    private void connect(WifiP2pDevice device ) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("MainActivitiy", "conncetion succed");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("MainActivitiy", "conncetion fail");
            }
        });

    }
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner
        // (server).
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
             mServer = (Server) new Server(port, numberOfPeers, mActivity).execute();
        } else if (wifiP2pInfo.groupFormed) {
            mServer.cancel(true);
            mClient.send(groupOwnerAddress ,port,mActivity.getInfoToSend());

            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }

    private void discoverPeers() {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reasonCode) {
                }
            });

        }
    public void onP2PStateReceive(boolean isEnable) {

    }
}
