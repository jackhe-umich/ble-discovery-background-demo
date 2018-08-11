package org.thaliproject.nativetest.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DataTransferService extends Service {
    public static final int SERVICE_ID = 1092;
    public DataTransferService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
