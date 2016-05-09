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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.HeadsetHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.helper.UpdateHelper;
import com.intervigil.micdroid.model.Recording;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity
        implements RecordingOptionsDialogFragment.RecordingOptionsDialogListener,
        NameEntryDialogFragment.NameEntryDialogListener,
        MicFragment.MicListener,
        SipdroidRecorder.RecorderStoppedListener {

    private static final String TAG = "MainActivity";

    private Context mContext;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SipdroidRecorder mRecorder;
    private AudioController mAudioControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mContext = this;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        Toolbar actionBar = (Toolbar) findViewById(R.id.main_action_bar);
        setSupportActionBar(actionBar);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, actionBar, R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView drawerView = (NavigationView) findViewById(R.id.main_drawer_view);
        drawerView.setNavigationItemSelectedListener(mDrawerClickListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content_frame, new MicFragment())
                .commit();

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        loadPreferences();

        mAudioControl = new AudioController(mContext);

        if (UpdateHelper.isAppUpdated(mContext)) {
            UpdateHelper.onAppUpdate(mContext);
            StartupDialogFragment startupDialogFragment = new StartupDialogFragment();
            startupDialogFragment.show(getSupportFragmentManager(), "startup");
        } else {
            mAudioControl.configureRecorder();
        }
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
        if (mAudioControl.isLive()) {
            if (!HeadsetHelper.isHeadsetPluggedIn(mContext)) {
                DialogHelper.showWarning(mContext,
                        R.string.no_headset_plugged_in_title,
                        R.string.no_headset_plugged_in_warning);
                return false;
            }
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
        nameEntryFragment.show(getSupportFragmentManager(), "nameEntry");
    }

    /* From NameEntryDialogListener */
    @Override
    public void onRename(String srcName, String destName) {
        String fullDestName = destName.trim() + ".wav";
        try {
            moveFile(srcName, fullDestName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSave(String name) {
        String fullName = name.trim() + ".wav";
        if (mAudioControl.isLive()) {
            try {
                moveFile(mContext.getString(R.string.default_recording_name), fullName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new AutotalentAsyncTask(mContext, mAudioControl.getSampleRate()).execute(fullName);
        }
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
        DialogFragment nameEntryFragment = new NameEntryDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(NameEntryDialogFragment.NAME_ENTRY_RENAME_FILE_KEY, true);
        args.putString(NameEntryDialogFragment.NAME_ENTRY_RENAME_FILE_NAME, r.getName());
        nameEntryFragment.setArguments(args);
        nameEntryFragment.show(getSupportFragmentManager(), "renameEntry");
    }

    @Override
    public void onExport(Recording r) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "onExport: External media is not available");
            Toast.makeText(mContext, R.string.recording_options_export_external_media_unavailable,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        File externalMusicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC);
        if (!externalMusicDir.exists() && !externalMusicDir.mkdirs()) {
            Log.e(TAG, "onExport: Failed to create external music directory");
            Toast.makeText(mContext, R.string.recording_options_export_external_music_dir_unavailable,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        File exported = new File(externalMusicDir, r.getName());
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = mContext.openFileInput(r.getName()).getChannel();
            dstChannel = new FileOutputStream(exported).getChannel();

            srcChannel.transferTo(0, srcChannel.size(), dstChannel);

            Toast.makeText(mContext, R.string.recording_options_export_complete,
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "onExport: Failed to export file: " + r.getName());
            e.printStackTrace();
            Toast.makeText(mContext, R.string.recording_options_export_copy_error,
                    Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (srcChannel != null) {
                    srcChannel.close();
                }
                if (dstChannel != null) {
                    dstChannel.close();
                }
            } catch (IOException e) {
                // Do nothing
            }
        }
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

    private void moveFile(String src, String dst) throws IOException {
        int len;
        InputStream in = null;
        OutputStream out = null;
        byte[] buf = new byte[1024];
        try {
            in = mContext.openFileInput(src);
            out = mContext.openFileOutput(dst, Context.MODE_WORLD_READABLE);
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                    mContext.deleteFile(src);
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        SharedPreferences prefReader = PreferenceManager.getDefaultSharedPreferences(mContext);
        onScreenLockUpdate(prefReader.getBoolean(
                getResources().getString(R.string.prefs_prevent_screen_lock_key),
                getResources().getBoolean(R.bool.prefs_prevent_screen_lock_default)));
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPrefs,
                                                      String key) {
                    if (getString(R.string.prefs_prevent_screen_lock_key).equals(key)) {
                        onScreenLockUpdate(sharedPrefs.getBoolean(
                                getResources().getString(R.string.prefs_prevent_screen_lock_key),
                                getResources().getBoolean(R.bool.prefs_prevent_screen_lock_default)));
                    }
                }
            };

    private NavigationView.OnNavigationItemSelectedListener mDrawerClickListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_record_fragment:
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_content_frame, new MicFragment())
                                    .commit();
                            setTitle(item.getTitle());
                            mDrawerLayout.closeDrawers();
                            break;
                        case R.id.nav_library_fragment:
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_content_frame, new LibraryFragment())
                                    .commit();
                            setTitle(item.getTitle());
                            mDrawerLayout.closeDrawers();
                            break;
                        case R.id.nav_settings_fragment:
                            getSupportFragmentManager().beginTransaction()
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