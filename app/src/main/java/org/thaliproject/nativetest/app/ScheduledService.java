/***
  Copyright (c) 2013-2015 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package org.thaliproject.nativetest.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledService extends Service
  implements Runnable {
  private static final String CHANNEL_WHATEVER="channel_whatever";
  private static int NOTIFY_ID=1337;

  private ScheduledExecutorService sched=
    Executors.newSingleThreadScheduledExecutor();
  private PowerManager.WakeLock wakeLock;
    private ConnectionEngine mConnectionEngine = null;
  @Override
  public void onCreate() {
    super.onCreate();

    PowerManager mgr=(PowerManager)getSystemService(POWER_SERVICE);

    wakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
      getClass().getSimpleName());
    wakeLock.acquire();
      foregroundify();
    createAndStartEngine();
    sched.scheduleAtFixedRate(this, 0, 9, TimeUnit.SECONDS);
  }

  @Override
  public void run() {

      mConnectionEngine.makeDeviceDiscoverable();
  }

  @Override
  public void onDestroy() {
    sched.shutdownNow();
    wakeLock.release();
    stopForeground(true);
    mConnectionEngine.dispose();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new IllegalStateException("Go away");
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

  private void foregroundify() {
    NotificationManager mgr=
            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O &&
            mgr.getNotificationChannel(CHANNEL_WHATEVER)==null) {
      mgr.createNotificationChannel(new NotificationChannel(CHANNEL_WHATEVER,
              "Whatever", NotificationManager.IMPORTANCE_DEFAULT));
    }

    NotificationCompat.Builder b=
            new NotificationCompat.Builder(this, CHANNEL_WHATEVER);
    Intent iActivity=new Intent(this, MainActivity.class);
    PendingIntent piActivity=
            PendingIntent.getActivity(this, 0, iActivity, 0);
    Intent iReceiver=new Intent(this, StopReceiver.class);
    PendingIntent piReceiver=
            PendingIntent.getBroadcast(this, 0, iReceiver, 0);

    b.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(piActivity)
            .setSmallIcon(R.drawable.ic_arrow_upward_blue_24dp)
            .setTicker(getString(R.string.app_name))
            .addAction(R.drawable.ic_send_white_24dp,
                    getString(R.string.notif_stop),
                    piReceiver);

    startForeground(NOTIFY_ID, b.build());
  }

}
