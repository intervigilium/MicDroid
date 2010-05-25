package com.intervigil.micdroid;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Mic extends Activity {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final int DEFAULT_SAMPLE_RATE = 22050;
	private static final float CONCERT_A = 440.0f;
	private static final char KEY_C_MAJOR = 'c';
	
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
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				// TODO: make most of these autotalent options configurable
				AutoTalent.instantiateAutoTalent(DEFAULT_SAMPLE_RATE);
	    		AutoTalent.initializeAutoTalent(CONCERT_A, KEY_C_MAJOR, 0, 0.2f, 1.0f, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0.5f);

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