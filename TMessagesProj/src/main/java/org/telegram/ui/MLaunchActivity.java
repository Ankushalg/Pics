package org.telegram.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.telegram.messenger.R;

// <> Pics

public class MLaunchActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlaunch);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        findViewById(R.id.ml_b1).setOnClickListener(v -> {
            Intent i = new Intent(MLaunchActivity.this, LaunchActivity.class);
            startActivity(i);
        });

        findViewById(R.id.ml_b2).setOnClickListener(v -> {
            Intent i = new Intent(MLaunchActivity.this, PicsMainActivity.class);
            startActivity(i);
        });
    }

// </> Pics
}