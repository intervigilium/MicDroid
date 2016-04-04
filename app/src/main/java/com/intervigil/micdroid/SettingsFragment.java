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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferences();

        setResetListener();
    }

    private void addPreferences() {
        addPreferencesFromResource(R.xml.preferences);
    }

    private void setResetListener() {
        Preference resetDefault = findPreference(getString(R.string.prefs_reset_default_key));
        resetDefault.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(getActivity());
                confirmDialogBuilder.setTitle(R.string.confirm_reset_prefs_title)
                        .setMessage(R.string.confirm_reset_prefs_message)
                        .setPositiveButton(R.string.confirm_reset_prefs_btn_yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceManager.getDefaultSharedPreferences(
                                                getActivity()).edit().clear().commit();
                                        // Force refresh preference screen
                                        setPreferenceScreen(null);
                                        addPreferences();
                                        setResetListener();
                                    }
                                })
                        .setNegativeButton(R.string.confirm_reset_prefs_btn_no,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                confirmDialogBuilder.create().show();
                return true;
            }
        });
    }
}