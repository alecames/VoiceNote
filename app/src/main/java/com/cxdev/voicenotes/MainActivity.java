package com.cxdev.voicenotes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private SpeechRecognizer speechRecognizer;
    private TextView transcription, state, timer;
    private boolean isRecording = false;
    NotesDBH db = new NotesDBH(MainActivity.this);

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("transcriptionText", transcription.getText().toString());
        outState.putString("stateText", state.getText().toString());
        outState.putString("timerText", timer.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transcription = findViewById(R.id.transcription);
        state = findViewById(R.id.state);
        timer = findViewById(R.id.timer);
        clear();
        transcription.setMovementMethod(new ScrollingMovementMethod());

        if (savedInstanceState != null) {
            transcription.setText(savedInstanceState.getString("transcriptionText"));
            state.setText(savedInstanceState.getString("stateText"));
            timer.setText(savedInstanceState.getString("timerText"));
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100000);


        // listener for history button
        findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        // listener for clear button
        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        // listener for save button
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                }
                state.setText("Saving...");
                showTitleDialog(MainActivity.this);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                transcription.setText("");
            }

            @Override
            public void onBeginningOfSpeech() {
                state.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {}

            @Override
            public void onBufferReceived(byte[] bytes) {}

            @Override
            public void onEndOfSpeech() {
                state.setHint("Waiting...");
            }

            @Override
            public void onError(int i) {
            }

            @Override
            public void onResults(Bundle results) {
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                // Get the recognition results
                List<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                // Append the results to the TextView
                for (String result : data) {
                    // clear leading and trailing whitespace
                    result = result.trim();
                    // make the first letter uppercase
                    result = result.substring(0, 1).toUpperCase() + result.substring(1);
                    if (!result.isEmpty()) {
                        transcription.append(result);
                        transcription.append("\n");
                    }
                }
            }
            @Override
            public void onEvent(int i, Bundle bundle) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void clear() {
        stopRecording();
        stopTimer();
        TextView transcription = findViewById(R.id.transcription);
        TextView state = findViewById(R.id.state);
        transcription.setText("Transcription will appear here...");
        state.setText("Press the button to start recording");
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

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();

                // get text from textview
                TextView textView = findViewById(R.id.transcription);
                String noteContent = textView.getText().toString();
                if (!title.isEmpty()) {
                    title = "Untitled " + db.getNotesCount();
                }
                db.addNote( new Note(db.getNotesCount(),
                        title,
                        noteContent,
                        Instant.now().toString()));
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
                dialog.cancel();
            }
        });

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
                transcription.setText("");
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
            state.setText("Stopped");
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            ((ImageView) findViewById(R.id.recordButton)).setImageResource(R.drawable.record_button);
        }
    }
}


