package com.faveoffate.bluetoothdetection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    TextView tv;
    String user;
    Context context;

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
            mHandler.postDelayed(mStatusChecker, 30000);
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
                    mScanning = false;                                                  //Indicate that we are not scanning - used for menu Stop/Scan context
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);                      //Stop scanning - callback method indicates which scan to stop
                    invalidateOptionsMenu();                                            //Indicate that the options menu has changed, so should be recreated.
                }
            }, 10000);

            mScanning = true;                                                           //Indicate that we are busy scanning - used for menu Stop/Scan context
            mBluetoothAdapter.startLeScan(mLeScanCallback);                             //Start scanning with callback method to execute when a new BLE device is found
        } else {                                                                          //Method was called with option to stop scanning
            mScanning = false;                                                          //Indicate that we are not scanning - used for menu Stop/Scan context
            mBluetoothAdapter.stopLeScan(mLeScanCallback);                              //Stop scanning - callback method indicates which scan to stop
        }
        invalidateOptionsMenu();                                                        //Indicate that the options menu has changed, so should be recreated.
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {   //Android calls method with Bluetooth device advertising information
            runOnUiThread(new Runnable() {                                              //Create runnable that will add the device to the list adapter
                @Override
                public void run() {
                    //tv.append(device.getName() + " " + device.getAddress() + "\n");
                    Log.d(TAG, "Found BLE Device: " + device.getAddress());                 //Debug information to log the devices as they are found
                    //if (device.getAddress().equals("D5:10:43:0B:99:2F")) {
                    if (device.getAddress().equals("CE:82:41:09:2A:22")) {
                        scanLeDevice(false);
                        appendToTextView();
                        appendToTextFile();
                        SendHTTPRequest sr = new SendHTTPRequest();
                        sr.execute(tv);
                        //Toast.makeText(MainActivity.this, "Little nut found!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private void appendToTextView() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        String date = dateformat.format(c.getTime());
        String time = timeformat.format(c.getTime());
        String datetime = date + " " + time;
        tv.append(user + " " + datetime);
        tv.append("\n");
    }

    private void appendToTextFile() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("timestamps.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(tv.getText().toString());
            outputStreamWriter.write("\n");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.d("Error: ", e.toString());
        }
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
                Toast.makeText(MainActivity.this, "Response from server: " + message, Toast.LENGTH_SHORT).show();
        }
    }
}
