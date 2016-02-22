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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.intervigil.micdroid.helper.AdHelper;
import com.intervigil.micdroid.helper.ApplicationHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.wave.exception.InvalidWaveException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibraryActivity extends Activity implements OnItemClickListener {

    private static final String TAG = "Library";
    private static final String STATE_LOAD_IN_PROGRESS = "load_recordings_in_progress";

    private RecordingAdapter mLibraryAdapter;
    private ArrayList<Recording> mRecordings;
    private LoadRecordingsTask mLoadTask;
    private ProgressDialog mLoadSpinner;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_library);

        Boolean showAds = PreferenceHelper.getShowAds(LibraryActivity.this);

        AdView ad = (AdView) findViewById(R.id.recording_ad);
        AdHelper.GenerateAd(ad, showAds);

        ListView library = (ListView) findViewById(R.id.recording_library_list);
        library.setOnItemClickListener(this);
        registerForContextMenu(library);

        Object savedRecordings = getLastNonConfigurationInstance();
        if (savedRecordings == null) {
            mRecordings = new ArrayList<>();
        } else {
            mRecordings = (ArrayList<Recording>) savedRecordings;
        }
        mLibraryAdapter = new RecordingAdapter(this, R.layout.recording_library_row, mRecordings);
        library.setAdapter(mLibraryAdapter);
        mLoadTask = (LoadRecordingsTask) new LoadRecordingsTask().execute((Void) null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        onCancelLoadRecordings();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        saveLoadRecordingsTask(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restoreLoadRecordingsTask(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mRecordings;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.INTENT_FILENAME_ENTRY:
                if (resultCode == Activity.RESULT_OK) {
                    Recording r = data.getParcelableExtra(Constants.INTENT_EXTRA_RECORDING);
                    mRecordings.remove(r);
                    String destinationName = data.getStringExtra(
                            Constants.INTENT_EXTRA_FILE_NAME).trim()
                            + ".wav";
                    File destination = new File(ApplicationHelper.getLibraryDirectory()
                            + File.separator + destinationName);
                    // TODO: Allow file moves
                    mRecordings.add(r);
                    mLibraryAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        view.showContextMenu();
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final Recording r = mLibraryAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.string.recording_options_play:
                RecordingOptionsHelper.playRecording(LibraryActivity.this, r);
                break;
            case R.string.recording_options_delete:
                DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                getApplicationContext().deleteFile(r.getName());
                                mLibraryAdapter.remove(r);
                                mLibraryAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };
                DialogHelper.showConfirmation(LibraryActivity.this,
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
                if (RecordingOptionsHelper.setRingTone(LibraryActivity.this, r)) {
                    Toast.makeText(LibraryActivity.this,
                            R.string.recording_options_ringtone_set,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LibraryActivity.this,
                            R.string.recording_options_ringtone_error,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.string.recording_options_set_notification:
                if (RecordingOptionsHelper.setNotificationTone(
                        LibraryActivity.this, r)) {
                    Toast.makeText(LibraryActivity.this,
                            R.string.recording_options_notification_set,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LibraryActivity.this,
                            R.string.recording_options_notification_error,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.string.recording_options_share:
                RecordingOptionsHelper.shareRecording(LibraryActivity.this, r);
                break;
            default:
                break;
        }
        return true;
    }

    private class RecordingAdapter extends ArrayAdapter<Recording> {
        public RecordingAdapter(Context context, int textViewResourceId, List<Recording> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.recording_library_row, parent, false);
            }

            Recording r = getItem(position);
            if (r != null) {
                TextView name = (TextView) view.findViewById(R.id.recording_row_first_line);
                TextView length = (TextView) view.findViewById(R.id.recording_row_second_line);
                name.setText("Name: " + r.getName());
                length.setText("Length: " + r.getLength());
            }

            return view;
        }
    }

    private void onCancelLoadRecordings() {
        if (mLoadTask != null
                && mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
    }

    private void saveLoadRecordingsTask(Bundle outState) {
        final LoadRecordingsTask task = mLoadTask;
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
            outState.putBoolean(STATE_LOAD_IN_PROGRESS, true);
        }
    }

    private void restoreLoadRecordingsTask(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(STATE_LOAD_IN_PROGRESS)) {
            mLoadTask = (LoadRecordingsTask) new LoadRecordingsTask()
                    .execute((Void) null);
        }
    }

    private class LoadRecordingsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mLibraryAdapter.clear();
            mLoadSpinner = new ProgressDialog(LibraryActivity.this);
            mLoadSpinner.setMessage("Loading mRecordings");
            mLoadSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String[] wavFiles = getApplicationContext().fileList();

            Log.i(TAG, "doInBackground: Found files: " + Arrays.toString(wavFiles));

            if (wavFiles != null) {
                for (String fileName : wavFiles) {
                    Recording r;
                    try {
                        r = new Recording(fileName, openFileInput(fileName));
                        mRecordings.add(r);
                    } catch (FileNotFoundException e) {
                        Log.w(TAG, fileName + " not found in library directory!");
                    } catch (InvalidWaveException e) {
                        Log.i(TAG, "Non-wav file " + fileName + " found in library directory!");
                    } catch (IOException e) {
                        // can't recover
                        Log.e(TAG, "Error opening file: " + fileName, e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mLibraryAdapter.notifyDataSetChanged();
            mLoadSpinner.dismiss();
        }
    }
}
