/* AudioHelper.java

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

package com.intervigil.micdroid.helper;

import android.media.AudioFormat;

public class AudioHelper {
    private static final String TAG = "AudioHelper";

    private AudioHelper() {}

    /**
     * Convert Android AudioFormat.CHANNEL_CONFIGURATION constants to integers
     *
     * @param androidChannels Android AudioFormat constant
     */
    public static int getChannelConfig(int androidChannels) {
        switch (androidChannels) {
            case AudioFormat.CHANNEL_CONFIGURATION_MONO:
            case AudioFormat.CHANNEL_IN_MONO:
                return 1;
            case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
            case AudioFormat.CHANNEL_IN_STEREO:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Convert Android AudioFormat.ENCODING_PCM constants to integers
     *
     * @param androidEncoding Android AudioFormat constant
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
}