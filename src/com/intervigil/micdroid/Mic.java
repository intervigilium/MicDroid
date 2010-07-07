package com.intervigil.micdroid;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
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
	
	private static final int DEFAULT_SAMPLE_RATE = 22050;
	private static final int DEFAULT_CHANNELS = 1;
	private static final int DEFAULT_PCM_FORMAT = 16;
	
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
	    			
	    			Toast.makeText(this, R.string.recording_save_success, Toast.LENGTH_SHORT).show();
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			Toast.makeText(this, R.string.recording_save_canceled, Toast.LENGTH_SHORT).show();
	    		} else {
	    			// Something went wrong!
	    		}
	    		break;
    		default:
    			break;
    	}
    }
    
    private class ProcessAutotalentTask extends AsyncTask<String, Void, Void> {
    	private WaveReader reader;
    	private WaveWriter writer;
    	
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
						DEFAULT_SAMPLE_RATE, reader.getChannels(), reader.getPcmFormat());
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
    }
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				micRecorderThread = new Thread(micRecorder, "Mic Recorder Thread");
				micRecorderThread.setPriority(Thread.MAX_PRIORITY);
				micRecorderThread.start();

	        	micWriterThread = new Thread(micWriter, "Mic Writer Thread");
	        	micWriterThread.start();
	        	Toast.makeText(getBaseContext(), R.string.recording_started_toast, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), R.string.recording_finished_toast, Toast.LENGTH_SHORT).show();
				
				micRecorder.stopRunning();
				micWriter.stopRunning();
				try {
					micRecorderThread.join();
					micWriterThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
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
    	
    	AutoTalent.instantiateAutoTalent(DEFAULT_SAMPLE_RATE);
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
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
		public void run() {
			isRunning = true;
			try {
				writer = new WaveWriter(
						((MicApplication)getApplication()).getOutputDirectory(),
						getString(R.string.default_recording_name), 
						DEFAULT_SAMPLE_RATE, 
						DEFAULT_CHANNELS, 
						DEFAULT_PCM_FORMAT);
				writer.createWaveFile();
    		} catch (IOException e) {
				// uh oh, cannot create writer or wave file, abort!
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
		}
    }
    
    private class MicRecorder implements Runnable {
    	private final BlockingQueue<Sample> queue;
    	private boolean isRunning;
    	    	
    	public MicRecorder(BlockingQueue<Sample> q) {
    		queue = q;
    	}
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
    	public void run() {
    		isRunning = true;
    		
    		int bufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
    		
    		AudioRecord recorder = new AudioRecord(AudioSource.MIC,
    				DEFAULT_SAMPLE_RATE, 
    				AudioHelper.convertChannelConfig(DEFAULT_CHANNELS), 
    				AudioHelper.convertPcmEncoding(DEFAULT_PCM_FORMAT),
    				bufferSize);
    		
    		short[] buffer = new short[bufferSize];
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
    	}
    }
}