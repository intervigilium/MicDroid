/* SipdroidRecorder.java
   An auto-tune app for Android

   Copyright (c) 2010 Ethan Chen

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.intervigil.micdroid.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.autotalent.Autotalent;
import net.sourceforge.resample.Resample;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.R;
import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.interfaces.DependentTask;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;
import com.intervigil.wave.exception.InvalidWaveException;

public class SipdroidRecorder implements Recorder {

    private static final String CLASS_SIPDROID_RECORDER = "SipdroidRecorder";

    private static final int RECORDER_MESSAGE_INVALID_INSTRUMENTAL = 8675309;
    private static final int RECORDER_MESSAGE_IO_ERROR = 8675308;
    private static final int RECORDER_MESSAGE_RECORD_ERROR = 8675310;
    private static final int RECORDER_MESSAGE_FINISHED = 8675307;

    private final Context context;
    private MicWriter writerThread;
    private final boolean isLiveMode;
    private final int sampleRate;
    private DependentTask postRecordTask;

    public SipdroidRecorder(Context context, DependentTask postRecordTask, boolean isLiveMode) {
        this.context = context;
        this.sampleRate = PreferenceHelper.getSampleRate(context);
        this.postRecordTask = postRecordTask;
        this.isLiveMode = isLiveMode;
    }

    public void start() {
        try {
            writerThread = new MicWriter();
            writerThread.start();
            Toast.makeText(context,
                    R.string.recording_started_toast,
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            DialogHelper.showWarning(context,
                    R.string.audio_record_exception_title,
                    R.string.audio_record_exception_warning);
            postRecordTask.handleError();
        }
    }

    public void stop() {
        if (isRunning()) {
            writerThread.close();
            try {
                writerThread.join();
            } catch (InterruptedException e) {}
            writerThread = null;
        }
    }

    public void cleanup() {
        stop();
    }

    public boolean isRunning() {
        return (writerThread != null
                && writerThread.getState() != Thread.State.NEW && writerThread
                .getState() != Thread.State.TERMINATED);
    }

    private Handler recorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORDER_MESSAGE_RECORD_ERROR:
                    DialogHelper.showWarning(context,
                            R.string.audio_record_exception_title,
                            R.string.audio_record_exception_warning);
                    postRecordTask.handleError();
                    break;
                case RECORDER_MESSAGE_INVALID_INSTRUMENTAL:
                    DialogHelper.showWarning(context,
                            R.string.instrumental_not_found_title,
                            R.string.instrumental_not_found_warning);
                    postRecordTask.handleError();
                    break;
                case RECORDER_MESSAGE_IO_ERROR:
                    DialogHelper.showWarning(context,
                            R.string.recording_io_error_title,
                            R.string.recording_io_error_warning);
                    postRecordTask.handleError();
                    break;
                case RECORDER_MESSAGE_FINISHED:
                    postRecordTask.doTask();
                    break;
            }
        }
    };

    private class MicWriter extends Thread {
        private final AudioRecord audioRecord;
        private AudioTrack audioTrack;
        private final WaveWriter writer;
        private WaveReader instrumentalReader;
        private final int frameSize;
        private final int frameRate;
        private final long framePeriod;
        private final int bufSize;
        private boolean running;

        public MicWriter() {
            this.frameSize = 160;
            this.framePeriod = 1000 / (sampleRate / frameSize);
            this.frameRate = (int) (sampleRate / frameSize * 1.5);
            this.bufSize = frameSize * (frameRate + 1);
            this.running = false;
            this.audioRecord = AudioHelper.getRecorder(context);
            this.writer = new WaveWriter(context.getCacheDir().getAbsolutePath(),
                    context.getString(R.string.default_recording_name), sampleRate,
                    AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG),
                    AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));

            if (isLiveMode) {
                this.audioTrack = AudioHelper.getPlayer(context);
            }

            String trackName = PreferenceHelper.getInstrumentalTrack(context);
            if (!trackName.equals(Constants.EMPTY_STRING)) {
                // start reading from instrumental track
                this.instrumentalReader = new WaveReader(new File(trackName));
            }
        }

        // weird little hack; eliminates the nasty click when AudioTrack
        // (dis)engages by playing
        // a few milliseconds of silence before starting AudioTrack
        // This quirky hack taken from PdCore of pd-for-android project
        private void avoidClickHack(Context context) {
            try {
                MediaPlayer mp = MediaPlayer.create(context, R.raw.silence);
                mp.start();
                Thread.sleep(10);
                mp.stop();
                mp.release();
            } catch (Exception e) {
                Log.e(CLASS_SIPDROID_RECORDER, e.toString());
            }
        }

        public synchronized void close() {
            running = false;
        }

        public void initialize() throws FileNotFoundException, InvalidWaveException, IOException {
            if (instrumentalReader != null) {
                instrumentalReader.openWave();
                Resample.initialize(instrumentalReader.getSampleRate(), sampleRate, Resample.DEFAULT_BUFFER_SIZE, instrumentalReader.getChannels());
            }
            writer.createWaveFile();
            if (isLiveMode) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setMode(AudioManager.MODE_NORMAL);
            }
        }

        public void cleanup() {
            // stop things
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            audioRecord.release();
            if (isLiveMode) {
                audioTrack.stop();
                audioTrack.release();
            }
            if (instrumentalReader != null) {
                try {
                    Resample.close();
                    instrumentalReader.closeWaveFile();
                } catch (IOException e) {
                    // no recovery possible here
                    e.printStackTrace();
                }
            }
            // close file
            try {
                writer.closeWaveFile();
            } catch (IOException e) {
                // no recovery possible here
                e.printStackTrace();
            }
        }

        public void run() {
            Message msg;
            int num;
            long now, nextFrameDelay, lastFrameTime = 0;
            short[] buf = new short[bufSize];

            try {
                initialize();
                running = true;
                avoidClickHack(context);
                audioRecord.startRecording();
                if (isLiveMode) {
                    audioTrack.play();
                }
                while (running) {
                    // delay reading if it's not time for the next frame
                    now = System.currentTimeMillis();
                    nextFrameDelay = framePeriod - (now - lastFrameTime);
                    lastFrameTime = now;
                    if (nextFrameDelay > 0) {
                        try {
                            sleep(nextFrameDelay);
                        } catch (InterruptedException e) {}
                        lastFrameTime = lastFrameTime + nextFrameDelay;
                    }
                    num = audioRecord.read(buf, 0, frameSize);
                    if (isLiveMode) {
                        processLiveAudio(buf, num);
                        audioTrack.write(buf, 0, num);
                    }
                    writer.write(buf, 0, num);
                }
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_FINISHED);
            } catch (IllegalStateException e) {
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_RECORD_ERROR);
            } catch (FileNotFoundException e) {
                // couldn't find instrumental file
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_INVALID_INSTRUMENTAL);
            } catch (InvalidWaveException e) {
                // not a wave file
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_INVALID_INSTRUMENTAL);
            } catch (IOException e) {
                // file IO error, no recovery possible?
                e.printStackTrace();
                msg = recorderHandler.obtainMessage(RECORDER_MESSAGE_IO_ERROR);
            }
            cleanup();
            recorderHandler.sendMessage(msg);
        }

        private void processLiveAudio(short[] samples, int numSamples) throws IOException {
            if (instrumentalReader != null) {
                int read;
                int bufferSize = (int) (numSamples / Resample.getFactor());
                short[] instrumental = new short[bufferSize];

                if (instrumentalReader.getChannels() == 1) {
                    read = instrumentalReader.read(instrumental, bufferSize);
                } else {
                    short[] instrRight = new short[bufferSize];
                    read = instrumentalReader.read(instrumental, instrRight, bufferSize);
                    Resample.downmix(instrumental, instrumental, instrRight, read);
                }
                Resample.process(instrumental, instrumental, Resample.CHANNEL_MONO, read != bufferSize);
                Autotalent.processSamples(samples, instrumental, numSamples);
            } else {
                Autotalent.processSamples(samples, numSamples);
            }
        }
    }
}
