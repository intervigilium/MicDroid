/* InstrumentalLibrary.java

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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.admob.android.ads.AdView;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.model.Instrumental;

public class InstrumentalLibrary extends Activity {

    private Boolean showAds;
    private AdView ad;

    /**
     * Called when the activity is starting. This is where most initialization
     * should go: calling setContentView(int) to inflate the activity's UI, etc.
     * 
     * @param savedInstanceState
     *            Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrumental_library);

        showAds = PreferenceHelper.getShowAds(InstrumentalLibrary.this);

        ad = (AdView) findViewById(R.id.instrumental_ad);
        ad.setEnabled(showAds);
    }

    @Override
    protected void onStart() {
        Log.i(getPackageName(), "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(getPackageName(), "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(getPackageName(), "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(getPackageName(), "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(getPackageName(), "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(getPackageName(), "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return super.onRetainNonConfigurationInstance();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        Instrumental track = null;

        switch (item.getItemId()) {
        case R.string.instrumental_options_set:
            PreferenceHelper.setInstrumentalTrack(InstrumentalLibrary.this,
                    track.getName());
            Toast.makeText(InstrumentalLibrary.this,
                            R.string.instrumental_options_track_set,
                            Toast.LENGTH_SHORT).show();
            break;
        case R.string.instrumental_options_unset:
            String selectedTrack = PreferenceHelper
                    .getInstrumentalTrack(InstrumentalLibrary.this);
            if (selectedTrack.equals(track.getName())) {
                PreferenceHelper.setInstrumentalTrack(InstrumentalLibrary.this,
                        Constants.EMPTY_STRING);
                Toast.makeText(InstrumentalLibrary.this,
                        R.string.instrumental_options_track_unset,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InstrumentalLibrary.this,
                        R.string.instrumental_options_track_unset_error,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        case R.string.instrumental_options_remove:
            if (track.asFile().delete()) {
                Toast.makeText(InstrumentalLibrary.this,
                        R.string.instrumental_options_track_deleted,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        default:
            break;
        }
        return true;
    }

    private OnItemClickListener libraryClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            view.showContextMenu();
        }
    };

    private OnCreateContextMenuListener instrumentalItemListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.instrumental_options_title);
            menu.add(Menu.NONE, R.string.instrumental_options_set, Menu.NONE,
                    R.string.instrumental_options_set);
            menu.add(Menu.NONE, R.string.instrumental_options_unset, Menu.NONE,
                    R.string.instrumental_options_unset);
            menu.add(Menu.NONE, R.string.instrumental_options_remove,
                    Menu.NONE, R.string.instrumental_options_remove);
        }
    };
}
