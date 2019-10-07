package com.example.testaar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.w3engineers.mesh.util.MeshLog;

import java.lang.reflect.Method;
import java.util.List;

public class LocalHotspotActivity extends AppCompatActivity implements BluetoothReceiver.Listener{
    private final String TAG = getClass().getName();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver bluetoothReceiver;
    private WifiManager wifiManager;
    private boolean isMesterEnable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_network);

        if(PermissionUtil.init(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            bluetoothReceiver = new BluetoothReceiver(this);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickButton(View view){

        turnOnHotspot();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {


        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                isMesterEnable = true;
                setBleVisibility();
                WifiConfiguration configuration = reservation.getWifiConfiguration();
                Log.d(TAG, "SSID ="+configuration.SSID);
                Log.d(TAG,"Preshare key ="+configuration.preSharedKey);
                Log.d(TAG,"Networ  ="+configuration.BSSID);
                String name = configuration.SSID+"@"+configuration.preSharedKey;
                bluetoothAdapter.setName(name);
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    private void setBleVisibility(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(intent, 100);
    }

    private void registerBleReceiver() {
        if (bluetoothReceiver != null) {
            //MeshLog.v("Register ble device receiver");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(bluetoothReceiver, intentFilter);
        }
    }

    public void unregisterBluetoothReceiver() {
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBluetoothFound(BluetoothDevice bluetoothDevice) {
        Log.d(TAG, "Bluetooth device  ="+bluetoothDevice.getName());
        String[] ssidPass = bluetoothDevice.getName().split("@");
        if(!isNetworkConnecting() && !isMesterEnable) {
            connectWifi(ssidPass[0], ssidPass[1]);
        }
    }

    @Override
    public void onScanFinished() {
        Log.d(TAG, "Bluetooth device stop scan");
        if(!isMesterEnable) {
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBleReceiver();
        bluetoothAdapter.startDiscovery();
        Log.d(TAG, "Bluetooth device start scan");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBluetoothReceiver();
    }

    private void connectWifi1(String ssid, String password) {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);

        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For RSN
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        int id = wifiManager.addNetwork(wifiConfig);

        if(id == -1){
            id= getNetIDBySSID(ssid);
        }

        if(id == -1){
            return;
        }

        wifiManager.disconnect();
        wifiManager.enableNetwork(id, true);
        boolean isSuccess = wifiManager.reconnect();
    }

    private int getNetIDBySSID(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs == null) {
            return -1;
        }

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals(ssid) || config.SSID.equals("\"" + ssid + "\"")) {
                return config.networkId;
            }
        }

        return -1;
    }


    public boolean connectWifi(String ssid, String passPhrase) {

        Log.d(TAG,"Network ssid ="+ssid+" pass ="+passPhrase);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", passPhrase);


        int networkId = getNetIDBySSID(ssid);
        if(networkId != -1) {
            wifiConfig.networkId = networkId;

            networkId = wifiManager.updateNetwork(wifiConfig);

            if(networkId == -1) {
                networkId = this.wifiManager.addNetwork(wifiConfig);

            }
        } else {
            networkId = this.wifiManager.addNetwork(wifiConfig);

        }

        Log.d(TAG,"Network id ="+networkId);

        wifiManager.enableNetwork(networkId, true);
        return wifiManager.reconnect();
    }

    public boolean isNetworkConnecting() {
        try {
            NetworkInfo activeNetwork = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (activeNetwork == null) {
                return false;
            }

            NetworkInfo.DetailedState state = activeNetwork.getDetailedState();
            if (state == NetworkInfo.DetailedState.CONNECTING
                    || state == NetworkInfo.DetailedState.OBTAINING_IPADDR
                    || state == NetworkInfo.DetailedState.CONNECTED
                    || state == NetworkInfo.DetailedState.AUTHENTICATING) {
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isHotspotEnable() {
        boolean isHotspot = false;
        try {
            WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
            int AP_STATE_ENABLING = 12;
            int AP_STATE_ENABLED = 13;
            isHotspot = actualState == AP_STATE_ENABLING || actualState == AP_STATE_ENABLED;
        } catch (Exception e) {
            isHotspot = false;
        }
        return isHotspot;
    }


}
