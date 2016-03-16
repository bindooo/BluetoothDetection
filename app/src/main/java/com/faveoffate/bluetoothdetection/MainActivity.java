package com.faveoffate.bluetoothdetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    TextView tv;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mSocket;
    String uuid;
    UUID MY_UUID;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        tv = (TextView) findViewById(R.id.textView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mSocket = null;

        uuid = "0000111f-0000-1000-8000-00805f9b34fb";
        MY_UUID = UUID.fromString(uuid);

        if (mBluetoothAdapter == null) {
            tv.setText("No Bluetooth for you :(");
            Log.d("Device info: ", "No Bluetooth for you :(");
        } else
            //enableBluetooth();
            startTaskLoop();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            findPairedDevices();
            Log.d("Handler", "Task repetition running");
            mHandler.postDelayed(mStatusChecker, 5000);
        }
    };

    void startTaskLoop() {
        mStatusChecker.run();
    }

    void stopTaskLoop() {
        mHandler.removeCallbacks(mStatusChecker);
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
                    Log.d("Socket: ", "Socket not created");
                    e1.printStackTrace();
                }
                try {
                    mSocket.connect();
                    Log.d("Connect: ", "Connected");
                    try {
                        mSocket.close();
                        Log.d("Connect: ", "Disconnected, socket closed");
                    } catch (IOException e1) {
                        Log.d("Closed: ", "Socket not closed 1");
                        e1.printStackTrace();
                    }
                    Log.d("Connect: ", "Disconnected");
                    SendHTTPRequest sr = new SendHTTPRequest();
                    sr.execute(tv);
                } catch (IOException e) {
                    try {
                        mSocket.close();
                        Log.d("Connect: ", "Cannot connect");
                        Log.d("Connect: ", "Socket closed");
                    } catch (IOException e1) {
                        Log.d("Closed: ", "Socket not closed 2");
                        e1.printStackTrace();
                    }
                }
            }
        } else
            tv.setText("No paired devices!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTaskLoop();
    }

    private class SendHTTPRequest extends AsyncTask<TextView, Void, String> {
        TextView t;
        String responseStr = "fail";

        @Override
        protected String doInBackground(TextView... params) {
            this.t = params[0];

            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
            String date = dateformat.format(c.getTime());
            String time = timeformat.format(c.getTime());
            Log.d("Current date ", date);
            Log.d("Current time ", time);
            String datetime = date + " " + time;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("balaton-team.com")
                    .appendPath("bringa_send.php")
                    .appendQueryParameter("id", "phu")
                    .appendQueryParameter("ts", datetime);
            String myUrl = builder.build().toString();

            URL url;
            try {
                url = new URL(myUrl);
                HttpURLConnection conn;
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                System.out.println("Response Code: " + conn.getResponseCode());
                InputStream in;
                in = new BufferedInputStream(conn.getInputStream());
                responseStr = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
            } catch (MalformedURLException e) {
                e.printStackTrace();
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
