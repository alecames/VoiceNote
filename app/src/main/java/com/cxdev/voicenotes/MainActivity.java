package com.cxdev.voicenotes;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder = new MediaRecorder();
    SpeechRecognizer speechRecognizer = new SpeechRecognizer();
    NotesDBH db = new NotesDBH(MainActivity.this);
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
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // listener for left button
        findViewById(R.id.leftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        // listener for right button
        findViewById(R.id.rightButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                }
                showTitleDialog(MainActivity.this);
            }
        });
    }

    private void cancel() {
        stopRecording();
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // stop timer
    private void stopTimer() {
        TextView timer = findViewById(R.id.timer);
        timer.setTextColor(getResources().getColor(R.color.white));
    }

    // title window
    private void showTitleDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter a Title");
        final EditText input = new EditText(context);
        builder.setView(input);

        // add button to save the user input
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();

                // get text from textview
                TextView textView = findViewById(R.id.transcription);
                String noteContent = textView.getText().toString();
                if (!title.isEmpty()) {
                    db.addNote( new Note(db.getNotesCount(),
                            title,
                            noteContent,
                            Instant.now().toString()));
                    Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a title", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add a button to cancel the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
                dialog.cancel();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void startRecording() {
        if (!isRecording) {
            // start recording
            try {
                startTimer();
                isRecording = true;
                speechRecognizer.startVoiceStreaming();
                ((ImageView) findViewById(R.id.recordButton)).setImageResource(R.drawable.record_button_recording);
                ((TextView) findViewById(R.id.transcription)).setText("Recording...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (isRecording) {
            // stop recording
            isRecording = false;
            speechRecognizer.stopVoiceStreaming();
            ((ImageView) findViewById(R.id.recordButton)).setImageResource(R.drawable.record_button);
            ((TextView) findViewById(R.id.transcription)).setText("Stopped");
        }
    }
}


