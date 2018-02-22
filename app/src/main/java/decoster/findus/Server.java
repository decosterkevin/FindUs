package decoster.findus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kevin on 22.02.18.
 */

public class Server extends AsyncTask{
    private int port;
    private int numberOfPeers;
    private MapsActivity mapsActivity;

    public Server(int port,int numberOfPeers, MapsActivity mapsActivity) {
        this.port = port;
        this.numberOfPeers = numberOfPeers;
        this.mapsActivity = mapsActivity;
    }


    @Override
    protected Void doInBackground(Object[] objects) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            int index = 0;
            ServerSocket serverSocket = new ServerSocket(this.port);
            while(index < this.numberOfPeers) {

                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */

                InputStream inputstream = client.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputstream));

                StringBuffer json = new StringBuffer(1024);
                String tmp = "";
                while ((tmp = reader.readLine()) != null)
                    json.append(tmp).append("\n");
                reader.close();
                JSONObject data = null;
                try {
                     data = new JSONObject(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(data !=null) {
                    mapsActivity.AddOrSetPeersMarker(data);
                }
            }
            serverSocket.close();

        } catch (IOException e) {
            Log.e("mainActivity", e.getMessage());
            return null;
        }
        return null;
    }


}
