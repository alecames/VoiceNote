package com.cxdev.voicenotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.time.Instant;

public class MainActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder = new MediaRecorder();
    SpeechRecognizer speechRecognizer = new SpeechRecognizer();
    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mediaRecorder.setOutputFile(getOutputFileName());

        // listener for history button
        findViewById(R.id.history).setOnClickListener(v -> {
            // start history activity
            startActivity(new Intent(this, HistoryActivity.class));
        });

        // listener for record button
        findViewById(R.id.recordButton).setOnClickListener(v -> {
            try {
                toggleRecording(getOutputFileName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // toggle recording
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
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        });

        // listener for timer
        findViewById(R.id.timer).setOnClickListener(v -> {
            Toast.makeText(this, "Timer", Toast.LENGTH_SHORT).show();
        });

        // ------------------------------------------------------------------------
    }

    private String getOutputFileName() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File audioFolder = new File(filePath, "MyAudioFolder");
        audioFolder.mkdirs();
        Instant currentTime = Instant.now();
        long seconds = currentTime.getEpochSecond();
        String cTime = Long.toString(seconds);
        return new File(audioFolder, cTime + ".3gp").getAbsolutePath();
    }
    private void toggleRecording(String audioFilePath) throws IOException {
        if (isRecording) {
            // Stop recording and save the audio file
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
        } else {
            // Start recording audio
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        }
    }
 }

