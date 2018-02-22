package decoster.findus;

import android.content.ContentResolver;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by kevin on 22.02.18.
 */

public class Client {

    public void send(InetAddress host, int port, JSONArray data) {
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data.toString().getBytes());
            outputStream.flush();
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
