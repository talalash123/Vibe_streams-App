package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // FIX: Redirect to the Login screen instead of the user home
            Intent intent = new Intent(SplashActivity.this, activity_login.class);
            startActivity(intent);
            finish();
        }, 500);
    }
}
