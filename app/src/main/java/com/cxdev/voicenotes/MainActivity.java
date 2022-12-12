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
    boolean onClickCalled = false;

    @Override
    protected void onStop() {
        super.onStop();
        speechRecognizer.stopVoiceStreaming();
        onClickCalled = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(16);


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
                if (onClickCalled) {
                    return;
                }
                onClickCalled = true;
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
//                startActivity(new Intent(MainActivity.this, TitleActivity.class));
            }
        });
    }

    // starts the timer above the button
    private void startTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int seconds = 0;
                while (isRecording) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    seconds++;
                    final int finalSeconds = seconds;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // make text red
                            ((TextView) findViewById(R.id.timer)).setTextColor(getResources().getColor(R.color.red));
                            ((TextView) findViewById(R.id.timer)).setText(String.format(Locale.getDefault(), "%02d:%02d", finalSeconds / 60, finalSeconds % 60));
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.timer)).setTextColor(getResources().getColor(R.color.white));
                    }
                });
            }
        }).start();
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
            // start recording
            try {
                mediaRecorder.setOutputFile(getOutputFileName());
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                startTimer();
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
            // stop recording
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            isRecording = false;
            speechRecognizer.stopVoiceStreaming();
            ((ImageView) findViewById(R.id.recordButton)).setImageAlpha(0xFF);
            ((TextView) findViewById(R.id.transcription)).setText("Stopped recording...");
        }
    }
}


