package com.faveoffate.bluetoothdetection;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckUserActivity extends AppCompatActivity {
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkuser);
        checkFile();
    }
    protected void checkFile() {
        ContextWrapper c = new ContextWrapper(this);
        String path = c.getFilesDir().getPath();
        File file = new File(path + "/user.txt");
        if(file.exists()) {
            Log.d("File: ", "File exists");

            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                user = text.toString();
            }
            catch (Exception e)
            {
                String error="";
                error=e.getMessage();
                Log.d("Error: ",error);
            }

            Intent i = new Intent(CheckUserActivity.this, LoggedInActivity.class);
            i.putExtra("user", user);
            startActivity(i);
        }
        else {
            Log.d("File: ", "File not exists");
            Intent i = new Intent(CheckUserActivity.this, LoginActivity.class);
            startActivity(i);
        }
    }
}
