package decoster.findus;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
/**
 * Created by kevin on 23.02.18.
 */

class SessionManager implements Runnable {
    private Socket socket = null;
    private MapsActivity handler;
    private boolean isGroupOwner;
    private final int SLEEP_TIME = 5000;
    public SessionManager(Socket socket,MapsActivity handler, boolean isGroupOwner) {
        this.socket = socket;
        this.handler = handler;
        this.isGroupOwner = isGroupOwner;
    }

    private BufferedReader reader;
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";

    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            reader = new BufferedReader(
                    new InputStreamReader(iStream));
            while (true) {
                try {
                    // Read from the InputStre am
                    if(isGroupOwner) {
                        //If server, wait for client data
                        JSONArray data = read();
                        handler.handleJSONArrayUpdate(data);
                        //Wait for the other threads to finish and update data
                        //Thread.sleep(sleepTime/2);
                        //send the up-to-date data
                        write(handler.getInfoToSend());
                    }
                    else {
                        //if client, send data to server
                        write(handler.getInfoToSend());

                        //then wait for the answer
                        JSONArray data = read();
                        handler.handleJSONArrayUpdate(data);
                    }
                    Thread.sleep(SLEEP_TIME);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void shutdown() {

        try {
            reader.close();
            iStream.close();
            oStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void write(JSONArray data) {
        try {
            if(data != null) {
                oStream.write(data.toString().getBytes());
            }

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public JSONArray read() {
        JSONArray res = null;
        try {


            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            res = new JSONArray(json.toString());

        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
        catch (JSONException e) {
            Log.e(TAG, "Exception during write", e);
        }
        return res;
    }
}
