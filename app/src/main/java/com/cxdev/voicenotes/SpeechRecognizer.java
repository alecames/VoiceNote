package com.cxdev.voicenotes;
import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import com.cxdev.voicenotes.MainActivity;

public class SpeechRecognizer {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private final AudioRecord audioRecord;
    private final SpeechClient speechClient;
    private final Context context;

    public SpeechRecognizer(Activity activity, Context context) throws IOException {
        this.context = context;

        // Check if the RECORD_AUDIO permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // If the permission is not granted, request it
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        // Initialize the AudioRecord instance
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );

        // Initialize the SpeechClient instance
        speechClient = SpeechClient.create();
    }

    public String recognizeSpeech() throws IOException {
        // Start recording
        audioRecord.startRecording();

        // Create a new RecognitionAudio instance
        RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(readAudio()))
                .build();

        // Create a new RecognitionConfig instance
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(AudioEncoding.LINEAR16)
                .setSampleRateHertz(SAMPLE_RATE)
                .setLanguageCode("en-US")
                .build();

        // Recognize the audio
        RecognizeResponse recognizeResponse = speechClient.recognize(recognitionConfig, recognitionAudio);

        // Get the first result
        SpeechRecognitionResult result = recognizeResponse.getResultsList().get(0);

        // Get the first alternative
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);

        // Return the transcript of the audio
        return alternative.getTranscript();
    }

    private byte[] readAudio() {
        // Create a buffer with the BUFFER_SIZE
        byte[] buffer = new byte[BUFFER_SIZE];

        // Read the audio into the buffer
        audioRecord.read(buffer, 0, buffer.length);

        // Return the buffer
        return buffer;
    }

    public void close() {
        // Stop recording
        audioRecord.stop();

        // Close the SpeechClient
        speechClient.close();
    }
}