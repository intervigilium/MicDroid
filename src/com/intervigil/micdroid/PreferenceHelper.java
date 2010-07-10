package com.intervigil.micdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {
	
	public static char getKey(Context context) {
		SharedPreferences prefReader = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefReader.getString(context.getString(R.string.prefs_key_key), context.getString(R.string.prefs_key_default));
		return pref.charAt(0);
	}
	
	public static float getFixedPitch(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_fixed_pitch_key), context.getString(R.string.prefs_fixed_pitch_default));
		return Float.valueOf(pref);
	}
	
	public static float getPullToFixedPitch(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_pitch_pull_key), context.getString(R.string.prefs_pitch_pull_default));
		return Float.valueOf(pref);
	}
	
	public static float getPitchShift(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_pitch_shift_key), context.getString(R.string.prefs_pitch_shift_default));
		return Float.valueOf(pref);
	}
	
	public static float getCorrectionStrength(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_corr_str_key), context.getString(R.string.prefs_corr_str_default));
		return Float.valueOf(pref);
	}
	
	public static float getCorrectionSmoothness(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_corr_smooth_key), context.getString(R.string.prefs_corr_smooth_default));
		return Float.valueOf(pref);
	}
	
	public static float getMix(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(context.getString(R.string.prefs_corr_mix_key), context.getString(R.string.prefs_corr_mix_default));
		return Float.valueOf(pref);
	}
	
	public static int getSeenStartupDialog(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.KEY_SEEN_STARTUP_DIALOG, -1);
	}
	
	public static void setSeenStartupDialog(Context context, int value) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(Constants.KEY_SEEN_STARTUP_DIALOG, value);
        editor.commit();
	}
	
	public static int getSampleRate(Context context) {
		SharedPreferences prefReader = PreferenceManager.getDefaultSharedPreferences(context);
		String sampleRate = prefReader.getString(context.getString(R.string.prefs_sample_rate_key), "-1");
		return Integer.parseInt(sampleRate);
	}
	
	public static void setSampleRate(Context context, int sampleRate) {
		Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefEditor.putString(context.getString(R.string.prefs_sample_rate_key), String.format("%d", sampleRate));
		prefEditor.commit();
	}
}
