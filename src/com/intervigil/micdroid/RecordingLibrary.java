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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.MediaStoreHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.wave.WaveReader;

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
            case Constants.INTENT_FILENAME_ENTRY:
                if (resultCode == Activity.RESULT_OK) {
                    // get results from the intent
                    Recording r = data.getParcelableExtra(Constants.INTENT_EXTRA_RECORDING);
                    String destinationName = data.getStringExtra(
                            Constants.INTENT_EXTRA_FILE_NAME).trim()
                            + ".wav";

                    File destination = new File(ApplicationHelper.getLibraryDirectory()
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
        menu.add(Menu.NONE, R.string.recording_options_play, Menu.NONE,
                R.string.recording_options_play);
        menu.add(Menu.NONE, R.string.recording_options_delete, Menu.NONE,
                R.string.recording_options_delete);
        menu.add(Menu.NONE, R.string.recording_options_rename, Menu.NONE,
                R.string.recording_options_rename);
        menu.add(Menu.NONE, R.string.recording_options_set_ringtone, Menu.NONE,
                R.string.recording_options_set_ringtone);
        menu.add(Menu.NONE, R.string.recording_options_set_notification,
                Menu.NONE, R.string.recording_options_set_notification);
        menu.add(Menu.NONE, R.string.recording_options_share, Menu.NONE,
                R.string.recording_options_share);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        final Recording r = (Recording) libraryAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.string.recording_options_play:
                try {
                    Intent playIntent = new Intent(Intent.ACTION_VIEW);
                    playIntent.setDataAndType(Uri.fromFile(r.asFile()), Constants.MIME_AUDIO_WAV);
                    startActivity(playIntent);
                } catch (ActivityNotFoundException e) {
                    Intent playIntent = new Intent(getBaseContext(), RecordingPlayer.class);
                    Bundle playData = new Bundle();
                    playData.putParcelable(Constants.INTENT_EXTRA_RECORDING, r);
                    playIntent.putExtras(playData);
                    startActivity(playIntent);
                }
                break;
            case R.string.recording_options_delete:
                DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                r.asFile().delete();
                                MediaStoreHelper.removeRecording(RecordingLibrary.this, r);
                                loadRecordingsTask = (LoadRecordingsTask) new LoadRecordingsTask()
                                    .execute((Void) null);
                                dialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                DialogHelper.showConfirmation(RecordingLibrary.this,
                        R.string.confirm_delete_title,
                        R.string.confirm_delete_message,
                        R.string.confirm_delete_btn_yes,
                        R.string.confirm_delete_btn_no,
                        deleteListener);
                break;
            case R.string.recording_options_rename:
                Intent renameFileIntent = new Intent(getBaseContext(), FileNameEntry.class);
                Bundle recordingData = new Bundle();
                recordingData.putParcelable(Constants.INTENT_EXTRA_RECORDING, r);
                renameFileIntent.putExtras(recordingData);

                startActivityForResult(renameFileIntent, Constants.INTENT_FILENAME_ENTRY);
                break;
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
            case R.string.recording_options_share:
                RecordingOptionsHelper.shareRecording(RecordingLibrary.this, r);
                break;
            default:
                break;
        }
        return true;
    }

    private OnItemClickListener libraryClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            view.showContextMenu();
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
                view = vi.inflate(R.layout.recording_library_row, parent, false);
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
                        Recording r = null;
                        try {
                            r = new Recording(waveFiles[i]);
                            recordings.add(r);
                        } catch (IOException e) {
                            Log.i("RecordingLibrary",
                                    String.format("Non-wave file %s found in library directory!",
                                            waveFiles[i].getName()));
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
