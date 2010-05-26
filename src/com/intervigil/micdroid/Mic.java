package com.intervigil.micdroid;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Mic extends Activity {
	
	private static final int MICDROID_PREFERENCES_CODE = 1337;
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final int DEFAULT_SAMPLE_RATE = 22050;
	
	private static final float CONCERT_A = 440.0f;

	private static final float DEFAULT_SCALE_ROTATE = 0.0f;
	private static final float DEFAULT_LFO_DEPTH = 0.0f;
	private static final float DEFAULT_LFO_RATE = 5.0f;
	private static final float DEFAULT_LFO_SHAPE = 0.0f;
	private static final float DEFAULT_LFO_SYM = 0.0f;
	private static final int DEFAULT_LFO_QUANT = 0;
	private static final int DEFAULT_FORM_CORR = 0;
	private static final float DEFAULT_FORM_WARP = 0.0f;
	
	private Thread micRecorderThread;
	private Thread micPlayerThread;
	private MicPlayer micPlayer;
	private MicRecorder micRecorder;
	private BlockingQueue<short[]> playQueue;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ToggleButton powerBtn = (ToggleButton)findViewById(R.id.mic_toggle);
        powerBtn.setOnCheckedChangeListener(mPowerBtnListener);
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
    		playQueue = new LinkedBlockingQueue<short[]>();
    	}
    	
    	micRecorder = new MicRecorder(playQueue);
    	micPlayer = new MicPlayer(playQueue);
    }
    
    @Override
    protected void onPause() {
    	Log.i(getPackageName(), "onPause()");
    	super.onPause();
    	
    	if (micRecorder != null) {
    		micRecorder.stopRunning();
    	}
    	if (micPlayer != null) {
    		micPlayer.stopRunning();
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
    	if (micPlayer != null) {
    		micPlayer.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
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
            	startActivityForResult(preferencesIntent, MICDROID_PREFERENCES_CODE);
            	break;
        }
        return true;
    }
    
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	switch (requestCode) {
//	    	case MICDROID_PREFERENCES_CODE:
//	    		updateAutoTalentPreferences();
//	    		break;
//    	}
//    }
    
    private void updateAutoTalentPreferences() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	char key = prefs.getString("key", null).charAt(0);
    	float fixedPitch = Float.valueOf(prefs.getString("fixed_pitch", null));
    	float fixedPull = Float.valueOf(prefs.getString("pitch_pull", null));
    	float pitchShift = Float.valueOf(prefs.getString("pitch_shift", null));
    	float strength = Float.valueOf(prefs.getString("strength", null));
    	float smooth = Float.valueOf(prefs.getString("smooth", null));
    	float mix = Float.valueOf(prefs.getString("mix", null));
    	
    	AutoTalent.instantiateAutoTalent(DEFAULT_SAMPLE_RATE);
    	AutoTalent.initializeAutoTalent(CONCERT_A, key, fixedPitch, fixedPull, 
    			strength, smooth, pitchShift, DEFAULT_SCALE_ROTATE, 
    			DEFAULT_LFO_DEPTH, DEFAULT_LFO_RATE, DEFAULT_LFO_SHAPE, DEFAULT_LFO_SYM, DEFAULT_LFO_QUANT, 
    			DEFAULT_FORM_CORR, DEFAULT_FORM_WARP, mix);
    }
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				updateAutoTalentPreferences();
	    		
				micRecorderThread = new Thread(micRecorder, "Mic Recorder Thread");
				micRecorderThread.start();

	        	micPlayerThread = new Thread(micPlayer, "Mic Player Thread");
	        	micPlayerThread.start();
			} else {
				micRecorder.stopRunning();
				micPlayer.stopRunning();
				try {
					micRecorderThread.join();
					micPlayerThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				playQueue.clear();
				AutoTalent.destroyAutoTalent();
			}
		}
    };

    private class MicPlayer implements Runnable {
    	private final BlockingQueue<short[]> queue;
    	private boolean isRunning;
    	
    	public MicPlayer(BlockingQueue<short[]> q) {
    		queue = q;
    	}
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
    	public boolean getIsRunning() {
    		return this.isRunning;
    	}
    	
		public void run() {
			isRunning = true;
			    		
			AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, 
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT,
    				DEFAULT_BUFFER_SIZE,
    				AudioTrack.MODE_STREAM);
    		
    		player.setPlaybackRate(DEFAULT_SAMPLE_RATE);
    		
    		player.play();
    		
    		while (isRunning) {
				try {
					short[] buffer = queue.take();
					AutoTalent.processSamples(buffer);
	    			player.write(buffer, 0, DEFAULT_BUFFER_SIZE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}

    		player.stop();
    		player.flush();
    		player.release();
    		player = null;
		}
    }
    
    private class MicRecorder implements Runnable {
    	private final BlockingQueue<short[]> queue;
    	private boolean isRunning;
    	    	
    	public MicRecorder(BlockingQueue<short[]> q) {
    		queue = q;
    	}
    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
    	public boolean getIsRunning() {
    		return this.isRunning;
    	}
    	
    	public void run() {
    		isRunning = true;
    		
    		AudioRecord recorder = new AudioRecord(AudioSource.MIC,
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				DEFAULT_BUFFER_SIZE);

    		Log.d(getPackageName(), String.format("recorder state: %d", recorder.getState()));
    		
    		short[] buffer= new short[DEFAULT_BUFFER_SIZE];
    		recorder.startRecording();
    		
    		while (isRunning) {
    			try {
    				recorder.read(buffer, 0, DEFAULT_BUFFER_SIZE);
					queue.put(buffer);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
    		}
    		
    		recorder.stop();
    		recorder.release();
    		recorder = null;
    	}
    }
    
    public AudioRecord getDeviceAudioRecord() {
    	AudioRecord recorder;
    	
    	return null;
    }
}