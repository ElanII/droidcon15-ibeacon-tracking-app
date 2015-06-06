package hackathon.dc15.ibeacontracker;

import android.app.Application;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

public class BeaconApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        // configured to see beaconinside beacons
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
    }
}