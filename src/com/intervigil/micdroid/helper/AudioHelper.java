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

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.R;

public class AudioHelper {
    private static final String TAG_AUDIOHELPER = "AudioHelper";
    private static final String MANUFACTURER_SAMSUNG = "samsung";
    private static final String DEVICE_ID_GALAXY_S = "gt-i9000";
    private static final String DEVICE_ID_CAPTIVATE = "sgh-i897";
    private static final String DEVICE_ID_VIBRANT = "sgh-t959";
    private static final String DEVICE_ID_FASCINATE = "sch-i500";
    private static final String DEVICE_ID_EPIC = "sph-d700";
    private static final String DEVICE_ID_MESMERIZE = "sch-i500";

    /**
     * Convert Android AudioFormat.CHANNEL_CONFIGURATION constants to integers
     * 
     * @param androidChannels
     *            Android AudioFormat constant
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
     * @param androidEncoding
     *            Android AudioFormat constant
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
     * @param numChannels
     *            number of channels, typically 1 or 2
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
     * @param bitsPerSample
     *            bits in a sample of audio, typically 8 or 16
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
     * Gets an AudioTrack object using the current playback settings
     * 
     * @param context
     *            Context which we are getting recorder for
     */
    public static AudioTrack getPlayer(Context context)
            throws IllegalArgumentException {
        AudioTrack player = null;
        int sampleRate = PreferenceHelper.getSampleRate(context);
        int bufferSizeAdjuster = PreferenceHelper
                .getBufferSizeAdjuster(context);

        Log.i(TAG_AUDIOHELPER,
                String.format("AudioTrack initialized with saved configuration! sample rate: %d, buffer adjuster: %d",
                        sampleRate, bufferSizeAdjuster));

        int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT)
                * bufferSizeAdjuster;

        player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT,
                bufferSize, AudioTrack.MODE_STREAM);

        if (player.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new IllegalArgumentException(
                    String.format("unable to initialize AudioTrack instance, sample rate: %d, channels: %d, buffer: %d",
                            sampleRate, Constants.DEFAULT_CHANNEL_CONFIG, bufferSize));
        }

        return player;
    }

    /**
     * Gets the validity of the current recorder settings, particularly sample
     * rate; This function wraps getRecorderBufferSize(Context)
     * 
     * @param context
     *            Context which we are getting recorder information about
     */
    public static boolean isValidRecorderConfiguration(Context context) {
        return getRecorderBufferSize(context) != AudioRecord.ERROR_BAD_VALUE;
    }

    /**
     * Gets the buffer size needed for the current recorder settings This
     * function wraps AudioRecord.getMinBufferSize(SampleRate, ChannelConfig,
     * PcmFormat) which means this function can return
     * AudioRecord.ERROR_BAD_VALUE for invalid settings or AudioRecord.ERROR
     * when the system is unable to query hardware for proper settings
     * 
     * @param context
     *            Context which we are getting recorder information about
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
     * @param context
     *            Context which we are getting recorder for
     */
    public static AudioRecord getRecorder(Context context)
            throws IllegalArgumentException {
        AudioRecord recorder = null;
        int sampleRate = PreferenceHelper.getSampleRate(context);
        int bufferSize = PreferenceHelper.getBufferSize(context);
        int bufferSizeAdjuster = PreferenceHelper.getBufferSizeAdjuster(context);
        int audioSource = AudioSource.MIC;

        Log.i(TAG_AUDIOHELPER,
                String.format("AudioRecord initialized with saved configuration! sample rate: %d, buffer: %d, buffer adjuster: %d",
                        sampleRate, bufferSize, bufferSizeAdjuster));

        recorder = new AudioRecord(audioSource, sampleRate,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT,
                bufferSize * bufferSizeAdjuster);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalArgumentException(
                    String.format("unable to initialize AudioRecord instance, sample rate: %d, channels: %d, buffer: %d",
                            sampleRate, Constants.DEFAULT_CHANNEL_CONFIG, bufferSize));
        }

        return recorder;
    }

    /**
     * Attempts to autoconfigure current Context's sample rate, buffer size,
     * or buffer size adjuster . Will show a pop-up warning if configuration
     * failed to initialize an AudioRecord instance.
     * 
     * @param context
     *            Context which we are attempting to configure
     */
    public static void configureRecorder(Context context) {
        AudioRecord rec = null;
        int sampleRate = PreferenceHelper.getSampleRate(context);
        int bufferSize = PreferenceHelper.getBufferSize(context);
        int bufferSizeAdjuster = PreferenceHelper.getBufferSizeAdjuster(context);

        if (sampleRate < 0 || bufferSize < 0 || bufferSizeAdjuster < 0) {
            if (AudioHelper.isSamsungGalaxyS()) {
                // bypass checking, go with defaults
                bufferSize = 6144;
                bufferSizeAdjuster = 1;
                sampleRate = Constants.SAMPLE_RATE_22KHZ;
                Log.i(TAG_AUDIOHELPER,
                        String.format("AudioRecord initially configured! sample rate: %d, buffer size: %d, buffer size adjuster: %d",
                                sampleRate, bufferSize, bufferSizeAdjuster));
                PreferenceHelper.setSampleRate(context, sampleRate);
                PreferenceHelper.setBufferSize(context, bufferSize);
                PreferenceHelper.setBufferSizeAdjuster(context, bufferSizeAdjuster);
                return;
            }

            if (bufferSizeAdjuster < 1) {
                // clamp buffer size adjuster to minimum of 1
                bufferSizeAdjuster = 1;
            }

            // try a new sample rates until we find one that works
            do {
                switch (sampleRate) {
                    case -1:
                        // set the default to 22050Hz, so slower devices perform
                        // better
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
                        // start the loop over again with a larger buffer size
                        bufferSizeAdjuster *= 2;
                        sampleRate = Constants.SAMPLE_RATE_22KHZ;
                }

                bufferSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        Constants.DEFAULT_CHANNEL_CONFIG,
                        Constants.DEFAULT_PCM_FORMAT);
                if (bufferSize != AudioRecord.ERROR && bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    if (bufferSize <= 4096) {
                        bufferSize = 4096 * 3/2;
                    }
                    rec = new AudioRecord(
                            AudioSource.MIC,
                            sampleRate,
                            Constants.DEFAULT_CHANNEL_CONFIG,
                            Constants.DEFAULT_PCM_FORMAT,
                            bufferSize * bufferSizeAdjuster);

                    if (rec != null && rec.getState() == AudioRecord.STATE_INITIALIZED) {
                        rec.release();
                        rec = null;
                        Log.i(TAG_AUDIOHELPER,
                                String.format("AudioRecord initially configured! sample rate: %d, buffer size: %d, buffer size adjuster: %d",
                                        sampleRate, bufferSize, bufferSizeAdjuster));
                        PreferenceHelper.setSampleRate(context, sampleRate);
                        PreferenceHelper.setBufferSize(context, bufferSize);
                        PreferenceHelper.setBufferSizeAdjuster(context, bufferSizeAdjuster);
                        return;
                    }
                    if (rec != null) {
                        rec.release();
                        rec = null;
                    }
                }
            } while (bufferSizeAdjuster < Constants.DEFAULT_BUFFER_LIMIT);

            // failure to configure!
            DialogHelper.showWarning(context,
                    R.string.unable_to_configure_audio_title,
                    R.string.unable_to_configure_audio_warning);
        }
    }

    /**
     * Tries to figure out if the current phone is Galaxy S based, since it has
     * recording issues This is pretty nasty since we are string matching, but
     * unless I can get a better way to do it...
     * 
     * @param
     */
    public static boolean isSamsungGalaxyS() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        String model = android.os.Build.MODEL.toLowerCase();
        String device = android.os.Build.DEVICE.toLowerCase();
        Log.i(TAG_AUDIOHELPER, String.format(
                "manufacturer: %s, model: %s, device: %s", manufacturer, model, device));

        if (manufacturer.equals(MANUFACTURER_SAMSUNG)) {
            if (device.equals(DEVICE_ID_GALAXY_S)) {
                Log.i(TAG_AUDIOHELPER, "Samsung Galaxy S detected");
                return true;
            }

            if (device.equals(DEVICE_ID_CAPTIVATE)) {
                Log.i(TAG_AUDIOHELPER, "ATT, Samsung Captivate detected");
                return true;
            }

            if (device.equals(DEVICE_ID_VIBRANT)) {
                Log.i(TAG_AUDIOHELPER, "T-Mobile US, Samsung Vibrant detected");
                return true;
            }

            if (device.equals(DEVICE_ID_EPIC)) {
                Log.i(TAG_AUDIOHELPER, "Sprint, Samsung Epic 4G detected");
                return true;
            }

            if (device.equals(DEVICE_ID_FASCINATE)) {
                Log.i(TAG_AUDIOHELPER, "Verizon, Samsung Fascinate detected");
                return true;
            }

            if (device.equals(DEVICE_ID_MESMERIZE)) {
                Log.i(TAG_AUDIOHELPER, "Samsung Mesmerize detected");
                return true;
            }
        }
        return false;
    }
}
