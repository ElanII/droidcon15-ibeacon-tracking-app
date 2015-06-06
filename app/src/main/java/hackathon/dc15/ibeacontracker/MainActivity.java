package hackathon.dc15.ibeacontracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.beaconinside.androidsdk.BeaconService;


public class MainActivity extends ActionBarActivity {

    private BeaconReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart");

        BeaconService.init(this, null);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BeaconService.INTENT_BEACON_REGION_ENTER);
        intentFilter.addAction(BeaconService.INTENT_BEACON_REGION_EXIT);

        receiver = new BeaconReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop");

        BeaconService.terminate(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class BeaconReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("MainActivity", "onReceive beacon data = " + action);

            if (BeaconService.INTENT_BEACON_REGION_ENTER.equals(action)) {
                // do something
            } else if (BeaconService.INTENT_BEACON_REGION_EXIT.equals(action)) {
                // do something
            }
        }
    }
}
