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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.HeadsetHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.UpdateHelper;
import com.intervigil.micdroid.interfaces.DependentTask;
import com.intervigil.micdroid.interfaces.Recorder;
import com.intervigil.micdroid.recorder.SipdroidRecorder;

import net.sourceforge.autotalent.Autotalent;

public class MicActivity extends Activity implements OnClickListener {

    private static final String TAG = "MicActivity";

    private static final float CONCERT_A = 440.0f;

    private static final int DEFAULT_SCALE_ROTATE = 0;
    private static final float DEFAULT_FIXED_PITCH = 0.0f;
    private static final float DEFAULT_LFO_DEPTH = 0.0f;
    private static final float DEFAULT_LFO_RATE = 5.0f;
    private static final float DEFAULT_LFO_SHAPE = 0.0f;
    private static final float DEFAULT_LFO_SYM = 0.0f;
    private static final int DEFAULT_LFO_QUANT = 0;

    private Context mContext;
    private Recorder mRecorder;
    private TimerDisplay mTimerDisplay;
    private ToggleButton mRecordButton;
    private AutotalentTask mAutotalentTask;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mContext = MicActivity.this;

        Typeface timerFont = Typeface.createFromAsset(getAssets(),
                "fonts/Clockopia.ttf");
        mRecordButton = ((ToggleButton) findViewById(R.id.recording_button));
        Button libraryButton = ((Button) findViewById(R.id.library_button));
        TextView timerDisplay = (TextView) findViewById(R.id.recording_timer);

        mRecordButton.setChecked(false);
        mRecordButton.setOnCheckedChangeListener(recordBtnListener);
        libraryButton.setOnClickListener(this);

        timerDisplay.setTypeface(timerFont);

        mTimerDisplay = new TimerDisplay();
        mTimerDisplay.registerDisplay(timerDisplay);

        mAutotalentTask = new AutotalentTask(mContext, postAutotalentTask);

        if (PreferenceHelper.getScreenLock(mContext)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mScreenLockListener);

        if (UpdateHelper.isAppUpdated(mContext)) {
            UpdateHelper.onAppUpdate(mContext);
        } else {
            AudioHelper.configureRecorder(mContext);
        }
    }

    @Override
    protected void onDestroy() {
        Autotalent.destroyAutotalent();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.main);

        boolean isRecording = mRecorder != null && mRecorder.isRunning();

        findViewById(R.id.library_button).setOnClickListener(this);
        ToggleButton micSwitch = (ToggleButton) findViewById(R.id.recording_button);
        micSwitch.setChecked(isRecording);
        micSwitch.setOnCheckedChangeListener(recordBtnListener);

        Typeface timerFont = Typeface.createFromAsset(getAssets(),
                "fonts/Clockopia.ttf");
        TextView timerDisplay = (TextView) findViewById(R.id.recording_timer);
        timerDisplay.setTypeface(timerFont);
        mTimerDisplay.registerDisplay(timerDisplay);
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
                        SettingsActivity.class);
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
                DialogHelper.showWarning(mContext, R.string.help_title,
                        R.string.help_text);
                break;
            case R.id.about:
                DialogHelper.showWarning(mContext, R.string.about_title,
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
                    updateAutoTalentPreferences();
                    mAutotalentTask.runAutotalentTask(fileName);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(mContext, R.string.recording_save_canceled,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.INTENT_PREFERENCES:
                if (mRecorder != null && !mRecorder.isRunning()) {
                    mRecorder.cleanup();
                    mRecorder = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.library_button:
                Intent libraryIntent = new Intent(getBaseContext(), LibraryActivity.class);
                startActivity(libraryIntent);
                break;
        }
    }

    private OnCheckedChangeListener recordBtnListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
            if (!hasWindowFocus()) {
                return;
            } else if (!canWriteToSdCard()) {
                btn.setChecked(false);
                DialogHelper.showWarning(mContext,
                        R.string.no_external_storage_title,
                        R.string.no_external_storage_warning);
            } else if (!AudioHelper.isValidRecorderConfiguration(mContext)) {
                btn.setChecked(false);
                DialogHelper.showWarning(mContext,
                        R.string.unconfigured_audio_title,
                        R.string.unconfigured_audio_warning);
            } else {
                if (btn.isChecked()) {
                    boolean isLiveMode = PreferenceHelper.getLiveMode(mContext);
                    if (isLiveMode
                            && !HeadsetHelper.isHeadsetPluggedIn(mContext)) {
                        btn.setChecked(false);
                        DialogHelper.showWarning(mContext,
                                R.string.no_headset_plugged_in_title,
                                R.string.no_headset_plugged_in_warning);
                    } else {
                        mTimerDisplay.reset();
                        if (isLiveMode) {
                            updateAutoTalentPreferences();
                        }
                        if (mRecorder == null) {
                            mRecorder = new SipdroidRecorder(mContext, postRecordTask, isLiveMode);
                        }
                        mRecorder.start();
                        mTimerDisplay.start();
                    }
                } else {
                    if (mRecorder != null && mRecorder.isRunning()) {
                        // only do this if it was running, otherwise an error
                        // message triggered the check state change
                        mRecorder.stop();
                        mTimerDisplay.stop();
                    }
                }
            }
        }
    };

    DependentTask postAutotalentTask = new DependentTask() {
        @Override
        public void handleError() {
            Autotalent.destroyAutotalent();
        }

        @Override
        public void doTask() {
            Autotalent.destroyAutotalent();
            Toast.makeText(mContext, R.string.recording_save_success,
                    Toast.LENGTH_SHORT).show();
        }
    };
    DependentTask postRecordTask = new DependentTask() {
        @Override
        public void doTask() {
            if (PreferenceHelper.getLiveMode(mContext)) {
                Autotalent.destroyAutotalent();
            }
            Toast.makeText(getBaseContext(),
                    R.string.recording_finished_toast,
                    Toast.LENGTH_SHORT).show();
            Intent saveFileIntent = new Intent(getBaseContext(),
                    FileNameEntry.class);
            startActivityForResult(saveFileIntent,
                    Constants.INTENT_FILENAME_ENTRY);
        }

        @Override
        public void handleError() {
            if (PreferenceHelper.getLiveMode(mContext)) {
                Autotalent.destroyAutotalent();
            }
            mRecordButton.setOnCheckedChangeListener(null);
            mRecordButton.setChecked(false);
            mRecordButton.setOnCheckedChangeListener(recordBtnListener);
        }
    };

    private void updateAutoTalentPreferences() {
        char key = PreferenceHelper.getKey(mContext);
        float fixedPull = PreferenceHelper.getPullToFixedPitch(mContext);
        float pitchShift = PreferenceHelper.getPitchShift(mContext);
        float strength = PreferenceHelper.getCorrectionStrength(mContext);
        float smooth = PreferenceHelper.getCorrectionSmoothness(mContext);
        int formantCorrection = PreferenceHelper.getFormantCorrection(mContext) ? 1 : 0;
        float formantWarp = PreferenceHelper.getFormantWarp(mContext);
        float mix = PreferenceHelper.getMix(mContext);

        Autotalent.instantiateAutotalent(PreferenceHelper.getSampleRate(mContext));
        Autotalent.setKey(key);
        Autotalent.setConcertA(CONCERT_A);
        Autotalent.setFixedPitch(DEFAULT_FIXED_PITCH);
        Autotalent.setFixedPull(fixedPull);
        Autotalent.setCorrectionStrength(strength);
        Autotalent.setCorrectionSmoothness(smooth);
        Autotalent.setPitchShift(pitchShift);
        Autotalent.setScaleRotate(DEFAULT_SCALE_ROTATE);
        Autotalent.setLfoDepth(DEFAULT_LFO_DEPTH);
        Autotalent.setLfoRate(DEFAULT_LFO_RATE);
        Autotalent.setLfoShape(DEFAULT_LFO_SHAPE);
        Autotalent.setLfoSymmetric(DEFAULT_LFO_SYM);
        Autotalent.setLfoQuantization(DEFAULT_LFO_QUANT);
        Autotalent.setFormantCorrection(formantCorrection);
        Autotalent.setFormantWarp(formantWarp);
        Autotalent.setMix(mix);
    }

    private static boolean canWriteToSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mScreenLockListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (getString(R.string.prefs_prevent_screen_lock_key).equals(key)) {
                        if (PreferenceHelper.getScreenLock(mContext)) {
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else {
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                }
            };
}