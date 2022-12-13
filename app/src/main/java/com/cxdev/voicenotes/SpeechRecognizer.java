package com.cxdev.voicenotes;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.Manifest;
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
import com.google.cloud.speech.v1.RecognitionConfig.Builder;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;

public class SpeechRecognizer {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private final AudioRecord audioRecord;
    private final SpeechClient speechClient;

    public SpeechRecognizer(Context context) throws IOException {

        // TODO check if permission is granted
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        speechClient = SpeechClient.create();
    }

    public String recognizeSpeech() throws Exception {
        // Start recording audio
        audioRecord.startRecording();

        // Create a byte array to store the audio data
        byte[] audioBytes = new byte[BUFFER_SIZE];

        // Read the audio data into the byte array
        int numBytes = audioRecord.read(audioBytes, 0, BUFFER_SIZE);

        // Create a RecognitionAudio object from the audio data and the sample rate
        RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioBytes, 0, numBytes)).build();

        // Create a RecognitionConfig object with the appropriate language and encoding settings
        Builder recognitionConfigBuilder = RecognitionConfig.newBuilder()
                .setEncoding(AudioEncoding.LINEAR16)
                .setLanguageCode("en-US");

        RecognitionConfig recognitionConfig = recognitionConfigBuilder.build();

        // Use the SpeechClient to recognize the audio
        RecognizeResponse recognizeResponse = speechClient.recognize(recognitionConfig, recognitionAudio);

        // Get the list of results from the response
        List<SpeechRecognitionResult> results = recognizeResponse.getResultsList();

        // Check if the response is empty
        if (results.isEmpty()) {
            throw new Exception("No speech was detected. Please try again.");
        }

        // Get the first result from the list
        SpeechRecognitionResult result = results.get(0);

        // Get the list of alternatives from the result
        List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();

        // Check if the list of alternatives is empty
        if (alternatives.isEmpty()) {
            throw new Exception("No recognition result found.");
        }

        // Return the first alternative
        return alternatives.get(0).getTranscript();
    }

    public void close() {
        audioRecord.stop();
        audioRecord.release();
        speechClient.close();
    }
}


