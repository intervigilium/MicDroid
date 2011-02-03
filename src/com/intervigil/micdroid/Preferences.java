/* Preferences.java

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

import net.sourceforge.autotalent.Autotalent;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.intervigil.micdroid.helper.PreferenceHelper;

public class Preferences extends PreferenceActivity {

    private static final String CLASS_PREFERENCES = "Preferences";

    /**
     * Called when the activity is starting. This is where most initialization
     * should go: calling setContentView(int) to inflate the activity's UI, etc.
     * 
     * @param icicle
     *            Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);

        Preference liveCorrection = (Preference) findPreference(getString(R.string.prefs_live_mode_key));
        liveCorrection.setEnabled(Autotalent.getLiveCorrectionEnabled());

        Preference resetDefault = (Preference) findPreference(getString(R.string.prefs_reset_default_key));
        resetDefault.setOnPreferenceClickListener(resetListener);
    }

    @Override
    protected void onStart() {
        Log.i(CLASS_PREFERENCES, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(CLASS_PREFERENCES, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(CLASS_PREFERENCES, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(CLASS_PREFERENCES, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(CLASS_PREFERENCES, "onDestroy()");
        super.onStop();
    }

    private OnPreferenceClickListener resetListener = new OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Builder confirmDialogBuilder = new Builder(Preferences.this);
            confirmDialogBuilder.setTitle(R.string.confirm_reset_prefs_title)
                    .setMessage(R.string.confirm_reset_prefs_message)
                    .setPositiveButton(R.string.confirm_reset_prefs_btn_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    PreferenceHelper
                                            .setDefaultPreferences(Preferences.this);
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setNegativeButton(
                            R.string.confirm_reset_prefs_btn_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
            confirmDialogBuilder.create().show();
            return true;
        }
    };
}
