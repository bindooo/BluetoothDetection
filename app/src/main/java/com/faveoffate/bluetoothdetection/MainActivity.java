package com.faveoffate.bluetoothdetection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String BeaconMAC1 = "8C:8B:83:56:4E:D5";
    private final static String BeaconMAC2 = "20:91:48:12:C3:81";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

    TextView tv;
    String user, date, time, datetime;
    Context context;
    Calendar c;

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView2);
        Intent i = getIntent();
        user = i.getStringExtra(LoggedInActivity.EXTRA);
        context = getApplicationContext();

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {           //Check if BLE is supported
            Toast.makeText(this, "Bluetooth Low Energy Not Supported!", Toast.LENGTH_SHORT).show(); //Message that BLE not supported
            finish();                                                                               //End the app
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE); //Get the BluetoothManager
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {                                                    //Check if we got the BluetoothAdapter
            Toast.makeText(this, "Bluetooth Not Supported!", Toast.LENGTH_SHORT).show();    //Message that Bluetooth not supported
            finish();                                                                       //End the app
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {   //User chose not to enable Bluetooth.
            finish();                                                                       //Destroy the activity - end the application
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);                              //Pass the activity result up to the parent method
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            scanLeDevice(true);
            Log.d("Handler", "Task repetition running");
            mHandler.postDelayed(mStatusChecker, 5000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {                                           //Check if BT is not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //Create an intent to get permission to enable BT
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);                  //Fire the intent to start the activity that will return a result based on user response
        }
        mStatusChecker.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mStatusChecker);
        scanLeDevice(false);                                                            //Stop scanning for BLE devices
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {                                                                   //Method was called with option to start scanning
            mHandler.postDelayed(new Runnable() {                                       //Create delayed runnable that will stop the scan when it runs after SCAN_PERIOD milliseconds
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);                      //Stop scanning - callback method indicates which scan to stop
                }
            }, 4000);

            mBluetoothAdapter.startLeScan(mLeScanCallback);                             //Start scanning with callback method to execute when a new BLE device is found
        } else {                                                                          //Method was called with option to stop scanning
            mBluetoothAdapter.stopLeScan(mLeScanCallback);                              //Stop scanning - callback method indicates which scan to stop
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {   //Android calls method with Bluetooth device advertising information
            runOnUiThread(new Runnable() {                                              //Create runnable that will add the device to the list adapter
                @Override
                public void run() {
                    Log.d(TAG, "Found BLE Device: " + device.getAddress());                 //Debug information to log the devices as they are found
                    if (device.getAddress().equals(BeaconMAC1) || device.getAddress().equals(BeaconMAC2) || device.getAddress().equals("CE:82:41:09:2A:22")) {
                        scanLeDevice(false);
                        c = Calendar.getInstance();
                        date = dateformat.format(c.getTime());
                        time = timeformat.format(c.getTime());
                        datetime = date + " " + time;
                        Log.d(TAG, "Current datetime: " + datetime);
                        appendToTextView(datetime);

                        if (isNetworkAvailable()) {
                            AsyncHttpClient client = new AsyncHttpClient();
                            RequestParams params = new RequestParams();
                            params.put("id", user);
                            params.put("ts", datetime);
                            client.get("http://www.balaton-team.com/bringa_send.php", params, new TextHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, String res) {
                                            // called when response HTTP status is "200 OK"
                                            Toast.makeText(MainActivity.this, "Request sent successfully!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                            Toast.makeText(MainActivity.this, "Error sent by the server!", Toast.LENGTH_SHORT).show();
                                            appendToTextFile(datetime);
                                        }
                                    }
                            );
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Offline, appending to file!", Toast.LENGTH_SHORT).show();
                            appendToTextFile(datetime);
                        }
                        c.clear();
                    }
                }
            });
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void appendToTextView(String datetime) {
        tv.append(user + " " + datetime);
        tv.append("\n");
    }

    private void appendToTextFile(String datetime) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("timestamps.txt", Context.MODE_APPEND));
            outputStreamWriter.write(user + " " + datetime);
            outputStreamWriter.write("\n");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.d("Error: ", e.toString());
        }
    }
}
