package com.faveoffate.bluetoothdetection;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

public class CheckUserActivity extends AppCompatActivity{
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
            Intent i = new Intent(CheckUserActivity.this, LoggedInActivity.class);
            startActivity(i);
        }
        else {
            Log.d("File: ", "File not exists");
            Intent i = new Intent(CheckUserActivity.this, LoginActivity.class);
            startActivity(i);
        }
    }
}
