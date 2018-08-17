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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledService extends Service
  implements Runnable {
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
    createAndStartEngine();
    sched.scheduleAtFixedRate(this, 0, 9, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
      mConnectionEngine.startBluetoothDeviceDiscovery();
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

}
