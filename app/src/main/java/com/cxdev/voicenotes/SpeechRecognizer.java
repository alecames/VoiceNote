package com.cxdev.voicenotes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeechRecognizer {
    private AudioRecord voiceRecorder;
    private MicListener micListener;
    private final ExecutorService streamExecutorService = Executors.newFixedThreadPool(1);
    private boolean isStreaming = false;

    public SpeechRecognizer() {
    }

    public void registerOnVoiceListener(MicListener voiceStreamListener) {
        this.micListener = voiceStreamListener;
    }

    private final Thread runnableAudioStream = new Thread() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            try {
                short[] buffer = new short[minBufferSize];
                if (voiceRecorder == null) {
                    voiceRecorder = new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            minBufferSize * 10
                    );
                }
                voiceRecorder.startRecording();
                while (isStreaming) {
                    minBufferSize = voiceRecorder.read(buffer, 0, buffer.length);
                    if (micListener != null) {
                        micListener.onVoiceStreaming(buffer);
                    }
//                    Log.i("MinBufferSize : ", String.valueOf(buffer.length));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public int getSampleRate() {
        return voiceRecorder != null ? voiceRecorder.getSampleRate() : 0;
    }

    public void stopVoiceStreaming() {
        isStreaming = false;
        if (voiceRecorder != null) {
            voiceRecorder.release();
            voiceRecorder = null;
        }
        if (runnableAudioStream.isAlive()) {
            streamExecutorService.shutdown();
        }
    }

    void startVoiceStreaming() {
        isStreaming = true;
        streamExecutorService.submit(runnableAudioStream);
    }


    private static final int sampleRate = 44100;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static int minBufferSize = 2200;

    public static void streamingRecognizeFile(String fileName) throws Exception, IOException {
        Path path = Paths.get(fileName);
        byte[] data = Files.readAllBytes(path);

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Configure request with local raw PCM audio
            RecognitionConfig recConfig =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(16000)
                            .setModel("default")
                            .build();
            StreamingRecognitionConfig config =
                    StreamingRecognitionConfig.newBuilder().setConfig(recConfig).build();

            class ResponseApiStreamingObserver<T> implements ApiStreamObserver<T> {
                private final SettableFuture<List<T>> future = SettableFuture.create();
                private final List<T> messages = new java.util.ArrayList<T>();

                @Override
                public void onNext(T message) {
                    messages.add(message);
                }

                @Override
                public void onError(Throwable t) {
                    future.setException(t);
                }

                @Override
                public void onCompleted() {
                    future.set(messages);
                }

                // Returns the SettableFuture object to get received messages / exceptions.
                public SettableFuture<List<T>> future() {
                    return future;
                }
            }

            ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver =
                    new ResponseApiStreamingObserver<>();

            BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable =
                    speech.streamingRecognizeCallable();

            ApiStreamObserver<StreamingRecognizeRequest> requestObserver =
                    callable.bidiStreamingCall(responseObserver);

            // The first request must **only** contain the audio configuration:
            requestObserver.onNext(
                    StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build());

            // Subsequent requests must **only** contain the audio data.
            requestObserver.onNext(
                    StreamingRecognizeRequest.newBuilder()
                            .setAudioContent(ByteString.copyFrom(data))
                            .build());

            // Mark transmission as completed after sending the data.
            requestObserver.onCompleted();

            List<StreamingRecognizeResponse> responses = responseObserver.future().get();

            for (StreamingRecognizeResponse response : responses) {
                // For streaming recognize, the results list has one is_final result (if available) followed
                // by a number of in-progress results (if iterim_results is true) for subsequent utterances.
                // Just print the first result here.
                StreamingRecognitionResult result = response.getResultsList().get(0);
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcript : %s\n", alternative.getTranscript());
            }
        }
    }


}
