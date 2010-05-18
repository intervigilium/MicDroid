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
	
	
	private static final int DEFAULT_SAMPLE_RATE = 22050;
//	private static final int[] KEY_C_MAJOR = new int[] { 1, -1, 1, 1, -1, 1, -1, 1, 1, -1, 1, -1 };
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
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				AutoTalent.initializeAutoTalent(KEY_C_MAJOR, 0, 0.2f, 1.0f, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0.5f);
				micRunner = new MicRunner();
		        micRunnerThread = new Thread(micRunner, "Mic Runner Thread");
		        
		        micRunnerThread.start();
			} else {
				try {
					// try to destroy autotalent instance created here
					
					micRunner.stopRunning();
					micRunnerThread.join();
				} catch (InterruptedException e) {
					Log.e(getPackageName(), "Thread interrupted during join!", e);
					e.printStackTrace();
				}
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
    		    		
//    		int bufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, 
//    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
//    				AudioFormat.ENCODING_PCM_16BIT) * 8;
    		int bufferSize = 4096;
    		
    		recorder = new AudioRecord(AudioSource.MIC, 
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				bufferSize);
    		
    		player = new AudioTrack(AudioManager.STREAM_MUSIC, 
    				DEFAULT_SAMPLE_RATE, 
    				AudioFormat.CHANNEL_CONFIGURATION_MONO, 
    				AudioFormat.ENCODING_PCM_16BIT, 
    				bufferSize, 
    				AudioTrack.MODE_STREAM);
    		
    		player.setPlaybackRate(DEFAULT_SAMPLE_RATE);
    		
    		short[] playbackBuffer= new short[bufferSize];
    		recorder.startRecording();
    		player.play();
    		
    		while (isRunning) {
    			recorder.read(playbackBuffer, 0, bufferSize);
    			processAudioSamples(playbackBuffer);
    			player.write(playbackBuffer, 0, playbackBuffer.length);
    		}
    		
    		player.stop();
    		player.flush();
    		player.release();
    		
    		recorder.stop();
    		recorder.release();
    	}
    	
    	private void processAudioSamples(short[] buffer) {
    		buffer = AutoTalent.processSamples(buffer);
    	}
    }
}