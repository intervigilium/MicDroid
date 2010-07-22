/* Constants.java

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

import android.media.AudioFormat;

public class Constants {
	
	// default recording parameters
	public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public static final int DEFAULT_PCM_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	
	// sample rates
	public static final int SAMPLE_RATE_44KHZ = 44100;
	public static final int SAMPLE_RATE_22KHZ = 22050;
	public static final int SAMPLE_RATE_11KHZ = 11025;
	public static final int SAMPLE_RATE_8KHZ = 8000;
	
	// recording names
	public static final String PLAY_DATA_RECORDING_NAME = "recordingName";
	
	// intent codes
	public static final int FILENAME_ENTRY_INTENT_CODE = 12768;
	public static final int PLAYER_INTENT_CODE = 12769;
	public static final int RESULT_FILE_DELETED = 12770;
	
	// intent extras
	public static final String NAME_ENTRY_INTENT_FILE_NAME = "FILENAME_RESULT";
	public static final String NAME_ENTRY_INTENT_ORIGINAL_FILENAME = "ORIGINAL_NAME";
	
	// shared preference keys
	public static final String KEY_SEEN_STARTUP_DIALOG = "prefs_seen_startup_dialog";
	
	// mime type
	public static final String AUDIO_WAVE = "audio/wav";
}
