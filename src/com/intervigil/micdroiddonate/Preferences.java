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

package com.intervigil.micdroiddonate;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.intervigil.micdroiddonate.helper.DialogHelper;
import com.intervigil.micdroiddonate.helper.PreferenceHelper;


public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	/**
     * Called when the activity is starting.  This is where most
     * initialization should go: calling setContentView(int) to inflate
     * the activity's UI, etc.
     * 
     * @param   icicle          Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
        
        Preference resetDefault = (Preference) findPreference(getString(R.string.prefs_reset_default_key));
        resetDefault.setOnPreferenceClickListener(resetListener);
    }
    
    @Override
    protected void onStart() {
        Log.i("Preferences", "onStart()");
        super.onStart();
    }
    
    @Override
    protected void onResume() {
    	Log.i("Preferences", "onResume()");
        super.onResume();
        
        // Set up a listener whenever a key changes      
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
    	Log.i("Preferences", "onPause()");
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
    
    @Override
    protected void onStop() {
    	Log.i("Preferences", "onStop()");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.i("Preferences", "onDestroy()");
    	super.onStop();
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.prefs_live_mode_key))) {
			if (PreferenceHelper.getLiveMode(Preferences.this)) {
				DialogHelper.showWarning(Preferences.this, R.string.live_mode_enable_title, R.string.live_mode_enable_warning);
			}
		}
	}
	
	private OnPreferenceClickListener resetListener = new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference preference) {
			Builder confirmDialogBuilder = new Builder(Preferences.this);
    		confirmDialogBuilder.setTitle(R.string.confirm_reset_prefs_title)
    			.setMessage(R.string.confirm_reset_prefs_message)
    			.setPositiveButton(R.string.confirm_reset_prefs_btn_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PreferenceHelper.setDefaultPreferences(Preferences.this);
		    			dialog.dismiss();
		    			finish();
					}
				})
    			.setNegativeButton(R.string.confirm_reset_prefs_btn_no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
    		confirmDialogBuilder.create().show();
    		return true;
		}
	};
}
