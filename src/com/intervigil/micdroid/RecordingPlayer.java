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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class RecordingPlayer extends Activity {
	
	private static final int SEEKBAR_RESOLUTION = 1000;
	
	private String recordingName;
	private SeekableMediaPlayer mediaPlayer;
	private SeekBar mediaSeekBar;
	
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
        
        mediaSeekBar.setMax(SEEKBAR_RESOLUTION);
        mediaPlayer = new SeekableMediaPlayer(((MicApplication)getApplication()).getLibraryDirectory() + File.separator + recordingName, mediaSeekBar); 
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
    	
    	if (mediaPlayer != null) {
    		mediaPlayer.close();
    	}
    	mediaPlayer = null;
    }
    
    @Override
    protected void onStop() {
    	Log.i(getPackageName(), "onStop()");
    	super.onStop();
    	
    	if (mediaPlayer != null) {
    		mediaPlayer.close();
    	}
    	mediaPlayer = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    public void recordingPlayerClickHandler(View view) {
    	switch (view.getId()) {
	    	case R.id.recording_player_btn_play:
	    		mediaPlayer.play();
	    		break;
	    	case R.id.recording_player_btn_stop:
	    		mediaPlayer.stop();
	    		break;
	    	case R.id.recording_player_btn_delete:
	    		mediaPlayer.close();
    			File toDelete = new File(((MicApplication)getApplication()).getLibraryDirectory() + File.separator + recordingName);
    			toDelete.delete();

    			setResult(Constants.RESULT_FILE_DELETED);
    			finish();
	    		break;
	    	case R.id.recording_player_btn_close:
				finish();
	    		break;
    		default:
    			break;
    	}
    }
}
