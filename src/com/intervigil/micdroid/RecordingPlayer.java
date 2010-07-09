/* RecordingPlayer.java
   Simple wave file player

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

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class RecordingPlayer extends Activity {
	
	private static final int READ_BUFFER_SIZE = 4096;
	private String recordingName;
	private MediaPlayer mediaPlayer;
	private Thread mediaPlayerThread;
	
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

        this.recordingName = getIntent().getExtras().getString(Constants.PLAY_DATA_RECORDING_NAME);
        
        ((TextView)findViewById(R.id.recording_player_file_name)).setText(recordingName);
        
        mediaPlayer = new MediaPlayer(recordingName);
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
    
    public void recordingPlayerClickHandler(View view) {
    	switch (view.getId()) {
	    	case R.id.recording_player_btn_play:
	    		if (!mediaPlayer.isRunning()) {
					mediaPlayerThread = new Thread(mediaPlayer, "Recording Player Thread");
					mediaPlayerThread.start();
				}
	    		break;
	    	case R.id.recording_player_btn_stop:
	    		if (mediaPlayer.isRunning()) {
					try {
						mediaPlayer.stopRunning();
						mediaPlayerThread.join();
						mediaPlayerThread = null;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
	    		break;
	    	case R.id.recording_player_btn_delete:
    			if (mediaPlayer.isRunning()) {
    				try {
    					mediaPlayer.stopRunning();
    					mediaPlayerThread.join();
    					mediaPlayerThread = null;
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    			File toDelete = new File(((MicApplication)getApplication()).getLibraryDirectory() + File.separator + recordingName);
    			toDelete.delete();

    			setResult(Constants.RESULT_FILE_DELETED);
    			finish();
	    		break;
	    	case R.id.recording_player_btn_close:
	    		if (mediaPlayer.isRunning()) {
					try {
						mediaPlayer.stopRunning();
						mediaPlayerThread.join();
						mediaPlayerThread = null;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				finish();
	    		break;
    		default:
    			break;
    	}
    }
	
	private class MediaPlayer implements Runnable {
    	private boolean isRunning;
    	private String recording;
    	private AudioTrack player;
    	private WaveReader reader;
    	    	
    	public MediaPlayer(String recording) {
    		this.recording = recording;
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
				reader = new WaveReader(((MicApplication)getApplication()).getLibraryDirectory(), recording);
				reader.openWave();
			} catch (IOException e) {
				// failed to open file somehow
				// TODO: add error handling
				e.printStackTrace();
			}
			
			int bufferSize = AudioTrack.getMinBufferSize(reader.getSampleRate(), 
					AudioHelper.convertChannelConfig(reader.getChannels()), 
					AudioHelper.convertPcmEncoding(reader.getPcmFormat())) * 2;
			player = new AudioTrack(AudioManager.STREAM_MUSIC, 
					reader.getSampleRate(), 
					AudioHelper.convertChannelConfig(reader.getChannels()), 
					AudioHelper.convertPcmEncoding(reader.getPcmFormat()), 
					bufferSize, 
					AudioTrack.MODE_STREAM);
			player.play();
			
			short[] buf = new short[READ_BUFFER_SIZE];
			while (isRunning) {
				try {
					int samplesRead = reader.readShort(buf, READ_BUFFER_SIZE);
					if (samplesRead > 0) {
						player.write(buf, 0, samplesRead);
					} else {
						break;
					}
				} catch (IOException e) {
					// failed to read/write to wave file
					// TODO: real error handling
					e.printStackTrace();
					break;
				}
			}
			
			try {
				reader.closeWaveFile();
				reader = null;
				player.stop();
				player.flush();
				player = null;
				isRunning = false;
			} catch (IOException e) {
				e.printStackTrace();
				// TODO: real error handling
			}
    	}
    }
}
