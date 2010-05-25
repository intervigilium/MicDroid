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
	private Thread micProcessorThread;
	private Thread micPlayerThread;
	private MicPlayer micPlayer;
	private MicProcessor micProcessor;
	private MicRecorder micRecorder;
	private BlockingQueue<short[]> playQueue;
	private BlockingQueue<short[]> recordQueue;
	
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
    	if (micRecorder != null) {
    		micRecorder.stopRunning();
    	}
    	if (micProcessor != null) {
    		micProcessor.stopRunning();
    	}
    	if (micPlayer != null) {
    		micPlayer.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
		
		super.onStop();
    }
    
    @Override
    public void onPause() {
    	if (micRecorder != null) {
    		micRecorder.stopRunning();
    	}
    	if (micProcessor != null) {
    		micProcessor.stopRunning();
    	}
    	if (micPlayer != null) {
    		micPlayer.stopRunning();
    	}
    	AutoTalent.destroyAutoTalent();
			
		super.onPause();
    }
    
    @Override
    public void onResume() {
    	((ToggleButton)findViewById(R.id.mic_toggle)).setChecked(false);
    	if (recordQueue != null) {
    		recordQueue.clear();
    	} else {
    		recordQueue = new LinkedBlockingQueue<short[]>();
    	}
    	if (playQueue != null) {
    		playQueue.clear();
    	} else {
    		playQueue = new LinkedBlockingQueue<short[]>();
    	}
    	
    	micRecorder = new MicRecorder(recordQueue);
    	micProcessor = new MicProcessor(recordQueue, playQueue);
    	micPlayer = new MicPlayer(playQueue);
    	
    	super.onResume();
    }
    
    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
    	public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
			if (btn.isChecked()) {
				micRecorderThread = new Thread(micRecorder, "Mic Recorder Thread");
				micRecorderThread.start();
				
				micProcessorThread = new Thread(micProcessor, "Mic Processor Thread");
				micProcessorThread.start();
	        
	        	micPlayerThread = new Thread(micPlayer, "Mic Player Thread");
	        	micPlayerThread.start();
			} else {
				micRecorder.stopRunning();
				micProcessor.stopRunning();
				micPlayer.stopRunning();
				recordQueue.clear();
				playQueue.clear();
			}
		}
    };
    
    private class MicProcessor implements Runnable {
    	private final BlockingQueue<short[]> inQueue;
    	private final BlockingQueue<short[]> outQueue;
    	private boolean isRunning;
    	
    	public MicProcessor(BlockingQueue<short[]> in, BlockingQueue<short[]> out) {
    		inQueue = in;
    		outQueue = out;
    	}
    	
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
			AutoTalent.instantiateAutoTalent(DEFAULT_SAMPLE_RATE);
    		AutoTalent.initializeAutoTalent(CONCERT_A, KEY_C_MAJOR, 0, 0.2f, 1.0f, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0.5f);

    		
    		while (isRunning) {
				try {
					short[] buffer = inQueue.take();
					AutoTalent.processSamples(buffer);
					outQueue.put(buffer);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
		}
    }
    
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
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			    		
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
					processAudioSamples(buffer);
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
		
		private void processAudioSamples(short[] buffer) {
			AutoTalent.processSamples(buffer);
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
    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    		      		
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
}