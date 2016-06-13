package com.faveoffate.bluetoothdetection;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cz.msebera.android.httpclient.Header;

public class LoggedInActivity extends AppCompatActivity {
    public static final String EXTRA = "extra";
    private static final String filename = "timestamps.txt";
    private ProgressBar bar;
    private Context context;
    TextView userTextView;
    Button changeUserButton, startButton, syncButton, showTextButton;
    String user, line;
    StringBuilder sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        Intent i = getIntent();
        user = i.getStringExtra("user");
        user = user.replaceAll("(\\r|\\n|\\t)", "");

        userTextView = (TextView) findViewById(R.id.userTextView);
        changeUserButton = (Button) findViewById(R.id.changeUserButton);
        startButton = (Button) findViewById(R.id.startButton);
        syncButton = (Button) findViewById(R.id.syncButton);
        showTextButton = (Button) findViewById(R.id.showTextButton);
        bar = (ProgressBar) findViewById(R.id.progressBar);

        sb = new StringBuilder();

        userTextView.setText(user);

        changeUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoggedInActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoggedInActivity.this, MainActivity.class);
                i.putExtra(EXTRA, user);
                startActivity(i);
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    bar.setVisibility(View.VISIBLE);
                    Toast.makeText(LoggedInActivity.this, "Syncing!", Toast.LENGTH_LONG).show();
                    synchronize();
                    bar.setVisibility(View.GONE);
                } else
                    Toast.makeText(LoggedInActivity.this, "Please connect to the internet to proceed!", Toast.LENGTH_LONG).show();
            }
        });

        showTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoggedInActivity.this, ShowTextActivity.class);
                i.putExtra(EXTRA, user);
                startActivity(i);
            }
        });

        context = getApplicationContext();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void synchronize() {

        try {
            InputStream inputStream = openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(new BufferedReader(inputStreamReader));
                while ((line = br.readLine()) != null) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.put("id", line.substring(0, 3));
                    params.put("ts", line.substring(4, 23));
                    client.get("http://www.balaton-team.com/bringa_send.php", params, new TextHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, String res) {
                                    Log.d("Synchronizing", "Successful");
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                    sb.append(line);
                                    sb.append("\n");
                                }
                            }
                    );
                }
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
                    outputStreamWriter.write(sb.toString());
                    outputStreamWriter.close();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error writing file!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "File not found!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
