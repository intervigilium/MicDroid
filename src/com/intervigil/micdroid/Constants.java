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

    // string parameters
    public static final String EMPTY_STRING = "";

    // sample rates
    public static final int SAMPLE_RATE_44KHZ = 44100;
    public static final int SAMPLE_RATE_22KHZ = 22050;
    public static final int SAMPLE_RATE_11KHZ = 11025;
    public static final int SAMPLE_RATE_8KHZ = 8000;

    // intent codes
    public static final int INTENT_FILENAME_ENTRY = 12768;
    public static final int INTENT_PREFERENCES = 12771;
    public static final int INTENT_OPEN_FILE = 12772;

    // intent extras
    public static final String INTENT_EXTRA_RECORDING = "NAME_ENTRY_RECORDING";
    public static final String INTENT_EXTRA_FILE_NAME = "NAME_ENTRY_FILENAME_RESULT";

    // shared preference keys
    public static final String KEY_LAST_VERSION_CODE = "prefs_last_version_code";

    // mime type
    public static final String MIME_AUDIO_WAV = "audio/x-wav";
}
