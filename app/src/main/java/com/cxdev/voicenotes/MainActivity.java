package com.cxdev.voicenotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder = new MediaRecorder();
    SpeechRecognizer speechRecognizer = new SpeechRecognizer();
    boolean isRecording = false;

    @Override
    protected void onStop() {
        super.onStop();
        speechRecognizer.stopVoiceStreaming();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check for mic permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // check for storage permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        // listener for history button
        findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start history activity
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        // listener for record button
        findViewById(R.id.recordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if recording
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        // listener for settings button
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // listener for left button
        findViewById(R.id.leftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }
        });

        // listener for right button
        findViewById(R.id.rightButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // trigger popup menu to enter title
                startActivity(new Intent(MainActivity.this, TitleDialogFragment.class));
            }
        });
    }

    // start timer
    private void startTimer() {
        long startTime = Instant.now().getEpochSecond();
        TextView timer = findViewById(R.id.timer);
        timer.setTextColor(getResources().getColor(R.color.red));
        timer.setText("00:00");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    long currentTime = Instant.now().getEpochSecond();
                    long timeDifference = currentTime - startTime;
                    long minutes = timeDifference / 60;
                    long seconds = timeDifference % 60;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                        }
                    });
                    try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }).start();
    }

    // stop timer
    private void stopTimer() {
        TextView timer = findViewById(R.id.timer);
        timer.setTextColor(getResources().getColor(R.color.white));
    }


//     get the file name for the recording
    private String getOutputFileName() {
        File file = new File(getExternalFilesDir(null), "recordings");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/" + Instant.now().getEpochSecond() + ".mp3";
    }

    private void startRecording() {
        if (!isRecording) {
            try {
                startTimer();
                isRecording = true;
                speechRecognizer.startVoiceStreaming();
                ((ImageView) findViewById(R.id.recordButton)).setImageAlpha(0x80);
                System.out.println("Recording started");
                ((TextView) findViewById(R.id.transcription)).setText("Recording...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (isRecording) {
            stopTimer();
            isRecording = false;
            speechRecognizer.stopVoiceStreaming();
            ((ImageView) findViewById(R.id.recordButton)).setImageAlpha(0xFF);
            ((TextView) findViewById(R.id.transcription)).setText("Stopped recording...");
        }
    }
}


