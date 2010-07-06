package com.intervigil.micdroid;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RecordingPlayer extends Activity {
	
	private static final int READ_BUFFER_SIZE = 4096;
	private String recordingName;
	private AudioTrack player;
	private WaveReader fileReader;
	
	/**
     * Called when the activity is starting.  This is where most
     * initialization should go: calling setContentView(int) to inflate
     * the activity's UI, etc.
     * 
     * @param   savedInstanceState	Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_player);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        Button playBtn = (Button)findViewById(R.id.recording_player_btn_play);
        Button stopBtn = (Button)findViewById(R.id.recording_player_btn_stop);
        Button closeBtn = (Button)findViewById(R.id.recording_player_btn_close);
        playBtn.setOnClickListener(playBtnListener);
        stopBtn.setOnClickListener(stopBtnListener);
        closeBtn.setOnClickListener(closeBtnListener);
        
        recordingName = getIntent().getExtras().getString("recordingName");
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
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }
    
    private OnClickListener playBtnListener = new OnClickListener() {	
		public void onClick(View v) {				
			try {
				fileReader = new WaveReader(getLibraryDirectory(), recordingName);
				fileReader.openWave();
			} catch (IOException e) {
				// failed to open file somehow
				// TODO: add error handling
				e.printStackTrace();
				finish();
			}
			
			Log.d("RecordingPlayer", String.format("playing file: %s, sample rate: %d, channels: %d, pcm format: %d", recordingName, fileReader.getSampleRate(), fileReader.getChannels(), fileReader.getPcmFormat()));
			
			int bufferSize = AudioRecord.getMinBufferSize(fileReader.getSampleRate(), 
					convertChannelConfig(fileReader.getChannels()), 
					convertPcmEncoding(fileReader.getPcmFormat())) * 2;
			player = new AudioTrack(AudioManager.STREAM_MUSIC, 
					fileReader.getSampleRate(), 
					convertChannelConfig(fileReader.getChannels()), 
					convertPcmEncoding(fileReader.getPcmFormat()), 
					bufferSize, 
					AudioTrack.MODE_STREAM);
			player.play();
			
			short[] buf = new short[READ_BUFFER_SIZE];
			while (true) {
				try {
					int samplesRead = fileReader.readShort(buf, READ_BUFFER_SIZE);
					if (samplesRead > 0) {
						player.write(buf, 0, samplesRead);
					} else {
						break;
					}
				} catch (IOException e) {
					// failed to read/write to wave file
					// TODO: real error handling
					e.printStackTrace();
					finish();
				}
			}
			
			try {
				fileReader.closeWaveFile();
				player.stop();
				player.flush();
				player = null;
			} catch (IOException e) {
				
			}
		}
	};
	
	private OnClickListener stopBtnListener = new OnClickListener() {	
		public void onClick(View v) {
			if (player != null) {
				if (player.getPlaybackRate() == AudioTrack.PLAYSTATE_PLAYING) {
					
					player.stop();
					player.flush();
					player = null;
					
				}
			}
		}
	};
	
	private OnClickListener closeBtnListener = new OnClickListener() {	
		public void onClick(View v) {
			finish();
		}
	};
	
	private int convertChannelConfig(int numChannels) {
		switch (numChannels) {
			case 1:
				return AudioFormat.CHANNEL_CONFIGURATION_MONO;
			case 2:
				return AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			default:
				return AudioFormat.CHANNEL_CONFIGURATION_DEFAULT;
		}
	}
	
	private int convertPcmEncoding(int bitsPerSample) {
		switch (bitsPerSample) {
			case 8:
				return AudioFormat.ENCODING_PCM_8BIT;
			case 16:
				return AudioFormat.ENCODING_PCM_16BIT;
			default:
				return AudioFormat.ENCODING_DEFAULT;
		}
	}
	
	private String getLibraryDirectory() {
    	return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName() + File.separator + "library";
    }
}
