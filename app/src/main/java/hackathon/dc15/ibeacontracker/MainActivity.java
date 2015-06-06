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
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {

    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    private WebSocketSender sender;
    private long nextSendTime = 0;
    private String androidDeviceId;
    private Set<String> beaconUuids;

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

        androidDeviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        textDeviceId.setText("Device id: " + androidDeviceId);

        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        beaconUuids = new HashSet<>();
        for (String uuid : getResources().getStringArray(R.array.beacon_uuids)) {
            beaconUuids.add(uuid);
        }

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
                    if (beaconUuids.contains(beacon.getId1().toString())) {
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
            data.put("deviceid", androidDeviceId);

            sender.send(data);
        } catch (Exception e) {
            Log.i("MainActivity", "failed creating json", e);
        }
    }
}
