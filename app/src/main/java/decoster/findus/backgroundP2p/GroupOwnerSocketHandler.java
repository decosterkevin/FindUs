package decoster.findus.backgroundP2p;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import decoster.findus.activity.MapsActivity;

/**
 * Created by kevin on 22.02.18.
 */

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerSocketHandler extends Thread {

    private static final String TAG = "GroupOwnerSocketHandler";
    private final int THREAD_COUNT = 10;
    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    ServerSocket socket = null;
    private MapsActivity handler;

    public GroupOwnerSocketHandler(MapsActivity handler) throws IOException {
        try {
            socket = new ServerSocket(handler.SOCKET_PORT);
            this.handler = handler;
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                pool.execute(new SessionManager(socket.accept(), handler, true));
                Log.d(TAG, "Launching the I/O handler");

            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException ioe) {

                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }

}

//public class Server extends AsyncTask{
//    private int port;
//    private int numberOfPeers;
//    private MapsActivity mapsActivity;
//
//    public Server(int port,int numberOfPeers, MapsActivity mapsActivity) {
//        this.port = port;
//        this.numberOfPeers = numberOfPeers;
//        this.mapsActivity = mapsActivity;
//    }
//
//
//    @Override
//    protected Void doInBackground(Object[] objects) {
//        try {
//
//            /**
//             * Create a server socket and wait for client connections. This
//             * call blocks until a connection is accepted from a client
//             */
//            int index = 0;
//            ServerSocket serverSocket = new ServerSocket(this.port);
//            while(index < this.numberOfPeers) {
//
//                Socket client = serverSocket.accept();
//
//                /**
//                 * If this code is reached, a client has connected and transferred data
//                 * Save the input stream from the client as a JPEG file
//                 */
//
//                InputStream inputstream = client.getInputStream();
//                BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(inputstream));
//
//                StringBuffer json = new StringBuffer(1024);
//                String tmp = "";
//                while ((tmp = reader.readLine()) != null)
//                    json.append(tmp).append("\n");
//
//
//                OutputStream outputStream = client.getOutputStream();
//                outputStream.write(mapsActivity.getInfoToSend().toString().getBytes());
//                outputStream.flush();
//
//
//                JSONArray data = null;
//                try {
//                     data = new JSONArray(json.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                if(data !=null) {
//                    mapsActivity.handleJSONArrayUpdate(data);
//                }
//                outputStream.close();
//                inputstream.close();
//                reader.close();
//            }
//            serverSocket.close();
//
//        } catch (IOException e) {
//            Log.e("mainActivity", e.getMessage());
//            return null;
//        }
//        return null;
//    }
//
//
//}
