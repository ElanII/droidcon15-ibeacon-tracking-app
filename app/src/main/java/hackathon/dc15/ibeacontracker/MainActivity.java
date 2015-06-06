package hackathon.dc15.ibeacontracker;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONObject;

import java.util.Collection;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    private WebSocketSender sender;
    private long nextSendTime = 0;

    private String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        beaconManager.bind(this);

        sender = new WebSocketSender("http://100.100.238.92:3000");
        sender.connect();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this))  {
            beaconManager.setBackgroundMode(false);
        }
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

    private void handleBeacon(Beacon beacon) {
        if (System.currentTimeMillis() < nextSendTime) {
            Log.i("MainActivity", "-> DISCARDING");
            return;
        }
        nextSendTime = System.currentTimeMillis() + 3000;

        Log.i("MainActivity", "Found beacon -> " + beacon.toString() + " is about " + beacon.getDistance() + " meters away.");
        Log.i("MainActivity", "-> Strength " + beacon.getRssi());

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
