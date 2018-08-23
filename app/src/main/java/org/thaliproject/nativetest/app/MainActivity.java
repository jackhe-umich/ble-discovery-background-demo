/* Copyright (c) 2015-2016 Microsoft Corporation. This software is licensed under the MIT License.
 * See the license file delivered with this project for further information.
 */
package org.thaliproject.nativetest.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import org.thaliproject.nativetest.app.model.PeerAndConnectionModel;
import org.thaliproject.p2p.btconnectorlib.PeerProperties;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, PeerAndConnectionModel.Listener {

    private static final String TAG = MainActivity.class.getName();

    private static MainActivity mThisInstance = null;
    private static Context mContext = null;

    private ConnectionManager mConnectionManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        mThisInstance = this;
        mContext = getApplicationContext();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();




        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
            String pkg=getPackageName();
            PowerManager pm=getSystemService(PowerManager.class);

            if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                Intent i=
                        new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:"+pkg));

                startActivity(i);
            }
        }

        //createAndStartEngine();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(mThisInstance,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

                // No explanation needed; request the permission
            ActivityCompat.requestPermissions(mThisInstance,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        mConnectionManager.startConnection("This is the thing I want to send");
    }

    /**
     * Updates the options menu.
     */
    public static void updateOptionsMenu() {
        if (mThisInstance != null) {
            Log.d(TAG, "updateOptionsMenu");
            mThisInstance.invalidateOptionsMenu();
        }
    }

    /**
     * Displays a toast with the given message.
     *
     * @param message The message to show.
     */
    public static void showToast(final String message) {
        final Context context = mContext;

        if (context != null) {
            Handler handler = new Handler(mContext.getMainLooper());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    CharSequence text = message;
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        }
    }

    @Override
    public void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
        //mConnectionEngine.start();
        //mConnectionEngine.makeDeviceDiscoverable();
        //mConnectionEngine.startBluetoothDeviceDiscovery();
        Intent startServiceIntent = new Intent(this, BackgroundConnectionService.class);
        startService(startServiceIntent);
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        //mConnectionEngine.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        //destroyEngine();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }



    @Override
    public void onDataChanged() {

    }

    @Override
    public void onPeerRemoved(PeerProperties peerProperties) {

    }
}
