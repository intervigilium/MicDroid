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

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.HeadsetHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.helper.UpdateHelper;
import com.intervigil.micdroid.model.Recording;

import net.sourceforge.autotalent.Autotalent;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements RecordingOptionsDialogFragment.RecordingOptionsDialogListener,
        NameEntryDialogFragment.NameEntryDialogListener,
        MicFragment.MicListener,
        SipdroidRecorder.RecorderStoppedListener {

    private static final String TAG = "MainActivity";

    private static final float CONCERT_A = 440.0f;

    private static final int DEFAULT_SCALE_ROTATE = 0;
    private static final float DEFAULT_FIXED_PITCH = 0.0f;
    private static final float DEFAULT_LFO_DEPTH = 0.0f;
    private static final float DEFAULT_LFO_RATE = 5.0f;
    private static final float DEFAULT_LFO_SHAPE = 0.0f;
    private static final float DEFAULT_LFO_SYM = 0.0f;
    private static final int DEFAULT_LFO_QUANT = 0;

    private Context mContext;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SipdroidRecorder mRecorder;
    private AudioController mAudioControl;
    private boolean mIsLive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mContext = this;

        mAudioControl = new AudioController(mContext);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        Toolbar actionBar = (Toolbar) findViewById(R.id.main_action_bar);
        setSupportActionBar(actionBar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, actionBar, R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView drawerView = (NavigationView) findViewById(R.id.main_drawer_view);
        drawerView.setNavigationItemSelectedListener(mDrawerClickListener);

        getFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, new MicFragment())
                .commit();

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        loadPreferences();

        if (UpdateHelper.isAppUpdated(mContext)) {
            UpdateHelper.onAppUpdate(mContext);
        } else {
            mAudioControl.configureRecorder();
        }
    }

    @Override
    public void onStop() {
        Autotalent.destroyAutotalent();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* From MicListener */
    @Override
    public boolean onMicStart() {
        if (!mAudioControl.isValidRecorder()) {
            DialogHelper.showWarning(mContext,
                    R.string.unconfigured_audio_title,
                    R.string.unconfigured_audio_warning);
            return false;
        }
        if (mIsLive) {
            if (!HeadsetHelper.isHeadsetPluggedIn(mContext)) {
                DialogHelper.showWarning(mContext,
                        R.string.no_headset_plugged_in_title,
                        R.string.no_headset_plugged_in_warning);
                return false;
            }
            updateAutotalentSettings();
        }

        if (mRecorder == null) {
            mRecorder = new SipdroidRecorder(mContext, mAudioControl);
            mRecorder.registerRecorderStoppedListener(this);
        }
        mRecorder.start();

        return true;
    }

    @Override
    public void onMicStop() {
        // mRecorder will trigger onRecorderStopped when it is finished
        mRecorder.stop();

        Toast.makeText(mContext,
                R.string.recording_finished_toast,
                Toast.LENGTH_SHORT).show();
    }

    /* From SipdroidRecorder */
    @Override
    public void onRecorderStopped() {
        DialogFragment nameEntryFragment = new NameEntryDialogFragment();
        nameEntryFragment.show(getFragmentManager(), "nameEntry");
    }

    /* From NameEntryDialogListener */
    @Override
    public void onSave(String name) {
        String fullName = name.trim() + ".wav";
        updateAutotalentSettings();
        // TODO: disallow interaction while async task is proceeding
        new AutotalentAsyncTask(mContext, mIsLive).execute(fullName);
    }

    @Override
    public void onCancel() {
        Toast.makeText(mContext, R.string.recording_save_canceled,
                Toast.LENGTH_SHORT).show();
    }

    /* From RecordingOptionsDialogListener */
    @Override
    public void onPlay(Recording r) {
        Intent playIntent = new Intent(Intent.ACTION_VIEW);
        File privateRootDir = getFilesDir();
        File recordingFile = new File(privateRootDir, r.getName());
        playIntent.setDataAndType(Uri.fromFile(recordingFile), Constants.MIME_AUDIO_WAV);
        startActivity(playIntent);
    }

    @Override
    public void onDelete(final Recording r) {
        DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteFile(r.getName());
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        DialogHelper.showConfirmation(mContext,
                R.string.confirm_delete_title,
                R.string.confirm_delete_message,
                R.string.confirm_delete_btn_yes,
                R.string.confirm_delete_btn_no,
                deleteListener);
    }

    @Override
    public void onRename(Recording r) {
        /*
        TODO: Allow this again
        Intent renameFileIntent = new Intent(getBaseContext(), NameEntryDialogFragment.class);
        Bundle recordingData = new Bundle();
        recordingData.putParcelable(Constants.INTENT_EXTRA_RECORDING, r);
        renameFileIntent.putExtras(recordingData);

        startActivityForResult(renameFileIntent, Constants.INTENT_FILENAME_ENTRY);
        */
    }

    @Override
    public void onSetRingtone(Recording r) {
        if (RecordingOptionsHelper.setRingTone(mContext, r)) {
            Toast.makeText(mContext,
                    R.string.recording_options_ringtone_set,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext,
                    R.string.recording_options_ringtone_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSetNotification(Recording r) {
        if (RecordingOptionsHelper.setNotificationTone(
                mContext, r)) {
            Toast.makeText(mContext,
                    R.string.recording_options_notification_set,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext,
                    R.string.recording_options_notification_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShare(Recording r) {
        RecordingOptionsHelper.shareRecording(mContext, r);
    }

    private void updateAutotalentSettings() {
        // TODO: Refactor to use loadPreferences/SharedPreferenceListener
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

    private void onLiveModeUpdate(boolean isLive) {
        mIsLive = isLive;
    }

    private void onScreenLockUpdate(boolean isLocked) {
        if (isLocked) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void loadPreferences() {
        onLiveModeUpdate(PreferenceHelper.getLiveMode(mContext));
        onScreenLockUpdate(PreferenceHelper.getScreenLock(mContext));
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    if (getString(R.string.prefs_live_mode_key).equals(key)) {
                        onLiveModeUpdate(PreferenceHelper.getLiveMode(mContext));
                    } else if (getString(R.string.prefs_prevent_screen_lock_key).equals(key)) {
                        onScreenLockUpdate(PreferenceHelper.getScreenLock(mContext));
                    }
                }
            };

    private NavigationView.OnNavigationItemSelectedListener mDrawerClickListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_record_fragment:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.main_content_frame, new MicFragment())
                                    .commit();
                            setTitle(item.getTitle());
                            mDrawerLayout.closeDrawers();
                            break;
                        case R.id.nav_library_fragment:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.main_content_frame, new LibraryFragment())
                                    .commit();
                            setTitle(item.getTitle());
                            mDrawerLayout.closeDrawers();
                            break;
                        case R.id.nav_settings_fragment:
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.main_content_frame, new SettingsFragment())
                                    .commit();
                            setTitle(item.getTitle());
                            mDrawerLayout.closeDrawers();
                            break;
                        case R.id.nav_help_item:
                            DialogHelper.showWarning(mContext, R.string.help_title,
                                    R.string.help_text);
                            break;
                        case R.id.nav_about_item:
                            DialogHelper.showWarning(mContext, R.string.about_title,
                                    R.string.about_text);
                            break;
                        case R.id.nav_donate_item:
                            Intent marketSearchIntent = new Intent(Intent.ACTION_SEARCH);
                            marketSearchIntent.setPackage("com.android.vending");
                            marketSearchIntent.putExtra("query", "micdroid donate");
                            marketSearchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(marketSearchIntent);
                            break;
                    }
                    return true;
                }
            };
}