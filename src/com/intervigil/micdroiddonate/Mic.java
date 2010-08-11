/* Mic.java
   An auto-tune app for Android

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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.intervigil.micdroiddonate.helper.ApplicationHelper;
import com.intervigil.micdroiddonate.helper.AudioHelper;
import com.intervigil.micdroiddonate.helper.DialogHelper;
import com.intervigil.micdroiddonate.helper.MediaStoreHelper;
import com.intervigil.micdroiddonate.helper.PreferenceHelper;
import com.intervigil.micdroiddonate.model.Recording;
import com.intervigil.micdroiddonate.pitch.AutoTalent;
import com.intervigil.micdroiddonate.wave.WaveReader;
import com.intervigil.micdroiddonate.wave.WaveWriter;

public class Mic extends Activity {

	private static final int AUTOTALENT_CHUNK_SIZE = 8192;
	
	private static final float CONCERT_A = 440.0f;

	private static final int DEFAULT_SCALE_ROTATE = 0;
	private static final float DEFAULT_LFO_DEPTH = 0.0f;
	private static final float DEFAULT_LFO_RATE = 5.0f;
	private static final float DEFAULT_LFO_SHAPE = 0.0f;
	private static final float DEFAULT_LFO_SYM = 0.0f;
	private static final int DEFAULT_LFO_QUANT = 0;
	private static final int DEFAULT_FORM_CORR = 0;
	private static final float DEFAULT_FORM_WARP = 0.0f;
	
	private WakeLock wakeLock;
	private StartupDialog startupDialog;
	private Recorder recorder;
	private Timer timer;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Typeface timerFont = Typeface.createFromAsset(getAssets(), "fonts/Clockopia.ttf");
        ToggleButton recordingButton = ((ToggleButton)findViewById(R.id.recording_button));
        Button libraryButton = ((Button)findViewById(R.id.library_button));
        TextView timerDisplay = (TextView)findViewById(R.id.recording_timer);
        
        recordingButton.setChecked(false);
        recordingButton.setOnCheckedChangeListener(mPowerBtnListener);
        libraryButton.setOnClickListener(mLibraryBtnListener);
        timerDisplay.setTypeface(timerFont);
        
        timer = new Timer(timerDisplay);
        startupDialog = new StartupDialog(this, R.string.startup_dialog_title, R.string.startup_dialog_text, R.string.startup_dialog_accept_btn);
    	
    	if (PreferenceHelper.getScreenLock(Mic.this)) {
    		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "recordingWakeLock");
    	}
    	
    	startupDialog.show();
    	AudioHelper.configureRecorder(Mic.this);
    	PreferenceHelper.resetKeyDefault(Mic.this);
    	migrateOldRecordings();
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
    	if (PreferenceHelper.getScreenLock(Mic.this)) {
    		wakeLock.acquire();
    	}
    }
    
    @Override
    protected void onPause() {
    	Log.i(getPackageName(), "onPause()");
    	super.onPause();
    	if (PreferenceHelper.getScreenLock(Mic.this)) {
    		wakeLock.release();
    	}
    }
    
    @Override
    protected void onStop() {
    	Log.i(getPackageName(), "onStop()");
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	Log.i(getPackageName(), "onDestroy()");
    	super.onStop();

    	if (recorder != null) {
    		recorder.cleanup();
    	}
    	AutoTalent.destroyAutoTalent();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	Log.i(getPackageName(), "onRestoreInstanceState()");
    	super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Log.i(getPackageName(), "onConfigurationChanged");
    	super.onConfigurationChanged(newConfig);
    	
    	setContentView(R.layout.main);

    	boolean isRecording = recorder != null ? recorder.isRunning() : false;

    	((Button)findViewById(R.id.library_button)).setOnClickListener(mLibraryBtnListener);
    	ToggleButton micSwitch = (ToggleButton)findViewById(R.id.recording_button);
    	micSwitch.setChecked(isRecording);
    	micSwitch.setOnCheckedChangeListener(mPowerBtnListener);
    	
    	Typeface timerFont = Typeface.createFromAsset(getAssets(), "fonts/Clockopia.ttf");
    	TextView timerDisplay = (TextView)findViewById(R.id.recording_timer);
    	timerDisplay.setTypeface(timerFont);
    	timer.registerDisplay(timerDisplay);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options:
            	Intent preferencesIntent = new Intent(getBaseContext(), Preferences.class);
            	startActivityForResult(preferencesIntent, Constants.PREFERENCE_INTENT_CODE);
            	break;
            case R.id.about:
            	DialogHelper.showWarning(Mic.this, R.string.about_title, R.string.about_text);
            	break;
            case R.id.help:
            	DialogHelper.showWarning(Mic.this, R.string.help_title, R.string.help_text);
            	break;
        }
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
	    	case Constants.FILENAME_ENTRY_INTENT_CODE:
	    		if (resultCode == Activity.RESULT_OK) {
	    			String fileName = data.getStringExtra(Constants.NAME_ENTRY_INTENT_FILE_NAME).trim() + ".wav";
	    			new ProcessAutotalentTask().execute(fileName);
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			Toast.makeText(Mic.this, R.string.recording_save_canceled, Toast.LENGTH_SHORT).show();
	    		}
	    		break;
	    	case Constants.PREFERENCE_INTENT_CODE:
	    		if (recorder != null && !recorder.isRunning()) {
	    			recorder.cleanup();
	    			recorder = null;
	    		}
	    		break;
    		default:
    			break;
    	}
    }
    
    private Handler recordingErrorHandler = new Handler() {
    	// use the handler to receive error messages from the recorder object
    	@Override
    	public void handleMessage(Message msg) {
    		ToggleButton recordingButton = (ToggleButton)findViewById(R.id.recording_button);
    		
    		timer.stop();
    		recorder.cleanup();
    		recorder = null;
    		recordingButton.setChecked(false);
    		
    		switch (msg.what) {
	    		case Constants.AUDIORECORD_ILLEGAL_STATE:
	    			// received error message that AudioRecord was started without being properly initialized
	    			DialogHelper.showWarning(Mic.this, R.string.audiorecord_exception_title, R.string.audiorecord_exception_warning);
	    			break;
	    		case Constants.AUDIORECORD_ILLEGAL_ARGUMENT:
	    			// received error message that AudioRecord was started with bad sample rate/buffer size
	    			DialogHelper.showWarning(Mic.this, R.string.audiorecord_exception_title, R.string.audiorecord_exception_warning);
	    			break;
	    		case Constants.WRITER_OUT_OF_SPACE:
	    			// received error that the writer is out of SD card space
	    			DialogHelper.showWarning(Mic.this, R.string.writer_out_of_space_title, R.string.writer_out_of_space_warning);
	    			break;
	    		case Constants.UNABLE_TO_CREATE_RECORDING:
	    			// received error that the writer couldn't create the recording
	    			DialogHelper.showWarning(Mic.this, R.string.unable_to_create_recording_title, R.string.unable_to_create_recording_warning);
	    			break;
	    		case Constants.RECORDING_GENERIC_EXCEPTION:
	    			// some sort of error occurred in the threads, don't know what yet, send a log!
	    			DialogHelper.showWarning(Mic.this, R.string.recording_exception_title, R.string.recording_exception_warning);
	    			break;
    		}
    	}
    };

    private class ProcessAutotalentTask extends AsyncTask<String, Void, Void> {
    	private WaveReader reader;
    	private WaveWriter writer;
    	private ProgressDialog spinner;
    	
    	public ProcessAutotalentTask() {
    		spinner = new ProgressDialog(Mic.this);
    		spinner.setCancelable(false);
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		spinner.setMessage(getString(R.string.autotalent_progress_msg));
    		spinner.show();
    	}
    	
		@Override
		protected Void doInBackground(String... params) {
			// maybe ugly but we only pass one string in anyway
			String fileName = params[0];

			try {
				reader = new WaveReader(
						ApplicationHelper.getOutputDirectory(), 
						getString(R.string.default_recording_name));
				reader.openWave();
				writer = new WaveWriter(
						ApplicationHelper.getLibraryDirectory(), 
						fileName,
						reader.getSampleRate(), reader.getChannels(), reader.getPcmFormat());
				writer.createWaveFile();
			} catch (IOException e) {
				// can't create our readers and writers for some reason!
				// TODO: real error handling
				e.printStackTrace();
			}
			
			updateAutoTalentPreferences();
			
			short[] buf = new short[AUTOTALENT_CHUNK_SIZE];
			while (true) {
				try {
					int samplesRead = reader.readShort(buf, AUTOTALENT_CHUNK_SIZE);
					if (samplesRead > 0) {
						AutoTalent.processSamples(buf, samplesRead);
						writer.write(buf, samplesRead);
					} else {
						break;
					}
				} catch (IOException e) {
					// failed to read/write to wave file
					// TODO: real error handling
					e.printStackTrace();
				}
			}
			
			try {
				reader.closeWaveFile();
				writer.closeWaveFile();
				AutoTalent.destroyAutoTalent();
			} catch (IOException e) {
				// failed to close out our files correctly
				// TODO: real error handling
				e.printStackTrace();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			spinner.dismiss();
			Toast.makeText(Mic.this, R.string.recording_save_success, Toast.LENGTH_SHORT).show();
		}
    }
    
    private OnClickListener mLibraryBtnListener = new OnClickListener() {
		public void onClick(View v) {
			Intent playbackIntent = new Intent(getBaseContext(), RecordingLibrary.class);
        	startActivity(playbackIntent);
		}
	};
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
    		if (!hasWindowFocus()) {
    			return;
    		}
    		else if (!canWriteToSdCard()) {
        		btn.setChecked(false);
    			DialogHelper.showWarning(Mic.this, R.string.no_external_storage_title, R.string.no_external_storage_warning);
        	}
    		else if (!AudioHelper.isValidRecorderConfiguration(Mic.this)) {
    			btn.setChecked(false);
    			DialogHelper.showWarning(Mic.this, R.string.unconfigured_audio_title, R.string.unconfigured_audio_warning);
    		}
    		else {
				if (btn.isChecked()) {
					if (recorder == null) {
						recorder = new Recorder(Mic.this, recordingErrorHandler, AudioHelper.getRecorderBufferSize(Mic.this));
					}
					recorder.start();
		        	timer.reset();
		        	timer.start();
		        	Toast.makeText(getBaseContext(), R.string.recording_started_toast, Toast.LENGTH_SHORT).show();
				} else {
					if (recorder != null && recorder.isRunning()) {
						// only do this if it was running, otherwise an error message triggered the check state change
						recorder.stop();
						timer.stop();
						Toast.makeText(getBaseContext(), R.string.recording_finished_toast, Toast.LENGTH_SHORT).show();
		    			Intent saveFileIntent = new Intent(getBaseContext(), FileNameEntry.class);
						startActivityForResult(saveFileIntent, Constants.FILENAME_ENTRY_INTENT_CODE);
					}
				}
    		}
		}
    };
    
    private void updateAutoTalentPreferences() {
    	char key = PreferenceHelper.getKey(Mic.this);
    	float fixedPitch = PreferenceHelper.getFixedPitch(Mic.this);
    	float fixedPull = PreferenceHelper.getPullToFixedPitch(Mic.this);
    	float pitchShift = PreferenceHelper.getPitchShift(Mic.this);
    	float strength = PreferenceHelper.getCorrectionStrength(Mic.this);
    	float smooth = PreferenceHelper.getCorrectionSmoothness(Mic.this);
    	float mix = PreferenceHelper.getMix(Mic.this);
    	
    	AutoTalent.instantiateAutoTalent(PreferenceHelper.getSampleRate(Mic.this));
    	AutoTalent.initializeAutoTalent(CONCERT_A, key, fixedPitch, fixedPull, 
    			strength, smooth, pitchShift, DEFAULT_SCALE_ROTATE, 
    			DEFAULT_LFO_DEPTH, DEFAULT_LFO_RATE, DEFAULT_LFO_SHAPE, DEFAULT_LFO_SYM, DEFAULT_LFO_QUANT, 
    			DEFAULT_FORM_CORR, DEFAULT_FORM_WARP, mix);
    }
    
    private void migrateOldRecordings() {
    	// this is a oneshot, only done on upgrade/new install
    	if (PreferenceHelper.getMovedOldLibrary(Mic.this) != ApplicationHelper.getPackageVersion(Mic.this)) {
			File oldLibraryDir = new File(ApplicationHelper.getOldLibraryDirectory());
			File[] waveFiles = oldLibraryDir.listFiles();
			
			if (waveFiles != null) {
				for (int i = 0; i < waveFiles.length; i++) {
					if (waveFiles[i].isFile() && waveFiles[i].getName().contains(".wav")) {
						try {
							Recording r = new Recording(waveFiles[i]);
							MediaStoreHelper.removeRecording(Mic.this, r);
							File destination = new File(ApplicationHelper.getLibraryDirectory() + File.separator + waveFiles[i].getName());
							r.moveTo(destination);
							MediaStoreHelper.insertRecording(Mic.this, r);
						} catch (IOException e) {
							// don't do anything since it's not a wave file, yes using exceptions for control flow is bad
						}
					}
				}
			}
			PreferenceHelper.setMovedOldLibrary(Mic.this, ApplicationHelper.getPackageVersion(Mic.this));
    	}
	}
    
    private static boolean canWriteToSdCard() {
    	return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}