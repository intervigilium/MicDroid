/* Constants.java

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

import android.media.AudioFormat;

public class Constants {
	
	// default recording parameters
	public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public static final int DEFAULT_PCM_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int DEFAULT_BUFFER_LIMIT = 32;
	
	// sample rates
	public static final int SAMPLE_RATE_44KHZ = 44100;
	public static final int SAMPLE_RATE_22KHZ = 22050;
	public static final int SAMPLE_RATE_11KHZ = 11025;
	public static final int SAMPLE_RATE_8KHZ = 8000;
	
	// intent codes
	public static final int FILENAME_ENTRY_INTENT_CODE = 12768;
	public static final int PLAYER_INTENT_CODE = 12769;
	public static final int PREFERENCE_INTENT_CODE = 12771;
	
	// result codes
	public static final int RESULT_FILE_DELETED = 12770;
	
	// intent extras
	public static final String PLAYER_INTENT_RECORDING = "PLAYER_RECORDING";
	public static final String NAME_ENTRY_INTENT_RECORDING = "NAME_ENTRY_RECORDING";
	public static final String NAME_ENTRY_INTENT_FILE_NAME = "NAME_ENTRY_FILENAME_RESULT";
	
	// shared preference keys
	public static final String KEY_SEEN_STARTUP_DIALOG = "prefs_seen_startup_dialog";
	public static final String KEY_MOVED_OLD_LIBRARY = "prefs_moved_old_library";
	
	// mime type
	public static final String AUDIO_WAVE = "audio/wav";
	
	// recording error handler codes
	public static final int AUDIORECORD_ILLEGAL_STATE = 4;
	public static final int AUDIORECORD_ILLEGAL_ARGUMENT = 5;
	public static final int WRITER_OUT_OF_SPACE = 6;
	public static final int UNABLE_TO_CREATE_RECORDING = 7;
	public static final int RECORDING_GENERIC_EXCEPTION = 8;
	
	// pitch corrector types
	public static final int PITCH_CORRECTOR_AUTOTALENT = 1;
	public static final int PITCH_CORRECTOR_TALENTEDHACK = 2;
}
