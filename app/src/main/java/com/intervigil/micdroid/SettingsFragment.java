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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import net.sourceforge.autotalent.Autotalent;

public class SettingsFragment extends PreferenceFragment {
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);

        PreferenceCategory recordingPrefs =
                (PreferenceCategory) findPreference(getString(R.string.prefs_cat_recording_key));
        Preference liveCorrection = findPreference(getString(R.string.prefs_live_mode_key));
        if (liveCorrection != null && !Autotalent.getLiveCorrectionEnabled()) {
            liveCorrection.setEnabled(Autotalent.getLiveCorrectionEnabled());
            recordingPrefs.removePreference(liveCorrection);
        }
    }
}