/* Mic.java
   An auto-tune app for Android

   Copyright (c) 2010 Ethan Chen

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
 */

package com.intervigil.micdroid;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

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
	
	private StartupDialog startupDialog;
	private Thread micRecorderThread;
	private Thread micWriterThread;
	private MicRecorder micRecorder;
	private MicWriter micWriter; 
	private BlockingQueue<Sample> playQueue;
	
	/** Packet of audio to pass between reader and writer threads. */
	private class Sample {
    	public short[] buffer;
    	public int bufferSize;
    	
    	public Sample(short[] buffer, int bufferSize) {
    		this.buffer = buffer;
    		this.bufferSize = bufferSize;
    	}
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((ToggleButton)findViewById(R.id.mic_toggle)).setOnCheckedChangeListener(mPowerBtnListener);
        startupDialog = new StartupDialog(this, R.string.startup_dialog_title, R.string.startup_dialog_text, R.string.startup_dialog_accept_btn);
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
    	
    	((ToggleButton)findViewById(R.id.mic_toggle)).setChecked(false);
    	if (playQueue != null) {
    		playQueue.clear();
    	} else {
    		playQueue = new LinkedBlockingQueue<Sample>();
    	}
    	
    	micRecorder = new MicRecorder(playQueue);
    	micWriter = new MicWriter(playQueue);
    	
    	startupDialog.show();
    	AudioHelper.configureRecorder(Mic.this);
    	
    	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    }
    
    @Override
    protected void onPause() {
    	Log.i(getPackageName(), "onPause()");
    	super.onPause();
    	
    	if (micRecorder != null) {
    		micRecorder.stopRunning();
    	}
    	if (micWriter != null) {
    		micWriter.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
    }
    
    @Override
    protected void onStop() {
    	Log.i(getPackageName(), "onStop()");
    	super.onStop();
    	
    	if (micRecorder != null) {
    		micRecorder.stopRunning();
    	}
    	if (micWriter != null) {
    		micWriter.stopRunning();
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
    	
    	boolean isRecording = micRecorder.isRunning() || micWriter.isRunning();
    	ToggleButton micSwitch = (ToggleButton)findViewById(R.id.mic_toggle);
    	micSwitch.setChecked(isRecording);
    	micSwitch.setOnCheckedChangeListener(mPowerBtnListener);
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
            	// Launch preferences as a subactivity
            	Intent preferencesIntent = new Intent(getBaseContext(), Preferences.class);
            	startActivity(preferencesIntent);
            	break;
            case R.id.playback:
            	// Launch playback mode as a subactivity
            	Intent playbackIntent = new Intent(getBaseContext(), RecordingLibrary.class);
            	startActivity(playbackIntent);
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
	    			String fileName = data.getStringExtra(getString(R.string.filename_entry_result));
	    			fileName = fileName + ".wav";
	    			Log.d(getPackageName(), String.format("filename is %s", fileName));
	    			new ProcessAutotalentTask().execute(fileName);
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			Toast.makeText(Mic.this, R.string.recording_save_canceled, Toast.LENGTH_SHORT).show();
	    		}
	    		break;
    		default:
    			break;
    	}
    }
    
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
						((MicApplication)getApplication()).getOutputDirectory(), 
						getString(R.string.default_recording_name));
				reader.openWave();
				writer = new WaveWriter(
						((MicApplication)getApplication()).getLibraryDirectory(), 
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
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				if (AudioHelper.isValidRecorderConfiguration(Mic.this)) {
					micRecorderThread = new Thread(micRecorder, "Mic Recorder Thread");
					micRecorderThread.setPriority(Thread.MAX_PRIORITY);
					micRecorderThread.start();

		        	micWriterThread = new Thread(micWriter, "Mic Writer Thread");
		        	micWriterThread.start();
		        	Toast.makeText(getBaseContext(), R.string.recording_started_toast, Toast.LENGTH_SHORT).show();
				} else {
					// TODO: theoretically this should stop those force-close on failure to record, but
					// it's possible that we can get the system to think the configuration is valid and still
					// have AudioRecord fail to initialize, so we need to make MicWriter start if and only if 
					// MicRecorder starts up correctly
					
					btn.setChecked(false);
					DialogHelper.showWarning(Mic.this, R.string.unconfigured_audio_title, R.string.unconfigured_audio_warning);
				}
			} else {
				Toast.makeText(getBaseContext(), R.string.recording_finished_toast, Toast.LENGTH_SHORT).show();
				
				micRecorder.stopRunning();
				micWriter.stopRunning();
				try {
					micRecorderThread.join();
					micWriterThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				playQueue.clear();
				
				// prompt user to save recording
				Intent saveFileIntent = new Intent(getBaseContext(), FileNameEntry.class);
				startActivityForResult(saveFileIntent, Constants.FILENAME_ENTRY_INTENT_CODE);
			}
		}
    };
    
    private void updateAutoTalentPreferences() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	char key = prefs.getString("key", getString(R.string.prefs_key_default)).charAt(0);
    	float fixedPitch = Float.valueOf(prefs.getString("fixed_pitch", getString(R.string.prefs_fixed_pitch_default)));
    	float fixedPull = Float.valueOf(prefs.getString("pitch_pull", getString(R.string.prefs_pitch_pull_default)));
    	float pitchShift = Float.valueOf(prefs.getString("pitch_shift", getString(R.string.prefs_pitch_shift_default)));
    	float strength = Float.valueOf(prefs.getString("strength", getString(R.string.prefs_corr_str_default)));
    	float smooth = Float.valueOf(prefs.getString("smooth", getString(R.string.prefs_corr_smooth_default)));
    	float mix = Float.valueOf(prefs.getString("mix", getString(R.string.prefs_corr_mix_default)));
    	
    	AutoTalent.instantiateAutoTalent(PreferenceHelper.getSampleRate(Mic.this));
    	AutoTalent.initializeAutoTalent(CONCERT_A, key, fixedPitch, fixedPull, 
    			strength, smooth, pitchShift, DEFAULT_SCALE_ROTATE, 
    			DEFAULT_LFO_DEPTH, DEFAULT_LFO_RATE, DEFAULT_LFO_SHAPE, DEFAULT_LFO_SYM, DEFAULT_LFO_QUANT, 
    			DEFAULT_FORM_CORR, DEFAULT_FORM_WARP, mix);
    }
    
    private class MicWriter implements Runnable {
    	private final BlockingQueue<Sample> queue;
    	private boolean isRunning;
    	private WaveWriter writer;
    	
    	public MicWriter(BlockingQueue<Sample> q) {
    		queue = q;
    	}
    	
    	public boolean isRunning() {
    		return this.isRunning;
    	}
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
		public void run() {
			isRunning = true;
			try {
				writer = new WaveWriter(
						((MicApplication)getApplication()).getOutputDirectory(),
						getString(R.string.default_recording_name), 
						PreferenceHelper.getSampleRate(Mic.this), 
						AudioHelper.getChannelConfig(Constants.DEFAULT_CHANNEL_CONFIG), 
						AudioHelper.getPcmEncoding(Constants.DEFAULT_PCM_FORMAT));
				writer.createWaveFile();
    		} catch (IOException e) {
				// uh oh, cannot create writer or wave file, abort!
    			// TODO: real error handling
				e.printStackTrace();
			}

			while (isRunning) {
				try {
					Sample sample = queue.take();
					writer.write(sample.buffer, sample.bufferSize);
				} catch (IOException e) {
					// problem writing to the buffer
					e.printStackTrace();
				} catch (InterruptedException e) {
					// problem removing from the queue
					e.printStackTrace();
				}
			}
			
			try {
				writer.closeWaveFile();
			} catch (IOException e) {
				// problem writing the header or closing the output stream
				e.printStackTrace();
			}
			isRunning = false;
		}
    }
    
    private class MicRecorder implements Runnable {
    	private final BlockingQueue<Sample> queue;
    	private boolean isRunning;
    	    	
    	public MicRecorder(BlockingQueue<Sample> q) {
    		queue = q;
    	}
    	
    	public boolean isRunning() {
    		return this.isRunning;
    	}
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
    	public void run() {
    		isRunning = true;

    		try {
    			AudioRecord recorder = AudioHelper.getRecorder(Mic.this);

	    		short[] buffer = new short[AudioHelper.getRecorderBufferSize(Mic.this)];
	    		recorder.startRecording();
	    		
	    		while (isRunning) {
	    			try {
	    				int numSamples = recorder.read(buffer, 0, buffer.length);
						queue.put(new Sample(buffer, numSamples));
					} catch (InterruptedException e) {
						// problem putting on the queue
						e.printStackTrace();
					}
	    		}
	    		
	    		recorder.stop();
	    		recorder.release();
	    		recorder = null;
    		} catch (IllegalArgumentException e) {
    			Log.e(getPackageName(), "Unable to initialize AudioRecord! Check sample rate settings!");
    			e.printStackTrace();
    			throw e;
    		}
    		isRunning = false;
    	}
    }
}