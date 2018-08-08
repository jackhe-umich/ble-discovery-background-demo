/* Copyright (c) 2016 Microsoft Corporation. This software is licensed under the MIT License.
 * See the license file delivered with this project for further information.
 */
package org.thaliproject.p2p.btconnectorlib.internal.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.util.Log;

/**
 * Class for discovering nearby Bluetooth devices (the old-fashioned way) i.e. devices that are
 * made discoverable.
 *
 * From http://developer.android.com/guide/topics/connectivity/bluetooth.html:
 *
 * Caution: Performing device discovery is a heavy procedure for the Bluetooth adapter and will
 * consume a lot of its resources. Once you have found a device to connect, be certain that you
 * always stop discovery with cancelDiscovery() before attempting a connection. Also, if you already
 * hold a connection with a device, then performing discovery can significantly reduce the bandwidth
 * available for the connection, so you should not perform discovery while connected.
 */
public class BluetoothDeviceDiscoverer {
    public interface BluetoothDeviceDiscovererListener {
        void onBluetoothDeviceDiscovered(BluetoothDevice bluetoothDevice);
    }

    private static final String TAG = BluetoothDeviceDiscoverer.class.getName();
    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothDeviceDiscovererListener mListener;
    private BluetoothDeviceDiscovererBroadcastReceiver mBroadcastReceiver = null;
    private CountDownTimer mStopBluetoothDeviceDiscoveryTimer = null;

    /**
     * Constructor.
     * @param context The application context.
     * @param bluetoothAdapter The Bluetooth adapter.
     * @param listener The listener.
     */
    public BluetoothDeviceDiscoverer(
            Context context, BluetoothAdapter bluetoothAdapter,
            BluetoothDeviceDiscovererListener listener) {
        mContext = context;
        mBluetoothAdapter = bluetoothAdapter;
        mListener = listener;
    }

    /**
     * @return True, if the device discovery is active. False otherwise.
     */
    public boolean isRunning() {
        return (mBroadcastReceiver != null && mBluetoothAdapter.isDiscovering());
    }

    /**
     * Starts the device discovery.
     * @param durationInMilliseconds The duration for the Bluetooth device discovery in milliseconds.
     *                               Use 0 for indefinite duration.
     * @return True, if started successfully. False otherwise.
     */
    public synchronized boolean start(long durationInMilliseconds) {
        boolean wasStarted = false;

        if (mStopBluetoothDeviceDiscoveryTimer != null) {
            mStopBluetoothDeviceDiscoveryTimer.cancel();
            mStopBluetoothDeviceDiscoveryTimer = null;
        }

        if (mBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mBroadcastReceiver = new BluetoothDeviceDiscovererBroadcastReceiver();

            try {
                mContext.registerReceiver(mBroadcastReceiver, filter);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "start: Failed to register the broadcast receiver: " + e.getMessage(), e);
                mBroadcastReceiver = null;
            }

            if (mBroadcastReceiver != null) {
                if (mBluetoothAdapter.isDiscovering() || mBluetoothAdapter.startDiscovery()) {
                    Log.i(TAG, "start: OK");
                    wasStarted = true;

                    if (durationInMilliseconds > 0) {
                        mStopBluetoothDeviceDiscoveryTimer =
                                new CountDownTimer(durationInMilliseconds, durationInMilliseconds) {
                            @Override
                            public void onTick(long l) {
                                // Not used
                            }

                            @Override
                            public void onFinish() {
                                Log.d(TAG, "Bluetooth device discovery timeout");
                                this.cancel();
                                mStopBluetoothDeviceDiscoveryTimer = null;
                                stop();
                            }
                        };

                        mStopBluetoothDeviceDiscoveryTimer.start();
                    }
                } else {
                    Log.e(TAG, "start: Failed to start discovery, stopping...");
                    stop();
                }
            }
        }

        return wasStarted;
    }

    /**
     * Stops the device discovery.
     */
    public synchronized void stop() {
        Log.i(TAG, "stop");

        if (mStopBluetoothDeviceDiscoveryTimer != null) {
            mStopBluetoothDeviceDiscoveryTimer.cancel();
            mStopBluetoothDeviceDiscoveryTimer = null;
        }

        if (mBluetoothAdapter.isDiscovering()) {
            if (!mBluetoothAdapter.cancelDiscovery()) {
                Log.e(TAG, "stop: Failed to cancel discovery");
            }
        }

        if (mBroadcastReceiver != null) {
            try {
                mContext.unregisterReceiver(mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "start: Failed to register the broadcast receiver: " + e.getMessage(), e);
            }

            mBroadcastReceiver = null;
        }
    }

    private class BluetoothDeviceDiscovererBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mListener != null) {
                    mListener.onBluetoothDeviceDiscovered(bluetoothDevice);
                }
            }
        }
    }
}
