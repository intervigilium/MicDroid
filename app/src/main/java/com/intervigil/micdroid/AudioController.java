/* AudioConfigurator.java
   An auto-tune app for Android

   Copyright (c) 2016 Ethan Chen

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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;

import com.intervigil.micdroid.helper.DialogHelper;

public class AudioController {
    private static final String TAG = "AudioController";

    private Context mContext;
    private int mInputBufferSize;
    private int mInputSampleRate;
    private boolean mIsLive;

    public AudioController(Context context) {
        mContext = context;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        loadPreferences(sharedPrefs);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    public int getSampleRate() {
        return mInputSampleRate;
    }

    public boolean isLive() {
        return mIsLive;
    }

    public boolean isValidRecorder() {
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mInputSampleRate, Constants.DEFAULT_CHANNEL_CONFIG,
                Constants.DEFAULT_PCM_FORMAT, mInputBufferSize);
        boolean valid = recorder.getState() == AudioRecord.STATE_INITIALIZED;
        recorder.release();
        return valid;
    }

    public AudioRecord getRecorder()
            throws IllegalArgumentException {
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mInputSampleRate, Constants.DEFAULT_CHANNEL_CONFIG,
                Constants.DEFAULT_PCM_FORMAT, mInputBufferSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new IllegalArgumentException("Unable to initialize AudioRecord, buffer: " +
                    mInputBufferSize);
        }

        return recorder;
    }

    public void configureRecorder() {
        int[] sampleRates = {
                Constants.SAMPLE_RATE_44KHZ,
                Constants.SAMPLE_RATE_22KHZ,
                Constants.SAMPLE_RATE_11KHZ,
                Constants.SAMPLE_RATE_8KHZ,
        };
        double[] multipliers = {
                1.0, 0.5, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0
        };

        // Assumption: The system isn't lying about this being the absolute minimum
        int minBufferSize = AudioRecord.getMinBufferSize(Constants.DEFAULT_SAMPLE_RATE,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT);

        for (int sampleRate : sampleRates) {
            for (double multiplier : multipliers) {
                AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT,
                        (int) (minBufferSize * multiplier));

                if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    saveBufferSize((int) (minBufferSize * multiplier));
                    saveSampleRate(sampleRate);
                    recorder.release();
                    return;
                }
                recorder.release();
            }
        }

        // Could not find valid setting
        DialogHelper.showWarning(mContext, R.string.unable_to_configure_audio_title,
                R.string.unable_to_configure_audio_warning);
    }

    public AudioTrack getPlayer() {
        int bufferSize = AudioTrack.getMinBufferSize(mInputSampleRate,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT);
        AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, mInputSampleRate,
                Constants.DEFAULT_CHANNEL_CONFIG, Constants.DEFAULT_PCM_FORMAT,
                bufferSize, AudioTrack.MODE_STREAM);
        if (player.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new IllegalArgumentException("Unable to initialize AudioRecord, buffer: " +
                    bufferSize);
        }

        return player;
    }

    private void saveSampleRate(int sampleRate) {
        SharedPreferences.Editor prefEditor =
                PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        prefEditor.putString(mContext.getString(R.string.prefs_sample_rate_key),
                String.format("%d", sampleRate));
        prefEditor.commit();
    }

    private void saveBufferSize(int bufferSize) {
        SharedPreferences.Editor prefEditor =
                PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        prefEditor.putInt(mContext.getString(R.string.prefs_buffer_size_key), bufferSize);
        prefEditor.commit();
    }

    private void loadPreferences(SharedPreferences sharedPrefs) {
        /* Recorder settings */
        mInputBufferSize = sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_buffer_size_key),
                mContext.getResources().getInteger(R.integer.prefs_buffer_size_default));
        mInputSampleRate = Integer.parseInt(sharedPrefs.getString(
                mContext.getResources().getString(R.string.prefs_sample_rate_key),
                mContext.getResources().getString(R.string.prefs_sample_rate_default)));
        mIsLive = sharedPrefs.getBoolean(
                mContext.getResources().getString(R.string.prefs_live_mode_key),
                mContext.getResources().getBoolean(R.bool.prefs_live_mode_default));
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
                    if (mContext.getString(R.string.prefs_buffer_size_key).equals(key)) {
                        mInputBufferSize = sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_buffer_size_key),
                                mContext.getResources().getInteger(R.integer.prefs_buffer_size_default));
                    } else if (mContext.getString(R.string.prefs_sample_rate_key).equals(key)) {
                        mInputSampleRate = Integer.parseInt(sharedPrefs.getString(
                                mContext.getResources().getString(R.string.prefs_sample_rate_key),
                                mContext.getResources().getString(R.string.prefs_sample_rate_default)));
                    } else if (mContext.getString(R.string.prefs_live_mode_key).equals(key)) {
                        mIsLive = sharedPrefs.getBoolean(
                                mContext.getResources().getString(R.string.prefs_live_mode_key),
                                mContext.getResources().getBoolean(R.bool.prefs_live_mode_default));
                    }
                }
            };
}