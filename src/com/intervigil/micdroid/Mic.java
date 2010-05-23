package com.intervigil.micdroid;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Mic extends Activity {
	
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final int DEFAULT_SAMPLE_RATE = 22050;
	private static final float CONCERT_A = 440.0f;
	private static final char KEY_C_MAJOR = 'c';
	
	private Thread micRunnerThread;
	private MicRunner micRunner;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ToggleButton powerBtn = (ToggleButton)findViewById(R.id.mic_toggle);
        powerBtn.setOnCheckedChangeListener(mPowerBtnListener);
    }
    
    @Override
    public void onStop() {
    	if (micRunner != null) {
    		micRunner.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
		
		super.onStop();
    }
    
    @Override
    public void onPause() {
    	if (micRunner != null) {
    		micRunner.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
			
		super.onPause();
    }
    
    @Override
    public void onResume() {
    	((ToggleButton)findViewById(R.id.mic_toggle)).setChecked(false);
    	
    	AutoTalent.instantiateAutoTalent(DEFAULT_SAMPLE_RATE);
    	
    	super.onResume();
    }
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				micRunner = new MicRunner();
		        micRunnerThread = new Thread(micRunner, "Mic Runner Thread");      
		        micRunnerThread.start();
			} else {
				micRunner.stopRunning();
				micRunnerThread = null;
				micRunner = null;
			}
		}
    };
    
    private class MicRunner implements Runnable {
    	private AudioRecord recorder;
    	private AudioTrack player;
    	private boolean isRunning;
    	    	
    	public void stopRunning() {
    		this.isRunning = false;
    	}
    	
    	public boolean getIsRunning() {
    		return this.isRunning;
    	}
    	
    	public void run() {
    		isRunning = true;
    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    		    
    		// TODO: make most of these autotalent options configurable
    		AutoTalent.initializeAutoTalent(CONCERT_A, KEY_C_MAJOR, 0, 0.2f, 1.0f, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0.5f);
    		
    		recorder = new AudioRecord(AudioSource.MIC,
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				DEFAULT_BUFFER_SIZE);
    		
    		player = new AudioTrack(AudioManager.STREAM_MUSIC, 
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				DEFAULT_BUFFER_SIZE, 
    				AudioTrack.MODE_STREAM);
    		
    		player.setPlaybackRate(DEFAULT_SAMPLE_RATE);
    		
    		short[] playbackBuffer= new short[DEFAULT_BUFFER_SIZE];
    		recorder.startRecording();
    		player.play();
    		
    		while (isRunning) {
    			// TODO: split this into two separate threads, one for read and one for write
    			recorder.read(playbackBuffer, 0, DEFAULT_BUFFER_SIZE);
    			processAudioSamples(playbackBuffer);
    			player.write(playbackBuffer, 0, DEFAULT_BUFFER_SIZE);
    		}
    		
    		playbackBuffer = null;
    		
    		player.stop();
    		player.flush();
    		player.release();
    		player = null;
    		
    		recorder.stop();
    		recorder.release();
    		recorder = null;
    	}
    	
    	private void processAudioSamples(short[] buffer) {
    		 AutoTalent.processSamples(buffer);
    	}
    }
}