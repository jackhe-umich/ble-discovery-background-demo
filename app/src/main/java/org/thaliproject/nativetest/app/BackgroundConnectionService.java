package org.thaliproject.nativetest.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BackgroundConnectionService extends IntentService {
    private ConnectionEngine mConnectionEngine;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundConnectionService(String name) {
        super(name);
    }

    public BackgroundConnectionService() {
        super("Anonymouse No Intent");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("BackgroundConnService","Service Started");
        createAndStartEngine();
        Log.d("BackgroundConnService","Service Started");
    }

    private void createAndStartEngine() {
        if (mConnectionEngine == null) {
            mConnectionEngine = new ConnectionEngine(getApplicationContext());
            mConnectionEngine.bindSettings();
        }

        mConnectionEngine.start();
        mConnectionEngine.startBluetoothDeviceDiscovery();
        mConnectionEngine.makeDeviceDiscoverable();
    }
}
