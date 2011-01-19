/* Mic.java
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

package com.intervigil.micdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.sourceforge.autotalent.Autotalent;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

public class SipdroidRecorder implements Recorder {
    private final Context context;
    private MicWriter writerThread;
    private AudioTrack audioTrack;
    private WaveReader instrumentalReader;
    private final AudioRecord audioRecord;
    private final Handler errorHandler;
    private final WaveWriter writer;
    private final boolean isLiveMode;
    private final int sampleRate;

    public SipdroidRecorder(Context context, Handler errorHandler,
            boolean isLiveMode) throws IllegalArgumentException {
        this.context = context;
        this.sampleRate = PreferenceHelper.getSampleRate(context);
        this.errorHandler = errorHandler;
        this.isLiveMode = isLiveMode;
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
            File instrumentalFile = new File(
                    ApplicationHelper.getInstrumentalDirectory()
                    + File.separator
                    + trackName);
            this.instrumentalReader = new WaveReader(instrumentalFile);
        }
    }

    public void start() throws IllegalStateException, FileNotFoundException,
            IOException {
        // create and open necessary files
        if (instrumentalReader != null) {
            instrumentalReader.openWave();
        }
        writer.createWaveFile();
        if (isLiveMode) {
            AudioManager am = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            am.setMode(AudioManager.MODE_NORMAL);
        }
        writerThread = new MicWriter();
        writerThread.start();
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
        audioRecord.release();
        if (isLiveMode) {
            audioTrack.release();
        }
    }

    public boolean isRunning() {
        return (writerThread != null
                && writerThread.getState() != Thread.State.NEW && writerThread
                .getState() != Thread.State.TERMINATED);
    }

    private class MicWriter extends Thread {
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
                Log.e("SipRecorder", e.toString());
            }
        }

        public void close() {
            running = false;
        }

        public void run() {
            int num;
            long now, nextFrameDelay, lastFrameTime = 0;
            short[] buf = new short[bufSize];

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
                try {
                    if (isLiveMode) {
                        if (instrumentalReader != null) {
                            short[] instrumentalBuf = new short[num];
                            instrumentalReader.read(instrumentalBuf,
                                    frameSize);
                            Autotalent.processMixSamples(buf,
                                    instrumentalBuf, num);
                        } else {
                            Autotalent.processSamples(buf, num);
                        }
                        audioTrack.write(buf, 0, num);
                    }
                    writer.write(buf, 0, num);
                } catch (IOException e) {
                    // problem writing to the buffer, usually means we're
                    // out of space
                    e.printStackTrace();

                    Message msg = errorHandler
                            .obtainMessage(Constants.WRITER_OUT_OF_SPACE);
                    errorHandler.sendMessage(msg);
                }
            }
            // stop things
            audioRecord.stop();
            if (isLiveMode) {
                audioTrack.stop();
            }
            if (instrumentalReader != null) {
                try {
                    instrumentalReader.closeWaveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // close file
            try {
                writer.closeWaveFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
