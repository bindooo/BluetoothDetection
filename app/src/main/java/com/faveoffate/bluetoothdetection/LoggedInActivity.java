package com.faveoffate.bluetoothdetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoggedInActivity extends AppCompatActivity {
    public static final String EXTRA = "extra";
    TextView userTextView;
    Button changeUserButton, startButton, showTextButton;
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedin);
        Intent i = getIntent();
        user = i.getStringExtra("user");
        user = user.replaceAll("(\\r|\\n|\\t)", "");

        userTextView = (TextView)findViewById(R.id.userTextView);
        changeUserButton = (Button)findViewById(R.id.changeUserButton);
        startButton = (Button)findViewById(R.id.startButton);
        showTextButton = (Button)findViewById(R.id.showTextButton);

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

        showTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoggedInActivity.this, ShowTextActivity.class);
                i.putExtra(EXTRA, user);
                startActivity(i);
            }
        });
    }
}
