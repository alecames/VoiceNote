package com.cxdev.voicenotes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

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
                    Log.i("MinBufferSize : ", String.valueOf(buffer.length));
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
}
