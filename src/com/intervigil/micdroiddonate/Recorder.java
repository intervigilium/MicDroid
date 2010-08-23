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

package com.intervigil.micdroiddonate;

import java.io.IOException;

import android.content.Context;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;

import com.intervigil.micdroiddonate.helper.ApplicationHelper;
import com.intervigil.micdroiddonate.helper.AudioHelper;
import com.intervigil.micdroiddonate.helper.PreferenceHelper;
import com.intervigil.micdroiddonate.model.Sample;
import com.intervigil.micdroiddonate.pitch.AutoTalent;
import com.intervigil.micdroiddonate.wave.WaveWriter;

public class Recorder {
	private MicWriter writerThread;
	
	private AudioTrack audioTrack;
	private final AudioRecordWrapper audioRecord;
	private final Handler errorHandler;
	private final WaveWriter writer;
	private final boolean isLiveMode;

	public Recorder(Context context, Handler errorHandler, boolean isLiveMode) {
		this.errorHandler = errorHandler;
		this.isLiveMode = isLiveMode;
		this.writer = new WaveWriter(
				ApplicationHelper.getOutputDirectory(),
				context.getString(R.string.default_recording_name), 
				PreferenceHelper.getSampleRate(context), 
				AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG), 
				AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));
		this.audioRecord = new AudioRecordWrapper(context, errorHandler);
		
		if (isLiveMode) {
			try {
				this.audioTrack = AudioHelper.getPlayer(context);
			} catch (IllegalArgumentException e) {
				// problem with audiotrack being given a bad sample rate/buffer size
				e.printStackTrace();
				
				Message msg = errorHandler.obtainMessage(Constants.AUDIOTRACK_ILLEGAL_ARGUMENT);
				errorHandler.sendMessage(msg);
			}
		}
	}
	
	public void start() {
		writerThread = new MicWriter();
		try {
			writer.createWaveFile();
			audioRecord.start();
			writerThread.start();
			if (isLiveMode) {
				audioTrack.play();
			}
		} catch (IOException e) {
			// problem writing to file, unable to create file?
			e.printStackTrace();
			
			Message msg = errorHandler.obtainMessage(Constants.UNABLE_TO_CREATE_RECORDING);
			errorHandler.sendMessage(msg);
		} catch (IllegalStateException e) {
			// problem starting playback from audiotrack
			e.printStackTrace();
			
			Message msg = errorHandler.obtainMessage(Constants.AUDIOTRACK_ILLEGAL_STATE);
			errorHandler.sendMessage(msg);
		}
	}
	
	public void stop() {
		if (isRunning()) {
			if (isLiveMode) {
				audioTrack.stop();
			}
			audioRecord.stop();
			writerThread.interrupt();
			try {
				writerThread.join();
			} catch (InterruptedException e) { }
			
			try {
				writer.closeWaveFile();
			} catch (IOException e) {
				e.printStackTrace();
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
		return (writerThread != null &&
				writerThread.getState() != Thread.State.NEW &&
				writerThread.getState() != Thread.State.TERMINATED);
	}
	
	private class MicWriter extends Thread {

		public void run() {
			while (!Thread.interrupted()) {
				Sample sample = audioRecord.poll();
				if (sample != null) {
					if (isLiveMode) {
						AutoTalent.processSamples(sample.buffer, sample.bufferSize);
						audioTrack.write(sample.buffer, 0, sample.bufferSize);
					}
					try {
						writer.write(sample.buffer, sample.bufferSize);
					} catch (IOException e) {
						// problem writing to the buffer, usually means we're out of space
						e.printStackTrace();
						
						Message msg = errorHandler.obtainMessage(Constants.WRITER_OUT_OF_SPACE);
						errorHandler.sendMessage(msg);
					}
				}
			}
		}
    }
}
