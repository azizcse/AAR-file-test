package com.example.testaar;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback;
import android.os.Build;
import android.os.Handler;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.mesh.TransportManager;
import com.w3engineers.mesh.TransportState;
import com.w3engineers.mesh.wifi.dispatch.LinkStateListener;
import com.w3engineers.mesh.wifi.protocol.Link;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LinkStateListener{
    private TransportManager transportManager;
    private final int APP_PORT = 4757;
    private Map<String, Link> connectionMap = new HashMap<>();
    private TextView textView;
    private String myNodeId;
    private String TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.discovered_node);
        transportManager = TransportManager.on(getApplicationContext(), "aartest", this);

    }
    public void startRunnable(View view) {
        for(Map.Entry<String, Link> item: connectionMap.entrySet()){
            Link link = item.getValue();
            link.sendFrame(item.getKey(), myNodeId, "Hello".getBytes());
        }
    }

    public void stopRunnable(View view){
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e("MainActivity_runnable", "runnable called ");
            HandlerUtil.postBackground(this, 5000);
        }
    };


    @Override
    public void onTransportInit(String nodeId, TransportState transportState, String msg) {
        if(transportState.ordinal() == TransportState.SUCCESS.ordinal()) {

            myNodeId = nodeId;
            transportManager.configTransport(nodeId, 4546, "hello".getBytes());
            transportManager.startMesh();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Init success",Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Init failed",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void linkConnected(String nodeId, Link link) {
        Log.e("MainActivity_runnable", "linkConnected = "+nodeId);
        connectionMap.put(nodeId, link);
    }

    @Override
    public void onMeshLinkFound(String nodeId, Link link) {
        Log.e("MainActivity_runnable", "linkConnected = "+nodeId);
        connectionMap.put(nodeId, link);
    }

    @Override
    public void linkDisconnected(Link link) {
        Log.e("MainActivity_runnable", "linkConnected = "+link.getNodeId());
        connectionMap.remove(link.getNodeId());
    }

    @Override
    public void linkDidReceiveFrame(String sender, byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Msg received",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMeshLinkDisconnect(String nodeId) {
        connectionMap.remove(nodeId);
        Log.e("MainActivity_runnable", "runnable called ");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageDelivered(long deliverId, boolean status) {
        Log.e("MainActivity_runnable", "runnable called ");

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);

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


    @RequiresApi(api = Build.VERSION_CODES.O)
    class HotspotCallback extends WifiManager.LocalOnlyHotspotCallback{
        @Override
        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
            super.onStarted(reservation);
        }

        @Override
        public void onFailed(int reason) {
            super.onFailed(reason);
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }
    }


    private void startLocalHotspot(){

    }
}
