package hackathon.dc15.ibeacontracker;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import de.roderick.weberknecht.WebSocket;
import de.roderick.weberknecht.WebSocketEventHandler;
import de.roderick.weberknecht.WebSocketMessage;

public class WebSocketSender {

    private WebSocket websocket;

    private Handler backgroundHandler;

    public WebSocketSender(String endpoint) {
        Thread thread = new Thread();
        backgroundHandler = new Handler();

        try {
            URI url = new URI(endpoint);
            websocket = new WebSocket(url);

            websocket.setEventHandler(new WebSocketEventHandler() {
                public void onOpen() {
                    Log.i("MainActivity", "websocket is open");
                }

                public void onMessage(WebSocketMessage message) {
                    Log.i("MainActivity", "websocket received message: " + message.toString());
                }

                @Override
                public void onError(IOException exception) {
                    Log.e("MainActivity", "websocket error happened", exception);
                }

                public void onClose() {
                    Log.i("MainActivity", "websocket has been closed");
                }

                public void onPing() {
                }

                public void onPong() {
                }
            });
        } catch (URISyntaxException e) {
            Log.e("MainActivity", "failed settig up websocket", e);
        }
    }

    public void connect() {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                websocket.connect();
            }
        });
    }

    public void disconnect() {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (websocket != null) {
                    websocket.close();
                }
            }
        });
    }

    public void send(final JSONObject data) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    websocket.send(data.toString());
                } catch (Exception e) {
                    Log.e("WebSocketSender", "failed sending websocket message");
                }
            }
        });
    }
}
