package decoster.findus.backgroundP2p;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import decoster.findus.activity.MapsActivity;

/**
 * Created by kevin on 22.02.18.
 */

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private MapsActivity handler;
    private SessionManager session;
    private InetAddress mAddress;

    public ClientSocketHandler(MapsActivity handler, InetAddress groupOwnerAddress) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), handler.SOCKET_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            session = new SessionManager(socket, handler, false);
            new Thread(session).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public SessionManager getSession() {
        return session;
    }

}
/*
public class Client {

    public void send(MapsActivity activity, InetAddress host, int port, JSONArray data) {
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];
        try {
            */
/**
 * Create a client socket with the host,
 * port, and timeout information.
 *//*

            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data.toString().getBytes());
            outputStream.flush();

            InputStream inputstream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputstream));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");

            JSONArray res = null;
            try {
                res = new JSONArray(json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(res !=null) {
                activity.handleJSONArrayUpdate(data);
            }

            outputStream.close();
            inputstream.close();
            reader.close();
        }
        catch (IOException e) {
            //catch logic
        }
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
        }
    }
}
*/
