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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.model.Instrumental;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.micdroid.wave.WaveReader;

public class InstrumentalLibrary extends Activity {

	private static final String STATE_LOAD_IN_PROGRESS = "load_instrumentals_in_progress";
	
	private ListView library;
	private InstrumentalAdapter libraryAdapter;
	private ArrayList<Instrumental> instrumentals;
	private LoadInstrumentalsTask loadInstrumentalsTask;
	
	private ProgressDialog loadInstrumentalSpinner;
	
	/**
     * Called when the activity is starting.  This is where most
     * initialization should go: calling setContentView(int) to inflate
     * the activity's UI, etc.
     * 
     * @param   savedInstanceState	Activity's saved state, if any.
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_library);

        library = (ListView)findViewById(R.id.recording_library_list);
        library.setOnItemClickListener(libraryClickListener);
        registerForContextMenu(library);
        
        Object savedInstrumentals = getLastNonConfigurationInstance();
        if (savedInstrumentals == null) {
	        instrumentals = new ArrayList<Instrumental>();
	        this.libraryAdapter = new InstrumentalAdapter(this, R.layout.instrumental_library_row, instrumentals);
	        library.setAdapter(libraryAdapter);
			loadInstrumentalsTask = (LoadInstrumentalsTask) new LoadInstrumentalsTask().execute((Void)null);
        } else {
        	instrumentals = (ArrayList<Instrumental>)savedInstrumentals;
        	this.libraryAdapter = new InstrumentalAdapter(this, R.layout.instrumental_library_row, instrumentals);
	        library.setAdapter(libraryAdapter);
	        this.libraryAdapter.notifyDataSetChanged();
        }
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
    	
    	onCancelLoadInstrumentals();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        
        saveLoadInstrumentalsTask(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	Log.i(getPackageName(), "onRestoreInstanceState()");
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	restoreLoadInstrumentalsTask(savedInstanceState);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	final ArrayList<Instrumental> instrumentalList = instrumentals;
    	return instrumentalList;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
	    	
    		default:
    			break;
    	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	Instrumental track = (Instrumental) libraryAdapter.getItem(info.position); 
    	
    	switch (item.getItemId()) {
			
			default:
				break;
		}
    	return true;
    }
    
    private OnItemClickListener libraryClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Instrumental track = (Instrumental)parent.getItemAtPosition(position);
			
	    	
		}
	};
    
    private class InstrumentalAdapter extends ArrayAdapter<Instrumental> {		
		public InstrumentalAdapter(Context context, int textViewResourceId, List<Instrumental> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.instrumental_library_row, parent, false);
            }
            
            Instrumental track = this.getItem(position);
            if (track != null) {
            	Drawable rowIcon = track.isSelected() ? 
            			getResources().getDrawable(R.drawable.android_music) : getResources().getDrawable(R.drawable.android_music);

            	((ImageView)view.findViewById(R.id.instrumental_row_icon)).setImageDrawable(rowIcon);
            	((TextView)view.findViewById(R.id.instrumental_row_first_line)).setText("Name: " + track.getName());
                ((TextView)view.findViewById(R.id.instrumental_row_second_line)).setText("Length: " + track.getLength());
            }

            return view;
        }
    }
    
    private void onCancelLoadInstrumentals() {
    	if (loadInstrumentalsTask != null && loadInstrumentalsTask.getStatus() == AsyncTask.Status.RUNNING) {
    		loadInstrumentalsTask.cancel(true);
    		loadInstrumentalsTask = null;
    	}
    }
    
    private void saveLoadInstrumentalsTask(Bundle outState) {
    	final LoadInstrumentalsTask task = loadInstrumentalsTask;
    	if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
    		task.cancel(true);
    		outState.putBoolean(STATE_LOAD_IN_PROGRESS, true);
    	}
    }
    
    private void restoreLoadInstrumentalsTask(Bundle savedInstanceState) {
    	if (savedInstanceState.getBoolean(STATE_LOAD_IN_PROGRESS)) {
    		loadInstrumentalsTask = (LoadInstrumentalsTask) new LoadInstrumentalsTask().execute((Void)null);
    	}
    }
    
    private class LoadInstrumentalsTask extends AsyncTask<Void, Void, Void> {
    	// Async load all the recordings already in the directory
    	
    	@Override
    	protected void onPreExecute() {
    		library.setVisibility(View.INVISIBLE);
    		instrumentals.clear();
    		loadInstrumentalSpinner = new ProgressDialog(InstrumentalLibrary.this);
    		loadInstrumentalSpinner.setMessage("Loading instrumentals");
    		loadInstrumentalSpinner.show();
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			File instrumentalDir = new File(ApplicationHelper.getInstrumentalDirectory());
			File[] waveFiles = instrumentalDir.listFiles();
			
			if (waveFiles != null) {
				for (int i = 0; i < waveFiles.length; i++) {
					if (waveFiles[i].isFile()) {
						WaveReader reader = new WaveReader(waveFiles[i]);
						
						try {
							reader.openWave();
							Instrumental r = new Instrumental(instrumentalDir.getAbsolutePath(), waveFiles[i].getName(), reader.getLength(), reader.getDataSize() + Recording.WAVE_HEADER_SIZE, false);
							reader.closeWaveFile();
							reader = null;
							
							instrumentals.add(r);
					    	
						} catch (IOException e) {
							// yes I know it sucks that we do control flow with an exception here, fix it later
							Log.i("InstrumentalLibrary", String.format("Non-wave file %s found in library directory!", waveFiles[i].getName()));
						}
					}
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			library.setVisibility(View.VISIBLE);
			libraryAdapter.notifyDataSetChanged();
			loadInstrumentalSpinner.dismiss();
		}
    }
}
