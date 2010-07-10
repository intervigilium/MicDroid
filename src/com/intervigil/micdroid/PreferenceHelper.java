package com.intervigil.micdroid;

import android.content.Context;
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
}
