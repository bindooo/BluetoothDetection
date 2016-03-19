package com.faveoffate.bluetoothdetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoggedInActivity extends AppCompatActivity {
    TextView userTextView;
    Button changeUserButton, startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        Intent i = getIntent();
        userTextView = (TextView)findViewById(R.id.userTextView);
        changeUserButton = (Button)findViewById(R.id.changeUserButton);
        startButton = (Button)findViewById(R.id.startButton);

        userTextView.setText(i.getStringExtra(LoginActivity.EXTRA));

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
                Intent i = new Intent(LoggedInActivity.this,MainActivity.class);
                startActivity(i);
            }
        });
    }
}
