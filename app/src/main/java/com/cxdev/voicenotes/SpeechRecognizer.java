package com.cxdev.voicenotes;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeechRecognizer {
    private AudioRecord voiceRecorder;
    private MicListener micListener;
    private final ExecutorService streamExecutorService = Executors.newFixedThreadPool(1);
    private boolean isStreaming = false;
    public long startTime = 0;
    public long endTime = 0;

    private static final int sampleRate = 22000;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static int minBufferSize = 2200;

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
        endTime = Instant.now().toEpochMilli();
        if (voiceRecorder != null) {
            voiceRecorder.release();
            voiceRecorder = null;
        }
        if (runnableAudioStream.isAlive()) {
            streamExecutorService.shutdown();
        }
    }

    void startVoiceStreaming() {
        startTime = Instant.now().toEpochMilli();
        isStreaming = true;
        streamExecutorService.submit(runnableAudioStream);
    }

    public static void streamingRecognizeFile(InputStream audioStream) throws Exception {
        // create a new SpeechClient
        try (SpeechClient speech = SpeechClient.create()){
            // create a new streaming recognize request
            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    // set the recognition config
                    .setStreamingConfig(StreamingRecognitionConfig.newBuilder()
                            .setConfig(RecognitionConfig.newBuilder()
                                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                                    .setSampleRateHertz(sampleRate)
                                    .setLanguageCode("en-US")
                                    .build())
                            .setInterimResults(true)
                            .build())
                    .build();

            // create a new BidiStreamingCallable for the SpeechClient
            BidiStreamingCallable<StreamingRecognizeRequest, StreamingRecognizeResponse> callable =
                    speech.streamingRecognizeCallable();

            // create an ApiStreamObserver to handle the responses from the server
            ApiStreamObserver<StreamingRecognizeResponse> responseObserver =
                    new ApiStreamObserver<StreamingRecognizeResponse>() {
                        @Override
                        public void onNext(StreamingRecognizeResponse response) {
                            // get the recognition results from the response
                            List<StreamingRecognitionResult> results = response.getResultsList();

                            // check if the response contains any recognition results
                            if (!results.isEmpty()) {
                                // get the first recognition result from the list
                                StreamingRecognitionResult result = results.get(0);

                                // check if the recognition result contains any alternatives
                                if (!result.getAlternativesList().isEmpty()) {
                                    // get the first alternative from the list
                                    SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                                    // print the alternative transcript to the console
                                    System.out.printf("Transcript: %s\n", alternative.getTranscript());
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.out.println("Error: " + t.getMessage());
                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("Completed");
                        }
                    };

            // create a SettableFuture to handle the request
            SettableFuture<Void> future = SettableFuture.create();

            // send the requests to the server
            ApiStreamObserver<StreamingRecognizeRequest> requestObserver =
                    callable.bidiStreamingCall(responseObserver);

            // read the audio data from the input stream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = audioStream.read(buffer)) != -1) {
                // create a new chunk of audio data
                ByteString audioData = ByteString.copyFrom(buffer, 0, len);

                // add the chunk of audio data to the request
                requestObserver.onNext(
                        StreamingRecognizeRequest.newBuilder()
                                .setAudioContent(audioData)
                                .build());
            }

            // indicate that the request is complete
            requestObserver.onCompleted();

            // wait for the response to complete
            future.get();

        }
    }

}
