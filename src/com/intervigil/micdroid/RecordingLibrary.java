/* RecordingLibrary.java

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.admob.android.ads.AdView;
import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.MediaStoreHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.micdroid.wave.WaveReader;

public class RecordingLibrary extends Activity {

    private static final String STATE_LOAD_IN_PROGRESS = "load_recordings_in_progress";

    private Boolean showAds;
    private AdView ad;
    private ListView library;
    private RecordingAdapter libraryAdapter;
    private ArrayList<Recording> recordings;
    private LoadRecordingsTask loadRecordingsTask;

    private ProgressDialog loadRecordingSpinner;

    /**
     * Called when the activity is starting. This is where most initialization
     * should go: calling setContentView(int) to inflate the activity's UI, etc.
     * 
     * @param savedInstanceState
     *            Activity's saved state, if any.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_library);

        showAds = PreferenceHelper.getShowAds(RecordingLibrary.this);

        ad = (AdView) findViewById(R.id.recording_ad);
        ad.setEnabled(showAds);

        library = (ListView) findViewById(R.id.recording_library_list);
        library.setOnItemClickListener(libraryClickListener);
        registerForContextMenu(library);

        Object savedRecordings = getLastNonConfigurationInstance();
        if (savedRecordings == null) {
            recordings = new ArrayList<Recording>();
            this.libraryAdapter = new RecordingAdapter(this,
                    R.layout.recording_library_row, recordings);
            library.setAdapter(libraryAdapter);
            loadRecordingsTask = (LoadRecordingsTask) new LoadRecordingsTask()
                    .execute((Void) null);
        } else {
            recordings = (ArrayList<Recording>) savedRecordings;
            this.libraryAdapter = new RecordingAdapter(this,
                    R.layout.recording_library_row, recordings);
            library.setAdapter(libraryAdapter);
            this.libraryAdapter.notifyDataSetChanged();
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
    protected void onDestroy() {
        Log.i(getPackageName(), "onDestroy()");
        super.onDestroy();

        onCancelLoadRecordings();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(getPackageName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);

        saveLoadRecordingsTask(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(getPackageName(), "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);

        restoreLoadRecordingsTask(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        final ArrayList<Recording> recordingList = recordings;
        return recordingList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case Constants.PLAYER_INTENT_CODE:
            if (resultCode == Constants.RESULT_FILE_DELETED) {
                // refresh recordings list since something was removed
                loadRecordingsTask = (LoadRecordingsTask) new LoadRecordingsTask()
                        .execute((Void) null);
            }
            break;
        case Constants.FILENAME_ENTRY_INTENT_CODE:
            if (resultCode == Activity.RESULT_OK) {
                // get results from the intent
                Recording r = data
                        .getParcelableExtra(Constants.NAME_ENTRY_INTENT_RECORDING);
                String destinationName = data.getStringExtra(
                        Constants.NAME_ENTRY_INTENT_FILE_NAME).trim()
                        + ".wav";

                File destination = new File(ApplicationHelper
                        .getLibraryDirectory()
                        + File.separator + destinationName);
                MediaStoreHelper.removeRecording(RecordingLibrary.this, r);
                r.moveTo(destination);
                // refresh recordings list since something was renamed
                loadRecordingsTask = (LoadRecordingsTask) new LoadRecordingsTask()
                        .execute((Void) null);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.recording_options_title);

        menu.add(Menu.NONE, R.string.recording_options_rename, Menu.NONE,
                R.string.recording_options_rename);
        menu.add(Menu.NONE, R.string.recording_options_set_ringtone, Menu.NONE,
                R.string.recording_options_set_ringtone);
        menu.add(Menu.NONE, R.string.recording_options_set_notification,
                Menu.NONE, R.string.recording_options_set_notification);
        menu.add(Menu.NONE, R.string.recording_options_send_email, Menu.NONE,
                R.string.recording_options_send_email);
        // disable MMS for now because it can't attach wav files
        // menu.add(Menu.NONE, R.string.recording_options_send_mms, Menu.NONE,
        // R.string.recording_options_send_mms);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        Recording r = (Recording) libraryAdapter.getItem(info.position);

        switch (item.getItemId()) {
        case R.string.recording_options_set_ringtone:
            if (RecordingOptionsHelper.setRingTone(RecordingLibrary.this, r)) {
                Toast.makeText(RecordingLibrary.this,
                        R.string.recording_options_ringtone_set,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RecordingLibrary.this,
                        R.string.recording_options_ringtone_error,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        case R.string.recording_options_set_notification:
            if (RecordingOptionsHelper.setNotificationTone(
                    RecordingLibrary.this, r)) {
                Toast.makeText(RecordingLibrary.this,
                        R.string.recording_options_notification_set,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RecordingLibrary.this,
                        R.string.recording_options_notification_error,
                        Toast.LENGTH_SHORT).show();
            }
            break;
        case R.string.recording_options_send_email:
            RecordingOptionsHelper
                    .sendEmailAttachment(RecordingLibrary.this, r);
            break;
        case R.string.recording_options_send_mms:
            RecordingOptionsHelper.sendMms(RecordingLibrary.this, r);
            break;
        case R.string.recording_options_rename:
            Intent renameFileIntent = new Intent(getBaseContext(),
                    FileNameEntry.class);
            // add recording info to file name entry intent
            Bundle recordingData = new Bundle();
            recordingData.putParcelable(Constants.NAME_ENTRY_INTENT_RECORDING,
                    r);
            renameFileIntent.putExtras(recordingData);

            startActivityForResult(renameFileIntent,
                    Constants.FILENAME_ENTRY_INTENT_CODE);
            break;
        default:
            break;
        }
        return true;
    }

    private OnItemClickListener libraryClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Recording r = (Recording) parent.getItemAtPosition(position);

            Intent playIntent = new Intent(getBaseContext(),
                    RecordingPlayer.class);
            Bundle playData = new Bundle();
            // add recording info to play intent
            playData.putParcelable(Constants.PLAYER_INTENT_RECORDING, r);
            playIntent.putExtras(playData);

            startActivityForResult(playIntent, Constants.PLAYER_INTENT_CODE);
        }
    };

    private class RecordingAdapter extends ArrayAdapter<Recording> {
        public RecordingAdapter(Context context, int textViewResourceId,
                List<Recording> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi
                        .inflate(R.layout.recording_library_row, parent, false);
            }

            Recording r = this.getItem(position);
            if (r != null) {
                ((TextView) view.findViewById(R.id.recording_row_first_line))
                        .setText("Name: " + r.getName());
                ((TextView) view.findViewById(R.id.recording_row_second_line))
                        .setText("Length: " + r.getLength());
            }

            return view;
        }
    }

    private void onCancelLoadRecordings() {
        if (loadRecordingsTask != null
                && loadRecordingsTask.getStatus() == AsyncTask.Status.RUNNING) {
            loadRecordingsTask.cancel(true);
            loadRecordingsTask = null;
        }
    }

    private void saveLoadRecordingsTask(Bundle outState) {
        final LoadRecordingsTask task = loadRecordingsTask;
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
            outState.putBoolean(STATE_LOAD_IN_PROGRESS, true);
        }
    }

    private void restoreLoadRecordingsTask(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(STATE_LOAD_IN_PROGRESS)) {
            loadRecordingsTask = (LoadRecordingsTask) new LoadRecordingsTask()
                    .execute((Void) null);
        }
    }

    private class LoadRecordingsTask extends AsyncTask<Void, Recording, Void> {
        // Async load all the recordings already in the directory

        @Override
        protected void onPreExecute() {
            libraryAdapter.clear();
            loadRecordingSpinner = new ProgressDialog(RecordingLibrary.this);
            loadRecordingSpinner.setMessage("Loading recordings");
            loadRecordingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File libraryDir = new File(ApplicationHelper.getLibraryDirectory());
            File[] waveFiles = libraryDir.listFiles();

            if (waveFiles != null) {
                for (int i = 0; i < waveFiles.length; i++) {
                    if (waveFiles[i].isFile()) {
                        WaveReader reader = new WaveReader(waveFiles[i]);

                        try {
                            reader.openWave();
                            Recording r = new Recording(libraryDir
                                    .getAbsolutePath(), waveFiles[i].getName(),
                                    reader.getLength(), reader.getDataSize()
                                            + Recording.WAVE_HEADER_SIZE);
                            reader.closeWaveFile();
                            reader = null;

                            publishProgress(r);

                            // check to see if this exists in the media store,
                            // if it doesn't insert it
                            if (!MediaStoreHelper.isInserted(
                                    RecordingLibrary.this, r)) {
                                MediaStoreHelper.insertRecording(
                                        RecordingLibrary.this, r);
                                Log.i("RecordingLibrary", String.format(
                                        "Added recording %s to media store", r
                                                .getName()));
                            }
                            Log.i("RecordingLibrary", String.format(
                                    "Added recording %s to library", r
                                            .getName()));
                        } catch (IOException e) {
                            // yes I know it sucks that we do control flow with
                            // an exception here, fix it later
                            Log
                                    .i(
                                            "RecordingLibrary",
                                            String
                                                    .format(
                                                            "Non-wave file %s found in library directory!",
                                                            waveFiles[i]
                                                                    .getName()));
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Recording... values) {
            Recording r = values[0];
            if (r != null) {
                libraryAdapter.add(r);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            loadRecordingSpinner.dismiss();
        }
    }
}
