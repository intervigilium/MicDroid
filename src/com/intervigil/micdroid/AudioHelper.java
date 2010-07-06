package com.intervigil.micdroid;

import android.media.AudioFormat;

public class AudioHelper {

	public static int convertChannelConfig(int numChannels) {
		switch (numChannels) {
			case 1:
				return AudioFormat.CHANNEL_CONFIGURATION_MONO;
			case 2:
				return AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			default:
				return AudioFormat.CHANNEL_CONFIGURATION_DEFAULT;
		}
	}
	
	public static int convertPcmEncoding(int bitsPerSample) {
		switch (bitsPerSample) {
			case 8:
				return AudioFormat.ENCODING_PCM_8BIT;
			case 16:
				return AudioFormat.ENCODING_PCM_16BIT;
			default:
				return AudioFormat.ENCODING_DEFAULT;
		}
	}
}
