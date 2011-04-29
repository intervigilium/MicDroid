/* RecordingPlayer.java
   Simple wave file player

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

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.intervigil.micdroid.model.Recording;

public class RecordingPlayer extends Activity implements OnClickListener {

    private static final String CLASS_RECORDING_PLAYER = "RecordingPlayer";
    private static final int SEEKBAR_RESOLUTION = 1000;

    private Recording recording;
    private SeekableMediaPlayer mediaPlayer;
    private SeekBar mediaSeekBar;

    /**
     * Called when the activity is starting. This is where most initialization
     * should go: calling setContentView(int) to inflate the activity's UI, etc.
     * 
     * @param savedInstanceState
     *            Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_player);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        recording = getIntent().getExtras().getParcelable(
                Constants.INTENT_EXTRA_RECORDING);

        ((Button) findViewById(R.id.recording_player_btn_play)).setOnClickListener(this);
        ((Button) findViewById(R.id.recording_player_btn_stop)).setOnClickListener(this);
        ((Button) findViewById(R.id.recording_player_btn_close)).setOnClickListener(this);

        mediaSeekBar = (SeekBar) findViewById(R.id.recording_player_seekbar);
        ((TextView) findViewById(R.id.recording_player_file_name))
                .setText(recording.getName());

        mediaSeekBar.setMax(SEEKBAR_RESOLUTION);
        mediaPlayer = new SeekableMediaPlayer(recording.getAbsolutePath(),
                mediaSeekBar);
    }

    @Override
    protected void onStart() {
        Log.i(CLASS_RECORDING_PLAYER, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(CLASS_RECORDING_PLAYER, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(CLASS_RECORDING_PLAYER, "onPause()");
        super.onPause();

        if (isFinishing()) {
            if (mediaPlayer != null) {
                mediaPlayer.close();
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        Log.i(CLASS_RECORDING_PLAYER, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(CLASS_RECORDING_PLAYER, "onDestroy()");
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.close();
        }
        mediaPlayer = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(CLASS_RECORDING_PLAYER, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(CLASS_RECORDING_PLAYER, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.recording_player);

        mediaSeekBar = (SeekBar) findViewById(R.id.recording_player_seekbar);
        ((TextView) findViewById(R.id.recording_player_file_name))
                .setText(recording.getName());

        mediaSeekBar.setMax(SEEKBAR_RESOLUTION);
        mediaPlayer.bindSeekBar(mediaSeekBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recording_player_btn_play:
                mediaPlayer.play();
                break;
            case R.id.recording_player_btn_stop:
                mediaPlayer.stop();
                break;
            case R.id.recording_player_btn_close:
                finish();
                break;
            default:
                break;
        }
    }
}
