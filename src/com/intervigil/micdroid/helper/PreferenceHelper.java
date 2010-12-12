/* PreferenceHelper.java

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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.R;

public class PreferenceHelper {

    public static boolean getShowAds(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean pref = prefReader.getBoolean(
                context.getString(R.string.prefs_enable_ads_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_enable_ads_default)));
        return pref;
    }

    public static boolean getScreenLock(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean pref = prefReader.getBoolean(
                context.getString(R.string.prefs_prevent_screen_lock_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_prevent_screen_lock_default)));
        return pref;
    }

    public static boolean getLiveMode(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean pref = prefReader.getBoolean(
                context.getString(R.string.prefs_live_mode_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_live_mode_default)));
        return pref;
    }

    public static char getKey(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefReader.getString(
                context.getString(R.string.prefs_key_key),
                context.getString(R.string.prefs_key_default));
        return pref.charAt(0);
    }

    public static float getPullToFixedPitch(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_pitch_pull_key),
                context.getString(R.string.prefs_pitch_pull_default));
        return Float.valueOf(pref);
    }

    public static float getPitchShift(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_pitch_shift_key),
                context.getString(R.string.prefs_pitch_shift_default));
        return Float.valueOf(pref);
    }

    public static float getCorrectionStrength(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_corr_str_key),
                context.getString(R.string.prefs_corr_str_default));
        return Float.valueOf(pref);
    }

    public static float getCorrectionSmoothness(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_corr_smooth_key),
                context.getString(R.string.prefs_corr_smooth_default));
        return Float.valueOf(pref);
    }

    public static boolean getFormantCorrection(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        boolean pref = prefReader.getBoolean(
                context.getString(R.string.prefs_formant_corr_key),
                Boolean.parseBoolean(context.getString(R.string.prefs_formant_corr_default)));
        return pref;
    }

    public static float getFormantWarp(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_formant_warp_key),
                context.getString(R.string.prefs_formant_warp_default));
        return Float.valueOf(pref);
    }

    public static float getMix(Context context) {
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefs.getString(
                context.getString(R.string.prefs_corr_mix_key),
                context.getString(R.string.prefs_corr_mix_default));
        return Float.valueOf(pref);
    }

    public static int getSampleRate(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        String sampleRate = prefReader.getString(
                context.getString(R.string.prefs_sample_rate_key), "-1");
        return Integer.parseInt(sampleRate);
    }

    public static void setSampleRate(Context context, int sampleRate) {
        Editor prefEditor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(context.getString(R.string.prefs_sample_rate_key),
                String.format("%d", sampleRate));
        prefEditor.commit();
    }

    public static int getBufferSize(Context context) {
        SharedPreferences prefReader = PreferenceManager
                .getDefaultSharedPreferences(context);
        String bufferSize = prefReader.getString(
                context.getString(R.string.prefs_buffer_size_key),
                "-1");
        return Integer.parseInt(bufferSize);
    }

    public static void setBufferSize(Context context,
            int bufferSize) {
        Editor prefEditor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(
                context.getString(R.string.prefs_buffer_size_key),
                String.format("%d", bufferSize));
        prefEditor.commit();
    }

    public static int getBufferSizeAdjuster(Context context) {
        SharedPreferences prefReader = PreferenceManager
                .getDefaultSharedPreferences(context);
        String bufferSize = prefReader.getString(
                context.getString(R.string.prefs_buffer_size_adjuster_key),
                "-1");
        return Integer.parseInt(bufferSize);
    }

    public static void setBufferSizeAdjuster(Context context,
            int bufferSizeAdjuster) {
        Editor prefEditor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(
                context.getString(R.string.prefs_buffer_size_adjuster_key),
                String.format("%d", bufferSizeAdjuster));
        prefEditor.commit();
    }

    public static String getInstrumentalTrack(Context context) {
        SharedPreferences prefReader =
            PreferenceManager.getDefaultSharedPreferences(context);
        String pref = prefReader.getString(
                context.getString(R.string.prefs_instrumental_track_key),
                Constants.EMPTY_STRING);
        return pref;
    }

    public static void setInstrumentalTrack(Context context,
            String instrumentalName) {
        Editor prefEditor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.putString(
                context.getString(R.string.prefs_instrumental_track_key),
                instrumentalName);
        prefEditor.commit();
    }

    public static int getLastVersionCode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(
                Constants.KEY_LAST_VERSION_CODE, -1);
    }

    public static void setLastVersionCode(Context context, int value) {
        Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(Constants.KEY_LAST_VERSION_CODE, value);
        editor.commit();
    }

    public static void setDefaultPreferences(Context context) {
        Editor prefEditor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefEditor.clear().commit();

        AudioHelper.configureRecorder(context);
    }

    public static void resetFormantCorrectionDefault(Context context) {
        Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(context.getString(R.string.prefs_formant_corr_key));
        editor.commit();
    }

    public static void resetPitchShiftDefault(Context context) {
        Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(context.getString(R.string.prefs_pitch_shift_key));
        editor.commit();
    }

    public static void unsetRecordingSettings(Context context) {
        Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(context.getString(R.string.prefs_sample_rate_key));
        editor.remove(context.getString(R.string.prefs_buffer_size_key));
        editor.remove(context.getString(R.string.prefs_buffer_size_adjuster_key));
        editor.commit();
    }
}
