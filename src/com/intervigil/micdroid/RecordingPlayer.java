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
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RecordingPlayer extends Activity {
	
	private String recordingName;
	private MediaPlayer mediaPlayer;
	private SeekBar mediaSeekBar;
	private RefreshHandler refreshHandler;
	
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

        recordingName = getIntent().getExtras().getString(Constants.PLAY_DATA_RECORDING_NAME);
        
        mediaSeekBar = (SeekBar)findViewById(R.id.recording_player_seekbar);
        ((TextView)findViewById(R.id.recording_player_file_name)).setText(recordingName);
        
        mediaSeekBar.setOnSeekBarChangeListener(mediaSeekListener);
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
    	
    	refreshHandler = new RefreshHandler();
    	mediaPlayer = new MediaPlayer();
    	mediaPlayer.setOnCompletionListener(playbackCompletionListener);
		try {
			FileInputStream file = new FileInputStream(((MicApplication)getApplication()).getLibraryDirectory() + File.separator + recordingName);
			mediaPlayer.setDataSource(file.getFD());
			mediaPlayer.prepare();
			file.close();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    protected void onPause() {
    	Log.i(getPackageName(), "onPause()");
    	super.onPause();
    	
    	if (mediaPlayer != null) {
    		if (mediaPlayer.isPlaying()) {
    			mediaPlayer.stop();
    		}
    		mediaPlayer.release();
    	}
    	mediaPlayer = null;
    }
    
    @Override
    protected void onStop() {
    	Log.i(getPackageName(), "onStop()");
    	super.onStop();
    	
    	if (mediaPlayer != null) {
    		if (mediaPlayer.isPlaying()) {
    			mediaPlayer.stop();
    		}
    		mediaPlayer.release();
    	}
    	mediaPlayer = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }
    
    private OnSeekBarChangeListener mediaSeekListener = new OnSeekBarChangeListener() {
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			// don't do anything yet
		}
		
		public void onStartTrackingTouch(SeekBar seekBar) {
			// don't do anything yet
		}
		
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				int progressMs = (int) ((progress/100.0) * mediaPlayer.getDuration());
				mediaPlayer.seekTo(progressMs);
			}
		}
	};
	
	private class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			updateProgressBar();
		}
		
		public void sleep(long delay) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delay);
		}
	}
	
	private OnCompletionListener playbackCompletionListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			try {
				mp.stop();
				mp.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}; 
    
    public void recordingPlayerClickHandler(View view) {
    	switch (view.getId()) {
	    	case R.id.recording_player_btn_play:
	    		if (!mediaPlayer.isPlaying()) {
	    			mediaPlayer.seekTo(0);
	    			mediaPlayer.start();
	    			//updateProgressBar();
	    		}
	    		break;
	    	case R.id.recording_player_btn_stop:
	    		if (mediaPlayer.isPlaying()) {
	    			try {
	    				mediaPlayer.stop();
						mediaPlayer.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    		break;
	    	case R.id.recording_player_btn_delete:
	    		if (mediaPlayer.isPlaying()) {
	    			mediaPlayer.stop();
	    			mediaPlayer.release();
	    			mediaPlayer = null;
	    		}
    			File toDelete = new File(((MicApplication)getApplication()).getLibraryDirectory() + File.separator + recordingName);
    			toDelete.delete();

    			setResult(Constants.RESULT_FILE_DELETED);
    			finish();
	    		break;
	    	case R.id.recording_player_btn_close:
	    		if (mediaPlayer.isPlaying()) {
	    			mediaPlayer.stop();
	    			mediaPlayer.release();
	    			mediaPlayer = null;
	    		}
				finish();
	    		break;
    		default:
    			break;
    	}
    }
    
    private void updateProgressBar() {
    	mediaSeekBar.setProgress((int) (mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration() * 100.00));
    	refreshHandler.sleep(100);
    }
}
