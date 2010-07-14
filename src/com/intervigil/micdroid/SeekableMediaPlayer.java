/* SeekableMediaPlayer.java

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

import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekableMediaPlayer {

	private static final int REFRESH = 1;
	private static final int DEFAULT_INCREMENT = 500;
	
	private MediaPlayer mediaPlayer;
	private SeekBar seekBar;
	private RefreshHandler refreshHandler;

	private boolean isPaused;
	
	public SeekableMediaPlayer(String filePath, SeekBar seekBar) {
		this.seekBar = seekBar;
		this.seekBar.setOnSeekBarChangeListener(mediaSeekListener);
		
		refreshHandler = new RefreshHandler();
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(playbackCompletionListener);
		
		try {
			// mediaPlayer seems to like file descriptors a lot more than giving it a file path
			FileInputStream file = new FileInputStream(filePath);
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
		
		isPaused = false;
	}
	
	public void play() {
		if (!mediaPlayer.isPlaying() || isPaused) {
			mediaPlayer.start();

			isPaused = false;
			
			long refreshDelay = onRefresh();
			queueNextRefresh(refreshDelay);
		}
	}
	
	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			
			isPaused = true;
		}
	}
	
	public void stop() {
		if (mediaPlayer.isPlaying() || isPaused) {
			try {
				mediaPlayer.stop();
				mediaPlayer.prepare();
				mediaPlayer.seekTo(0);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			isPaused = false;
		}
	}
	
	public void close() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.release();
		}
		mediaPlayer = null;
		
		refreshHandler.removeMessages(REFRESH);
		
		isPaused = true;
	}
	
	private OnSeekBarChangeListener mediaSeekListener = new OnSeekBarChangeListener() {
    	
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (isPaused) {
				play();
			}
		}
		
		public void onStartTrackingTouch(SeekBar seekBar) {
			if (mediaPlayer.isPlaying()) {
				pause();
			}
		}
		
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				int progressMs = (progress * mediaPlayer.getDuration())/seekBar.getMax();
				mediaPlayer.seekTo(progressMs);
			}
		}
	};
	
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
	
	private class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REFRESH:
					long delay = onRefresh();
					queueNextRefresh(delay);
					break;
				default:
					break;
			}
		}
	}
	
	private void queueNextRefresh(long delay) {
    	if (mediaPlayer.isPlaying()) {
    		Message msg = refreshHandler.obtainMessage(REFRESH);
    		refreshHandler.removeMessages(REFRESH);
    		refreshHandler.sendMessageDelayed(msg, delay);
    	}
    }
    
    private long onRefresh() {
    	if (mediaPlayer != null) {
    		int progress = (mediaPlayer.getCurrentPosition() * seekBar.getMax())/mediaPlayer.getDuration();
    		seekBar.setProgress((int)progress);
    	}
    	return DEFAULT_INCREMENT;
    }
}
