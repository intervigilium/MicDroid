package com.intervigil.micdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

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
		return Integer.parseInt(prefReader.getString(context.getString(R.string.prefs_sample_rate), "-1"));
	}
	
	public static void setSampleRate(Context context, int sampleRate) {
		Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		prefEditor.putString(context.getString(R.string.prefs_sample_rate), String.format("%d", sampleRate));
		prefEditor.commit();
	}
}
