package com.cxdev.voicenotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LISTENERS ------------------------------------
        // listener for history button
        findViewById(R.id.history).setOnClickListener(v -> {
            // start history activity
            startActivity(new Intent(this, HistoryActivity.class));
        });

        // listener for record button
        findViewById(R.id.recordButton).setOnClickListener(v -> {
            // toggle record button
            v.setSelected(!v.isSelected());
            Toast.makeText(this, v.isSelected() ? "Recording..." : "Stopped", Toast.LENGTH_SHORT).show();
        });

        // listener for settings button
        findViewById(R.id.settings).setOnClickListener(v -> {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();

            // toggle dark mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);

        });

        // listener for right button
        findViewById(R.id.rightButton).setOnClickListener(v -> {
            Toast.makeText(this, "Translate", Toast.LENGTH_SHORT).show();
        });

        // listener for left button
        findViewById(R.id.leftButton).setOnClickListener(v -> {
            Toast.makeText(this, "Translate", Toast.LENGTH_SHORT).show();
        });

        // listner for timer
        findViewById(R.id.timer).setOnClickListener(v -> {
            Toast.makeText(this, "Timer", Toast.LENGTH_SHORT).show();
        });

        // ------------------------------------------------------------------------
    }
}