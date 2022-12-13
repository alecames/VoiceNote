package com.cxdev.voicenotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder;
    SpeechRecognizer speechRecognizer;
    AudioRecord audioRecord;
    boolean isRecording = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private boolean permissionToRecordGranted = false;

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    @Override
    protected void onStop() {
        super.onStop();
        speechRecognizer.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        // TODO: make this
        try {
            speechRecognizer = new SpeechRecognizer(this);
        } catch (IOException e) {
            e.printStackTrace();
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
                showTitleDialog(MainActivity.this);
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showTitleDialog(Context context) {
        // Create a new AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title of the dialog
        builder.setTitle("Enter a Title");

        // Create an EditText view to get user input
        final EditText input = new EditText(context);

        // Set the EditText view as the content of the dialog
        builder.setView(input);

        // Add a button to save the user's input
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the user's input from the EditText view
                String title = input.getText().toString();

                // Do something with the user's input
                // ...
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
                TextView transcription = findViewById(R.id.transcription);
                transcription.setText(speechRecognizer.recognizeSpeech());
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
            speechRecognizer.close();
            ((ImageView) findViewById(R.id.recordButton)).setImageAlpha(0xFF);
            ((TextView) findViewById(R.id.transcription)).setText("Stopped recording...");
        }
    }
}


