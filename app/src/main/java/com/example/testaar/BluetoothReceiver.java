package com.example.testaar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.w3engineers.mesh.db.SharedPref;
import com.w3engineers.mesh.util.Constant;

public class BluetoothReceiver extends BroadcastReceiver {
    private String prefix = "AndroidShare";
    public interface Listener {
        void onBluetoothFound(BluetoothDevice bluetoothDevice);
        void onScanFinished();
    }

    private Listener bluetoothListener;

    public BluetoothReceiver(Listener listener) {
        this.bluetoothListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null || action.isEmpty()) return;

        switch (action) {
            case BluetoothDevice.ACTION_FOUND:

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getName() != null && device.getName().startsWith(prefix)) {
                    bluetoothListener.onBluetoothFound(device);
                }

                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                bluetoothListener.onScanFinished();
                break;
        }
    }
}
