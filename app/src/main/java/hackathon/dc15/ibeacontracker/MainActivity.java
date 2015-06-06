package hackathon.dc15.ibeacontracker;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    private WebSocketSender sender;
    private long nextSendTime = 0;

    private String android_id;

    private TextView textLastSend;
    private TextView textStrength;
    private DateFormat formatter;

    public MainActivity() {
        sender = new WebSocketSender("http://100.100.238.92:3000");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        textLastSend = (TextView) findViewById(R.id.textLastSend);
        TextView textDeviceId = (TextView) findViewById(R.id.textDeviceId);
        textStrength = (TextView) findViewById(R.id.textStrength);

        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        textDeviceId.setText("Device id: " + android_id);

        formatter = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        sender.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(true);
        }

        sender.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this))  {
            beaconManager.setBackgroundMode(false);
        }

        sender.connect();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (Beacon beacon : beacons) {
                    // TODO make configurable
                    if (beacon.getId1().toString().contains("f0018b9b-7509-4c31-a905-1a27d39c003c")) {
                        handleBeacon(beacon);
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleBeacon(final Beacon beacon) {
        if (System.currentTimeMillis() < nextSendTime) {
            return;
        }
        nextSendTime = System.currentTimeMillis() + 3000;

        Log.i("MainActivity", "Found beacon -> " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textLastSend.setText("Last report: " + formatter.format(System.currentTimeMillis()));
                textStrength.setText("Strength: " + beacon.getRssi());
            }
        });

        try {
            JSONObject data = new JSONObject();
            data.put("timestamp", System.currentTimeMillis());
            data.put("uuid", beacon.getId1().toString());
            data.put("strength", beacon.getRssi());
            data.put("distance", beacon.getDistance());
            data.put("deviceid", android_id);

            sender.send(data);
        } catch (Exception e) {
            Log.i("MainActivity", "failed creating json", e);
        }
    }
}
