/* AudioHelper.java

   Copyright (c) 2010 Ethan Chen

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
 */

package com.intervigil.micdroid;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class AudioHelper {
	
	/**
     * Convert Android AudioFormat.CHANNEL_CONFIGURATION constants to integers
     * 
     * @param		androidChannels			Android AudioFormat constant
     */
	public static int getChannelConfig(int androidChannels) {
		switch (androidChannels) {
			case AudioFormat.CHANNEL_CONFIGURATION_MONO:
				return 1;
			case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
				return 2;
			default:
				return 1;
		}
	}
	
	/**
     * Convert Android AudioFormat.ENCODING_PCM constants to integers
     * 
     * @param		androidEncoding			Android AudioFormat constant
     */
	public static int getPcmEncoding(int androidEncoding) {
		switch (androidEncoding) {
			case AudioFormat.ENCODING_PCM_8BIT:
				return 8;
			case AudioFormat.ENCODING_PCM_16BIT:
				return 16;
			default:
				return 8;
		}
	}

	/**
     * Convert integers to AudioFormat.CHANNEL_CONFIGURATION constants
     * 
     * @param		numChannels			number of channels, typically 1 or 2
     */
	public static int getAndroidChannelConfig(int numChannels) {
		switch (numChannels) {
			case 1:
				return AudioFormat.CHANNEL_CONFIGURATION_MONO;
			case 2:
				return AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			default:
				return AudioFormat.CHANNEL_CONFIGURATION_DEFAULT;
		}
	}
	
	/**
     * Convert integers to AudioFormat.ENCODING_PCM constants
     * 
     * @param		bitsPerSample			bits in a sample of audio, typically 8 or 16
     */
	public static int getAndroidPcmEncoding(int bitsPerSample) {
		switch (bitsPerSample) {
			case 8:
				return AudioFormat.ENCODING_PCM_8BIT;
			case 16:
				return AudioFormat.ENCODING_PCM_16BIT;
			default:
				return AudioFormat.ENCODING_DEFAULT;
		}
	}
	
	/**
     * Gets the validity of the current recorder settings, particularly sample rate;
     * This function wraps getRecorderBufferSize(Context)
     * 
     * @param		context				Context which we are getting recorder information about
     */
	public static boolean isValidRecorderConfiguration(Context context) {
		return getRecorderBufferSize(context) != AudioRecord.ERROR_BAD_VALUE;
	}
	
	/**
     * Gets the buffer size needed for the current recorder settings
     * This function wraps AudioRecord.getMinBufferSize(SampleRate, ChannelConfig, PcmFormat)
     * which means this function can return AudioRecord.ERROR_BAD_VALUE for invalid settings
     * or AudioRecord.ERROR when the system is unable to query hardware for proper settings
     * 
     * @param		context				Context which we are getting recorder information about
     */
	public static int getRecorderBufferSize(Context context) {
		int bufferSize = 0;
		int sampleRate = PreferenceHelper.getSampleRate(context);
		
		if (sampleRate > 0) {
			bufferSize = AudioRecord.getMinBufferSize(sampleRate, 
					Constants.DEFAULT_CHANNEL_CONFIG, 
					Constants.DEFAULT_PCM_FORMAT);
		}
		return bufferSize;
	}
	
	/**
     * Gets an AudioRecord object using the current recording settings
     * 
     * @param		context				Context which we are getting recorder for
     */
	public static AudioRecord getRecorder(Context context) throws IllegalArgumentException {
		AudioRecord recorder = null;
		int bufferSize = 0;
		int sampleRate = PreferenceHelper.getSampleRate(context);

		Log.i("AudioHelper", String.format("AudioRecord initialized with saved configuration! sample rate: %d", sampleRate));
		
		bufferSize = AudioRecord.getMinBufferSize(sampleRate, 
				Constants.DEFAULT_CHANNEL_CONFIG, 
				Constants.DEFAULT_PCM_FORMAT);

		recorder = new AudioRecord(AudioSource.MIC,
				sampleRate, 
				Constants.DEFAULT_CHANNEL_CONFIG,
				Constants.DEFAULT_PCM_FORMAT,
				bufferSize);
		
		return recorder;
	}
	
	/**
     * Attempts to autoconfigure current Context's sample rate
     * Will show a pop-up warning if autoconfiguration failed to set sample rate
     * 
     * @param		context				Context which we are attempting to configure
     */
	public static void configureRecorder(Context context) {
		int bufferSize = 0;
		int sampleRate = PreferenceHelper.getSampleRate(context);
		
		if (sampleRate < 0) {
			// try a new sample rates until we find one that works  		
    		do {
    			switch (sampleRate) {
	    			case -1:
	    				// set the default to 22050Hz, so slower devices perform better
	    				sampleRate = Constants.SAMPLE_RATE_22KHZ;
	    				break;
					case Constants.SAMPLE_RATE_44KHZ:
						sampleRate = Constants.SAMPLE_RATE_22KHZ;
						break;
					case Constants.SAMPLE_RATE_22KHZ:
						sampleRate = Constants.SAMPLE_RATE_11KHZ;
						break;
					case Constants.SAMPLE_RATE_11KHZ:
						sampleRate = Constants.SAMPLE_RATE_8KHZ;
						break;
    				default:
    					// show some kind of error pop-up
    					Log.w("AudioHelper", String.format("Hardware does not support recording!"));
    					DialogHelper.showWarning(context, R.string.unable_to_configure_audio_title, R.string.unable_to_configure_audio_warning);
    					return;
    			}
    			
    			bufferSize = AudioRecord.getMinBufferSize(
    					sampleRate, 
    					Constants.DEFAULT_CHANNEL_CONFIG, 
    					Constants.DEFAULT_PCM_FORMAT);
    			
    		} while (bufferSize == AudioRecord.ERROR_BAD_VALUE);
    		
    		// save the last known good sample rate
    		Log.i("AudioHelper", String.format("AudioRecord initially configured! sample rate: %d", sampleRate));
    		PreferenceHelper.setSampleRate(context, sampleRate);
		}
	}
}
