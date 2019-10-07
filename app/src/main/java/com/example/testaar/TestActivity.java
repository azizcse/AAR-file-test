package com.example.testaar;

import android.Manifest;
import android.app.AppComponentFactory;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestActivity extends AppCompatActivity {
    WifiManager manager;//= (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    private String TAG = "TestActivity";
    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startRunnable(View view) {
        if (PermissionUtil.init(this).request(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            start();
        }
    }


    public void stopRunnable(View view) {
        if(isEnable()){
            Log.d(TAG, "Enabled true");
        }else {
            Log.d(TAG, "Enabled false");
        }

        stop();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void start() {
        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                mReservation = reservation;
                WifiConfiguration con = mReservation.getWifiConfiguration();
                Log.d(TAG, "id =" + con.SSID);
                Log.d(TAG, "pass =" + con.preSharedKey);
                Log.d(TAG, "bid =" + con.BSSID);
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

    private void stop() {
        mReservation.close();
    }
    public static int AP_STATE_DISABLING = 10;
    public static int AP_STATE_DISABLED = 11;
    public static int AP_STATE_ENABLING = 12;
    public static int AP_STATE_ENABLED = 13;
    public static int AP_STATE_FAILED = 14;
    private boolean isEnable() {
        Method method = null;
        try {
            method = manager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int actualState = (Integer) method.invoke(manager, (Object[]) null);
            if(actualState ==AP_STATE_ENABLED){
                return true;
            }
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;

    }
}
