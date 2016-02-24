package com.faveoffate.bluetoothdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_ENABLE_BT = 1;
    private Handler mHandler = new Handler();
    TextView tv;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mSocket;
    TelephonyManager tManager;
    String uuid;
    UUID MY_UUID;
    Set<BluetoothDevice> pairedDevices;
    int rssi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.textView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSocket = null;

        uuid ="0000111f-0000-1000-8000-00805f9b34fb";
/*
        Method getUuidsMethod = null;
        try {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        ParcelUuid[] uuids = new ParcelUuid[0];
        try {
            uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        for (ParcelUuid uuid: uuids) {
            Log.d("Device: ", "UUID: " + uuid.getUuid().toString());
        }
*/
        //tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //uuid = tManager.getDeviceId();
        MY_UUID = UUID.fromString(uuid);

        if (mBluetoothAdapter == null) {
            tv.setText("No Bluetooth for you :(");
        }
        else {
            //enableBluetooth();
            //mHandler.postDelayed(new Runnable() {
                //public void run() {
                    findPairedDevices();
                    //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    //registerReceiver(mReceiver, filter);
                    //mBluetoothAdapter.startDiscovery();
                //}
            //}, 5000);

        }
    }
    protected void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    protected void findPairedDevices() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("Device: ", device.getName() + device.getAddress() + "\n");
                try {
                    mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                    Log.d("Socket: ","Socket not created");
                    e1.printStackTrace();
                }
                //device.getUuids();
                try{
                    mSocket.connect();
                    Log.d("Connect: ", "Connected");
                } catch(IOException e){
                    try {
                        mSocket.close();
                        Log.d("Connect: ", "Cannot connect");
                    } catch (IOException e1) {
                        Log.d("Closed: ","Socket not closed");
                        e1.printStackTrace();
                    }
                }
            }
        }
        else
            tv.setText("No paired devices :(");
    }
    protected void checkIfPaired(String foundMacAddress) {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(foundMacAddress))
                    Toast.makeText(MainActivity.this,Integer.toString(rssi), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d("Device:", device.getName() + " " + device.getAddress() + " " +rssi + "\n");
                checkIfPaired(device.getAddress());
            }
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mReceiver);
        //mBluetoothAdapter.cancelDiscovery();
    }
}
