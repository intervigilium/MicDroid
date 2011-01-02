/* Mic.java
   An auto-tune app for Android

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.HeadsetHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.UpdateHelper;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.micdroid.pitch.AutoTalent;
import com.intervigil.wave.WaveReader;
import com.intervigil.wave.WaveWriter;

public class Mic extends Activity {

    private static final int AUTOTALENT_CHUNK_SIZE = 8192;

    private static final float CONCERT_A = 440.0f;

    private static final int DEFAULT_SCALE_ROTATE = 0;
    private static final float DEFAULT_FIXED_PITCH = 0.0f;
    private static final float DEFAULT_LFO_DEPTH = 0.0f;
    private static final float DEFAULT_LFO_RATE = 5.0f;
    private static final float DEFAULT_LFO_SHAPE = 0.0f;
    private static final float DEFAULT_LFO_SYM = 0.0f;
    private static final int DEFAULT_LFO_QUANT = 0;

    private WakeLock wakeLock;
    private Recorder recorder;
    private Timer timer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Typeface timerFont = Typeface.createFromAsset(getAssets(),
                "fonts/Clockopia.ttf");
        ToggleButton recordingButton = ((ToggleButton) findViewById(R.id.recording_button));
        Button libraryButton = ((Button) findViewById(R.id.library_button));
        TextView timerDisplay = (TextView) findViewById(R.id.recording_timer);

        recordingButton.setChecked(false);
        recordingButton.setOnCheckedChangeListener(mPowerBtnListener);
        libraryButton.setOnClickListener(mLibraryBtnListener);
        timerDisplay.setTypeface(timerFont);

        timer = new Timer(timerDisplay);

        if (PreferenceHelper.getScreenLock(Mic.this)) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    "recordingWakeLock");
        }

        if (UpdateHelper.isAppUpdated(Mic.this)) {
            UpdateHelper.onAppUpdate(Mic.this);
        } else {
            AudioHelper.configureRecorder(Mic.this);
        }
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
        if (PreferenceHelper.getScreenLock(Mic.this)) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        Log.i(getPackageName(), "onPause()");
        super.onPause();
        if (PreferenceHelper.getScreenLock(Mic.this)) {
            wakeLock.release();
        }
    }

    @Override
    protected void onStop() {
        Log.i(getPackageName(), "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(getPackageName(), "onDestroy()");
        super.onStop();

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (recorder != null) {
            recorder.cleanup();
        }
        AutoTalent.destroyAutoTalent();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(getPackageName(), "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(getPackageName(), "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.main);

        boolean isRecording = recorder != null ? recorder.isRunning() : false;

        ((Button) findViewById(R.id.library_button))
                .setOnClickListener(mLibraryBtnListener);
        ToggleButton micSwitch = (ToggleButton) findViewById(R.id.recording_button);
        micSwitch.setChecked(isRecording);
        micSwitch.setOnCheckedChangeListener(mPowerBtnListener);

        Typeface timerFont = Typeface.createFromAsset(getAssets(),
                "fonts/Clockopia.ttf");
        TextView timerDisplay = (TextView) findViewById(R.id.recording_timer);
        timerDisplay.setTypeface(timerFont);
        timer.registerDisplay(timerDisplay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options:
            Intent preferencesIntent = new Intent(getBaseContext(),
                    Preferences.class);
            startActivityForResult(preferencesIntent,
                    Constants.INTENT_PREFERENCES);
            break;
        case R.id.donate:
            Intent marketSearchIntent = new Intent(Intent.ACTION_SEARCH);
            marketSearchIntent.setPackage("com.android.vending");
            marketSearchIntent.putExtra("query", "micdroid donate");
            marketSearchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(marketSearchIntent);
            break;
        case R.id.help:
            DialogHelper.showWarning(Mic.this, R.string.help_title,
                    R.string.help_text);
            break;
        case R.id.about:
            DialogHelper.showWarning(Mic.this, R.string.about_title,
                    R.string.about_text);
            break;
        case R.id.quit:
            finish();
            break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case Constants.INTENT_FILENAME_ENTRY:
            if (resultCode == Activity.RESULT_OK) {
                String fileName = data.getStringExtra(
                        Constants.INTENT_EXTRA_FILE_NAME).trim()
                        + ".wav";
                new ProcessAutotalentTask().execute(fileName);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(Mic.this, R.string.recording_save_canceled,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        case Constants.INTENT_PREFERENCES:
            if (recorder != null && !recorder.isRunning()) {
                recorder.cleanup();
                recorder = null;
            }
            break;
        default:
            break;
        }
    }

    private Handler recordingErrorHandler = new Handler() {
        // use the handler to receive error messages from the recorder object
        @Override
        public void handleMessage(Message msg) {
            ToggleButton recordingButton = (ToggleButton) findViewById(R.id.recording_button);

            if (recorder != null) {
                recorder.cleanup();
                recorder = null;
            }
            timer.stop();
            recordingButton.setChecked(false);

            switch (msg.what) {
            case Constants.WRITER_OUT_OF_SPACE:
                // received error that the writer is out of SD card space
                DialogHelper.showWarning(Mic.this,
                        R.string.writer_out_of_space_title,
                        R.string.writer_out_of_space_warning);
                break;
            case Constants.UNABLE_TO_CREATE_RECORDING:
                // received error that the writer couldn't create the recording
                DialogHelper.showWarning(Mic.this,
                        R.string.unable_to_create_recording_title,
                        R.string.unable_to_create_recording_warning);
                break;
            }
        }
    };

    private class ProcessAutotalentTask extends AsyncTask<String, Void, Void> {
        private WaveReader reader;
        private WaveWriter writer;
        private ProgressDialog spinner;
        private boolean isLiveMode;

        public ProcessAutotalentTask() {
            spinner = new ProgressDialog(Mic.this);
            spinner.setCancelable(false);
            isLiveMode = PreferenceHelper.getLiveMode(Mic.this);
        }

        @Override
        protected void onPreExecute() {
            if (isLiveMode) {
                spinner.setMessage(getString(R.string.saving_recording_progress_msg));
            } else {
                spinner.setMessage(getString(R.string.autotalent_progress_msg));
            }
            spinner.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            // maybe ugly but we only pass one string in anyway
            String fileName = params[0];

            if (isLiveMode) {
                int len;
                InputStream in;
                OutputStream out;
                byte[] buf = new byte[1024];

                File src = new File(
                        getCacheDir().getAbsolutePath()
                        + File.separator
                        + getString(R.string.default_recording_name));
                File dst = new File(
                        ApplicationHelper.getLibraryDirectory()
                        + File.separator
                        + fileName);
                // do a file copy since renameto doesn't work
                try {
                    in = new FileInputStream(src);
                    out = new FileOutputStream(dst);
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = recordingErrorHandler
                            .obtainMessage(Constants.UNABLE_TO_CREATE_RECORDING);
                    recordingErrorHandler.sendMessage(msg);
                    return null;
                }
            } else {
                try {
                    reader = new WaveReader(
                            getCacheDir().getAbsolutePath(),
                            getString(R.string.default_recording_name));
                    reader.openWave();
                    writer = new WaveWriter(
                            ApplicationHelper.getLibraryDirectory(),
                            fileName,
                            reader.getSampleRate(),
                            reader.getChannels(),
                            reader.getPcmFormat());
                    writer.createWaveFile();
                } catch (IOException e) {
                    // can't create our readers and writers for some reason!
                    e.printStackTrace();
                    Message msg = recordingErrorHandler
                            .obtainMessage(Constants.UNABLE_TO_CREATE_RECORDING);
                    recordingErrorHandler.sendMessage(msg);
                    return null;
                }

                updateAutoTalentPreferences();

                short[] buf = new short[AUTOTALENT_CHUNK_SIZE];
                while (true) {
                    try {
                        int samplesRead = reader.read(buf,
                                AUTOTALENT_CHUNK_SIZE);
                        if (samplesRead > 0) {
                            AutoTalent.processSamples(buf, samplesRead);
                            writer.write(buf, 0, samplesRead);
                        } else {
                            break;
                        }
                    } catch (IOException e) {
                        // failed to read/write to wave file
                        e.printStackTrace();
                        Message msg = recordingErrorHandler
                                .obtainMessage(Constants.WRITER_OUT_OF_SPACE);
                        recordingErrorHandler.sendMessage(msg);
                        break;
                    }
                }

                try {
                    reader.closeWaveFile();
                    writer.closeWaveFile();
                    AutoTalent.destroyAutoTalent();
                } catch (IOException e) {
                    // failed to close out our files correctly
                    // TODO: real error handling
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            spinner.dismiss();
            Toast.makeText(Mic.this, R.string.recording_save_success,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private OnClickListener mLibraryBtnListener = new OnClickListener() {
        public void onClick(View v) {
            Intent libraryIntent = new Intent(getBaseContext(),
                    RecordingLibrary.class);
            startActivity(libraryIntent);
        }
    };

    private OnCheckedChangeListener mPowerBtnListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
            if (!hasWindowFocus()) {
                return;
            } else if (!canWriteToSdCard()) {
                btn.setChecked(false);
                DialogHelper.showWarning(Mic.this,
                        R.string.no_external_storage_title,
                        R.string.no_external_storage_warning);
            } else if (!AudioHelper.isValidRecorderConfiguration(Mic.this)) {
                btn.setChecked(false);
                DialogHelper.showWarning(Mic.this,
                        R.string.unconfigured_audio_title,
                        R.string.unconfigured_audio_warning);
            } else {
                if (btn.isChecked()) {
                    boolean isLiveMode = PreferenceHelper.getLiveMode(Mic.this);
                    if (isLiveMode
                            && !HeadsetHelper.isHeadsetPluggedIn(Mic.this)) {
                        btn.setChecked(false);
                        DialogHelper.showWarning(Mic.this,
                                R.string.no_headset_plugged_in_title,
                                R.string.no_headset_plugged_in_warning);
                    } else {
                        timer.reset();
                        if (isLiveMode) {
                            updateAutoTalentPreferences();
                        }
                        if (recorder == null) {
                            try {
                                recorder = new SipdroidRecorder(Mic.this,
                                        recordingErrorHandler, isLiveMode);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                btn.setChecked(false);
                                DialogHelper.showWarning(Mic.this,
                                        R.string.audio_record_exception_title,
                                        R.string.audio_record_exception_warning);
                                return;
                            }
                        }
                        try {
                            recorder.start();
                            timer.start();
                            Toast.makeText(getBaseContext(),
                                    R.string.recording_started_toast,
                                    Toast.LENGTH_SHORT).show();
                        } catch (IllegalStateException e) {
                            btn.setChecked(false);
                            recorder.cleanup();
                            DialogHelper.showWarning(Mic.this,
                                    R.string.audio_record_exception_title,
                                    R.string.audio_record_exception_warning);
                            return;
                        } catch (FileNotFoundException e) {
                            btn.setChecked(false);
                            recorder.cleanup();
                            DialogHelper.showWarning(Mic.this,
                                    R.string.instrumental_not_found_title,
                                    R.string.instrumental_not_found_warning);
                            return;
                        } catch (IOException e) {
                            btn.setChecked(false);
                            recorder.cleanup();
                            DialogHelper.showWarning(Mic.this,
                                    R.string.unable_to_create_recording_title,
                                    R.string.unable_to_create_recording_warning);
                            return;
                        }
                    }
                } else {
                    if (recorder != null && recorder.isRunning()) {
                        // only do this if it was running, otherwise an error
                        // message triggered the check state change
                        recorder.stop();
                        timer.stop();
                        Toast.makeText(getBaseContext(),
                                R.string.recording_finished_toast,
                                Toast.LENGTH_SHORT).show();
                        Intent saveFileIntent = new Intent(getBaseContext(),
                                FileNameEntry.class);
                        startActivityForResult(saveFileIntent,
                                Constants.INTENT_FILENAME_ENTRY);
                    }
                }
            }
        }
    };

    private void updateAutoTalentPreferences() {
        char key = PreferenceHelper.getKey(Mic.this);
        float fixedPull = PreferenceHelper.getPullToFixedPitch(Mic.this);
        float pitchShift = PreferenceHelper.getPitchShift(Mic.this);
        float strength = PreferenceHelper.getCorrectionStrength(Mic.this);
        float smooth = PreferenceHelper.getCorrectionSmoothness(Mic.this);
        int formantCorrection = PreferenceHelper.getFormantCorrection(Mic.this) ? 1
                : 0;
        float formantWarp = PreferenceHelper.getFormantWarp(Mic.this);
        float mix = PreferenceHelper.getMix(Mic.this);

        AutoTalent.instantiateAutoTalent(PreferenceHelper
                .getSampleRate(Mic.this));
        AutoTalent.initializeAutoTalent(CONCERT_A, key, DEFAULT_FIXED_PITCH,
                fixedPull, strength, smooth, pitchShift, DEFAULT_SCALE_ROTATE,
                DEFAULT_LFO_DEPTH, DEFAULT_LFO_RATE, DEFAULT_LFO_SHAPE,
                DEFAULT_LFO_SYM, DEFAULT_LFO_QUANT, formantCorrection,
                formantWarp, mix);
    }

    private static boolean canWriteToSdCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
