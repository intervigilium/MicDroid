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

import android.content.Context;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.micdroid.model.Sample;
import com.intervigil.micdroid.pitch.AutoTalent;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

public class PdRecorder implements Recorder {
    private MicWriter writerThread;
    private AudioTrack audioTrack;
    private WaveReader instrumentalReader;
    private AudioRecordWrapper audioRecord;
    private final Handler errorHandler;
    private final WaveWriter writer;
    private final boolean isLiveMode;

    public PdRecorder(Context context, Handler errorHandler, boolean isLiveMode)
            throws IllegalArgumentException {
        this.errorHandler = errorHandler;
        this.isLiveMode = isLiveMode;
        this.writer = new WaveWriter(
                context.getCacheDir().getAbsolutePath(),
                context.getString(R.string.default_recording_name),
                PreferenceHelper.getSampleRate(context),
                AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG),
                AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));
        this.audioRecord = new AudioRecordWrapper(context);

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
            instrumentalReader = new WaveReader(instrumentalFile);
        }
    }

    public void start()
            throws IllegalStateException, FileNotFoundException, IOException {
        writerThread = new MicWriter();
        writer.createWaveFile();
        writerThread.start();
        if (instrumentalReader != null) {
            instrumentalReader.openWave();
        }
        if (isLiveMode) {
            audioTrack.play();
        }
        audioRecord.start();
    }

    public void stop() {
        if (isRunning()) {
            audioRecord.stop();
            if (isLiveMode) {
                audioTrack.stop();
            }
            if (instrumentalReader != null) {
                try {
                    instrumentalReader.closeWaveFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            writerThread.interrupt();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
            }

            writerThread = null;
        }
    }

    public void cleanup() {
        stop();
        audioRecord.cleanup();
        if (isLiveMode) {
            audioTrack.release();
        }
    }

    public boolean isRunning() {
        return (writerThread != null
                && writerThread.getState() != Thread.State.NEW
                && writerThread.getState() != Thread.State.TERMINATED);
    }

    private class MicWriter extends Thread {
        public void run() {
            while (!Thread.interrupted()) {
                Sample sample = audioRecord.poll();
                if (sample != null) {
                    try {
                        if (isLiveMode) {
                            if (instrumentalReader != null) {
                                short[] instrumentalBuf = new short[sample.bufferSize];
                                instrumentalReader.read(instrumentalBuf,
                                        sample.bufferSize);
                                AutoTalent.processMixSamples(sample.buffer,
                                        instrumentalBuf, sample.bufferSize);
                            } else {
                                AutoTalent.processSamples(sample.buffer,
                                        sample.bufferSize);
                            }
                            audioTrack.write(sample.buffer, 0,
                                    sample.bufferSize);
                        }
                        writer.write(sample.buffer, sample.bufferSize);
                    } catch (IOException e) {
                        // problem writing to the buffer, usually means we're
                        // out of space
                        e.printStackTrace();

                        Message msg = errorHandler
                                .obtainMessage(Constants.WRITER_OUT_OF_SPACE);
                        errorHandler.sendMessage(msg);
                    }
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
