package com.faveoffate.bluetoothdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
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
        else {/*
            date = dateformat.format(c.getTime());
            time = timeformat.format(c.getTime());
            Log.d("Current date ", date);
            Log.d("Current time ", time);
*/
            SendHTTPRequest sr = new SendHTTPRequest();
            sr.execute(tv);
            //enableBluetooth();
            //mHandler.postDelayed(new Runnable() {
                //public void run() {
                    //findPairedDevices();
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
                    try {
                        mSocket.close();
                        Log.d("Connect: ", "Disconnected, socket closed");
                    } catch (IOException e1) {
                        Log.d("Closed: ","Socket not closed 1");
                        e1.printStackTrace();
                    }
                    Log.d("Connect: ", "Disconnected");
                    SendHTTPRequest sr = new SendHTTPRequest();
                    sr.execute(tv);
                } catch(IOException e){
                    try {
                        mSocket.close();
                        Log.d("Connect: ", "Cannot connect");
                        Log.d("Connect: ", "Socket closed");
                    } catch (IOException e1) {
                        Log.d("Closed: ","Socket not closed 2");
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

    private class SendHTTPRequest extends AsyncTask<TextView, Void, String> {
        String date;
        String time;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm:ss");
        TextView t;
        String responseStr = "fail";
        @Override
        protected String doInBackground(TextView... params) {
            this.t = params[0];

            date = dateformat.format(c.getTime());
            time = timeformat.format(c.getTime());
            Log.d("Current date ", date);
            Log.d("Current time ", time);

            String datetime = date + " " + time;

//            String urlString = "http://balaton-team.com/bringa_send.php?" + date + "%20" + time;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("balaton-team.com")
                    .appendPath("bringa_send.php")
                    .appendQueryParameter("id", "phu")
                    .appendQueryParameter("ts", datetime);
            String myUrl = builder.build().toString();

            URL url = null;
            try {
                //url = new URL("http://balaton-team.com/bringa_send.php?id=phu&ts=2016-03-18%2011:12:00");
                url = new URL(myUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                conn.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            // read the response
            try {
                System.out.println("Response Code: " + conn.getResponseCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream in = null;
            try {
                in = new BufferedInputStream(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                responseStr = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(responseStr);
            return responseStr;
        }
        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            t.setText(message);
        }
    }
}
