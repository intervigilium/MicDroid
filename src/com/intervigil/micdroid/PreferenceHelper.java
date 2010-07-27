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

package com.intervigil.micdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {
	
	public static void resetKeyDefault(Context context) {
		if (getKey(context) == 'c') {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
	        editor.putString(context.getString(R.string.prefs_key_key), "C");
	        editor.commit();
		}
	}
	
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
	
	public static int getMovedOldLibrary(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.KEY_MOVED_OLD_LIBRARY, -1);
	}

	public static void setMovedOldLibrary(Context context, int value) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(Constants.KEY_MOVED_OLD_LIBRARY, value);
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
