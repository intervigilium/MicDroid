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

import java.io.IOException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class Recorder {
	private AudioRecordWrapper audioRecord;
	private MicWriter writerThread;
	private Handler errorHandler;
	private WaveWriter writer;

	public Recorder(Context context, Handler errorHandler, int bufferSize) {
		this.errorHandler = errorHandler;
		this.writer = new WaveWriter(
				ApplicationHelper.getOutputDirectory(),
				context.getString(R.string.default_recording_name), 
				PreferenceHelper.getSampleRate(context), 
				AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG), 
				AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));
		this.audioRecord = new AudioRecordWrapper(context, errorHandler, bufferSize);
		this.writerThread = new MicWriter();
	}
	
	public void start() {
		try {
			writer.createWaveFile();
		} catch (IOException e) {
			// problem writing to file, unable to create file?
			e.printStackTrace();
			
			Message msg = errorHandler.obtainMessage(Constants.UNABLE_TO_CREATE_RECORDING);
			errorHandler.sendMessage(msg);
		}
		audioRecord.start();
		writerThread.start();
	}
	
	public void stop() {
		if (isRunning()) {
			audioRecord.stop();
			writerThread.interrupt();
			try {
				writerThread.join();
			} catch (InterruptedException e) {
				// don't do anything?
			}
			
			try {
				writer.closeWaveFile();
				writer = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writerThread = null;
		}
	}
	
	public void cleanup() {
		stop();
		audioRecord.cleanup();
	}
	
	public boolean isRunning() {
		return (writerThread != null && writerThread.getState() != Thread.State.TERMINATED);
	}
	
	private class MicWriter extends Thread {

		public void run() {
			while (!Thread.interrupted()) {
				Sample sample = audioRecord.poll();
				if (sample != null) {
					try {
						writer.write(sample.buffer, sample.bufferSize);
					} catch (IOException e) {
						// problem writing to the buffer, usually means we're out of space
						e.printStackTrace();
						
						Message msg = errorHandler.obtainMessage(Constants.WRITER_OUT_OF_SPACE);
						errorHandler.sendMessage(msg);
					}
				}
				else {
					// log it?
				}
			}
		}
    }
}
