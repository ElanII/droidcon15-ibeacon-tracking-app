package hackathon.dc15.ibeacontracker;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketSender {

    private Socket websocket;

    public WebSocketSender(String endpoint) {

        try {
            URI url = new URI(endpoint);

            websocket = IO.socket(url);
            websocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i("MainActivity", "websocket is open");
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i("MainActivity", "websocket has been closed");
                }
            });
        } catch (URISyntaxException e) {
            Log.e("MainActivity", "failed settig up websocket", e);
        }
    }

    public void connect() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                websocket.connect();
//            }
//        }).start();
    }

    public void disconnect() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                if (websocket != null) {
                    websocket.close();
                }
//            }
//        }).start();
    }

    public void send(final JSONObject data) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                try {
                    websocket.emit("new_loc", data);
                } catch (Exception e) {
                    Log.e("WebSocketSender", "failed sending websocket message");
                }
//            }
//        }).start();
    }
}
