package com.faveoffate.bluetoothdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    String date;
    String time;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm:ss");

    String dataUrl;
    String dataUrlParameters;
    URL url;
    HttpURLConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.textView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSocket = null;

        uuid ="0000111f-0000-1000-8000-00805f9b34fb";
/*
        ///getting own phone's uuids
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

        ///logging them
        for (ParcelUuid uuid: uuids) {
            Log.d("Device: ", "UUID: " + uuid.getUuid().toString());
        }
*/

        MY_UUID = UUID.fromString(uuid);

        if (mBluetoothAdapter == null) {
            tv.setText("No Bluetooth for you :(");
            Log.d("Device info: ", "No Bluetooth for you :(");
        }
        else {
            //enableBluetooth();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    findPairedDevices();
                    //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    //registerReceiver(mReceiver, filter);
                    //mBluetoothAdapter.startDiscovery();
                }
            }, 5000);

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
                    mSocket.close();
                    Log.d("Connect: ", "Disconnected");
                    ///TODO timestamp and HTTP REQUEST

                    date = dateformat.format(c.getTime());
                    time = timeformat.format(c.getTime());
                    Log.d("Current date ", date);
                    Log.d("Current time ", time);

                    dataUrl = "http://balaton-team.com/bringa_send.php";
                    dataUrlParameters = "id="+"phu"+"&ts="+date+"%"+time;
                    connection = null;
                    try {
                        // Create connection
                        url = new URL(dataUrl);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                        connection.setRequestProperty("Content-Length","" + Integer.toString(dataUrlParameters.getBytes().length));
                        connection.setRequestProperty("Content-Language", "en-US");
                        connection.setUseCaches(false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        // Send request
                        DataOutputStream wr = new DataOutputStream(
                                connection.getOutputStream());
                        wr.writeBytes(dataUrlParameters);
                        wr.flush();
                        wr.close();
                        // Get Response
                        InputStream is = connection.getInputStream();
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String line;
                        StringBuilder response = new StringBuilder();
                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                            response.append('\r');
                        }
                        rd.close();
                        String responseStr = response.toString();
                        Log.d("Server response ",responseStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
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
