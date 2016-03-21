package com.faveoffate.bluetoothdetection;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    EditText idEditText,passwordEditText,loginEditText;
    Button loginButton;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();

        idEditText = (EditText)findViewById(R.id.idEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        loginEditText = (EditText)findViewById(R.id.loginEditText);
        loginEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(loginEditText.getText().toString().contains("NOK") || idEditText.getText().toString().equals("") || passwordEditText.getText().toString().equals("")) {
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(LoginActivity.this, "Authentication successful", Toast.LENGTH_LONG).show();

                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user.txt", Context.MODE_PRIVATE));
                        outputStreamWriter.write(idEditText.getText().toString());
                        outputStreamWriter.write("\n");
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        Log.d("Error: ", e.toString());
                    }

                    Intent i = new Intent(LoginActivity.this, LoggedInActivity.class);
                    i.putExtra("user", idEditText.getText().toString());
                    startActivity(i);
                }
            }
        });
    }
    protected void login() {
        if(isNetworkAvailable()) {
            SendLoginInfo sr = new SendLoginInfo(loginEditText, idEditText.getText().toString(), passwordEditText.getText().toString());
            sr.execute();
        }
        else
            Toast.makeText(LoginActivity.this,"Please connect to the internet to proceed!",Toast.LENGTH_LONG).show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class SendLoginInfo extends AsyncTask<String, Void, String> {
        EditText editText;
        String idString;
        String pwString;

        public SendLoginInfo(EditText editText, String idString, String pwString){
            this.editText = editText;
            this.idString = idString;
            this.pwString = pwString;
        }

        @Override
        protected String doInBackground(String... params) {
            String responseStr = "fail";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("balaton-team.com")
                    .appendPath("bringa_send.php")
                    .appendQueryParameter("usr", idString)
                    .appendQueryParameter("pwd", pwString);
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
            editText.setText(message);
        }
    }
}
