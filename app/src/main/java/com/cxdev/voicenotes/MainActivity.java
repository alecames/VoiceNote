package com.cxdev.voicenotes;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.vosk.android.SpeechService;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    NotesDBH db = new NotesDBH(MainActivity.this);
    private static final int REQUEST_CODE = 1;
    private SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView transcription = findViewById(R.id.transcription);

        // check for mic permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // check for storage permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

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
                if (!isRecording) {
                    startRecording(speechRecognizerIntent);
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

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                transcription.setText("");
                transcription.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                transcription.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }
    
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE) {
//            if (resultCode == RESULT_OK && data != null) {
//                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                ((TextView) findViewById(R.id.transcription)).setText(Objects.requireNonNull(result).get(0));
//            }
//        }
//    }

    private void cancel() {
        stopRecording();
        stopTimer();
        ((TextView) findViewById(R.id.timer)).setText("00:00");
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

    // start recording
    private void startRecording(Intent speechRecognizerIntent) {
        if (!isRecording) {
            try {
                startTimer();
                isRecording = true;
                speechRecognizer.startListening(speechRecognizerIntent);
                ((ImageView) findViewById(R.id.recordButton)).setImageResource(R.drawable.record_button_recording);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // stop recording
    private void stopRecording() {
        if (isRecording) {
            stopTimer();
            isRecording = false;
            speechRecognizer.stopListening();
            ((ImageView) findViewById(R.id.recordButton)).setImageResource(R.drawable.record_button);
        }
    }
}


