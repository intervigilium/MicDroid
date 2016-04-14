/* AutotalentController.java
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
import android.preference.PreferenceManager;

import net.sourceforge.autotalent.Autotalent;

public class AutotalentController {

    private static final String TAG = "AutotalentController";

    private static final float CONCERT_A = 440.0f;
    private static final int DEFAULT_SCALE_ROTATE = 0;
    private static final float DEFAULT_FIXED_PITCH = 0.0f;
    private static final float DEFAULT_LFO_DEPTH = 0.0f;
    private static final float DEFAULT_LFO_RATE = 5.0f;
    private static final float DEFAULT_LFO_SHAPE = 0.0f;
    private static final float DEFAULT_LFO_SYM = 0.0f;
    private static final int DEFAULT_LFO_QUANT = 0;

    private Autotalent mAutotalent;
    private Context mContext;

    public AutotalentController(Context context) {
        mContext = context;
    }

    public void initializeAutotalent(int sampleRate) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mAutotalent = Autotalent.getInstance(sampleRate);
        mAutotalent.setConcertA(CONCERT_A);
        mAutotalent.setFixedPitch(DEFAULT_FIXED_PITCH);
        mAutotalent.setScaleRotate(DEFAULT_SCALE_ROTATE);
        mAutotalent.setLfoDepth(DEFAULT_LFO_DEPTH);
        mAutotalent.setLfoRate(DEFAULT_LFO_RATE);
        mAutotalent.setLfoShape(DEFAULT_LFO_SHAPE);
        mAutotalent.setLfoSymmetric(DEFAULT_LFO_SYM);
        mAutotalent.setLfoQuantization(DEFAULT_LFO_QUANT);
        char key = sharedPrefs.getString(
                mContext.getResources().getString(R.string.prefs_key_key),
                mContext.getResources().getString(R.string.prefs_key_default)).charAt(0);
        mAutotalent.setKey(key);
        float fixedPull = (float) sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_pitch_pull_key),
                mContext.getResources().getInteger(R.integer.prefs_pitch_pull_default)) / 100.0f;
        mAutotalent.setFixedPull(fixedPull);
        float pitchShift = sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_pitch_shift_key),
                mContext.getResources().getInteger(R.integer.prefs_pitch_shift_default));
        mAutotalent.setPitchShift(pitchShift);
        float strength = (float) sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_corr_str_key),
                mContext.getResources().getInteger(R.integer.prefs_corr_str_default)) / 100.0f;
        mAutotalent.setStrength(strength);
        float smoothness = (float) sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_corr_smooth_key),
                mContext.getResources().getInteger(R.integer.prefs_corr_smooth_default)) / 100.0f;
        mAutotalent.setSmoothness(smoothness);
        boolean formantCorrection = sharedPrefs.getBoolean(
                mContext.getResources().getString(R.string.prefs_formant_corr_key),
                mContext.getResources().getBoolean(R.bool.prefs_formant_corr_default));
        mAutotalent.enableFormantCorrection(formantCorrection);
        float formantWarp = (float) sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_formant_warp_key),
                mContext.getResources().getInteger(R.integer.prefs_formant_warp_default)) / 100.0f;
        mAutotalent.setFormantWarp(formantWarp);
        float mix = (float) sharedPrefs.getInt(
                mContext.getResources().getString(R.string.prefs_corr_mix_key),
                mContext.getResources().getInteger(R.integer.prefs_corr_mix_default)) / 100.0f;
        mAutotalent.setMix(mix);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    public void process(short[] samples, int numSamples) {
        mAutotalent.process(samples, numSamples);
    }

    public void closeAutotalent() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        mAutotalent.close();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
                    if (mContext.getString(R.string.prefs_key_key).equals(key)) {
                        char autotalentKey = sharedPrefs.getString(
                                mContext.getResources().getString(R.string.prefs_key_key),
                                mContext.getResources().getString(R.string.prefs_key_default)).charAt(0);
                        mAutotalent.setKey(autotalentKey);

                    } else if (mContext.getString(R.string.prefs_pitch_pull_key).equals(key)) {
                        float fixedPull = (float) sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_pitch_pull_key),
                                mContext.getResources().getInteger(R.integer.prefs_pitch_pull_default)) / 100.0f;
                        mAutotalent.setFixedPull(fixedPull);

                    } else if (mContext.getString(R.string.prefs_pitch_shift_key).equals(key)) {
                        float pitchShift = sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_pitch_shift_key),
                                mContext.getResources().getInteger(R.integer.prefs_pitch_shift_default));
                        mAutotalent.setPitchShift(pitchShift);

                    } else if (mContext.getString(R.string.prefs_corr_str_key).equals(key)) {
                        float strength = (float) sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_corr_str_key),
                                mContext.getResources().getInteger(R.integer.prefs_corr_str_default)) / 100.0f;
                        mAutotalent.setStrength(strength);

                    } else if (mContext.getString(R.string.prefs_corr_smooth_key).equals(key)) {
                        float smoothness = (float) sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_corr_smooth_key),
                                mContext.getResources().getInteger(R.integer.prefs_corr_smooth_default)) / 100.0f;
                        mAutotalent.setSmoothness(smoothness);

                    } else if (mContext.getString(R.string.prefs_formant_corr_key).equals(key)) {
                        boolean enabled = sharedPrefs.getBoolean(
                                mContext.getResources().getString(R.string.prefs_formant_corr_key),
                                mContext.getResources().getBoolean(R.bool.prefs_formant_corr_default));
                        mAutotalent.enableFormantCorrection(enabled);

                    } else if (mContext.getString(R.string.prefs_formant_warp_key).equals(key)) {
                        float formantWarp = (float) sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_formant_warp_key),
                                mContext.getResources().getInteger(R.integer.prefs_formant_warp_default)) / 100.0f;
                        mAutotalent.setFormantWarp(formantWarp);

                    } else if (mContext.getString(R.string.prefs_corr_mix_key).equals(key)) {
                        float mix = (float) sharedPrefs.getInt(
                                mContext.getResources().getString(R.string.prefs_corr_mix_key),
                                mContext.getResources().getInteger(R.integer.prefs_corr_mix_default)) / 100.0f;
                        mAutotalent.setMix(mix);
                    }
                }
            };
}
