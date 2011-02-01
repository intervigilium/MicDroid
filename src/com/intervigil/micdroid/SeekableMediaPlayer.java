/* SeekableMediaPlayer.java

   Copyright (c) 2010 Ethan Chen

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
            // mediaPlayer seems to like file descriptors a lot more than giving
            // it a file path
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

    public void bindSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        this.seekBar.setOnSeekBarChangeListener(mediaSeekListener);
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

        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            if (fromUser) {
                int progressMs = (progress * mediaPlayer.getDuration())
                        / seekBar.getMax();
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
        if (mediaPlayer != null && mediaPlayer.getDuration() > 0) {
            int progress = (mediaPlayer.getCurrentPosition() * seekBar.getMax())
                    / mediaPlayer.getDuration();
            seekBar.setProgress((int) progress);
        }
        return DEFAULT_INCREMENT;
    }
}
