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
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.R;

public class PreferenceHelper {

    private PreferenceHelper() {}

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
        editor.commit();
    }
}
