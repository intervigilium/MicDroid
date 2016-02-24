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
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.intervigil.micdroid.helper.AdHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.helper.RecordingOptionsHelper;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.wave.exception.InvalidWaveException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends Activity
        implements RecordingOptionsDialogFragment.RecordingOptionsDialogListener {
    private static final String TAG = "Library";

    private static class PrivateFileObserver extends FileObserver {
        final RecordingLoader mLoader;

        public PrivateFileObserver(RecordingLoader loader) {
            super(loader.getContext().getFilesDir().getAbsolutePath(),
                    CREATE | DELETE | MOVED_FROM | MOVED_TO);
            mLoader = loader;
        }

        @Override
        public void onEvent(int event, String path) {
            mLoader.onContentChanged();
        }
    }

    private static class RecordingLoader extends AsyncTaskLoader<List<Recording>> {
        private PrivateFileObserver mFileObserver;

        public RecordingLoader(Context context) {
            super(context);
        }

        @Override
        public List<Recording> loadInBackground() {
            final Context context = getContext();
            List<Recording> entries = new ArrayList<>();
            String[] wavFiles = context.fileList();

            if (wavFiles != null) {
                for (String fileName : wavFiles) {
                    try {
                        entries.add(new Recording(fileName, context.openFileInput(fileName)));
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
            return entries;
        }

        @Override
        protected void onStartLoading() {
            if (mFileObserver == null) {
                // Perform initial load first
                forceLoad();
                mFileObserver = new PrivateFileObserver(this);
                mFileObserver.startWatching();
            }
            super.onStartLoading();
        }

        @Override
        protected void onReset() {
            super.onReset();

            if (mFileObserver != null) {
                mFileObserver.stopWatching();
                mFileObserver = null;
            }
        }
    }

    private static class RecordingAdapter extends ArrayAdapter<Recording> {
        private final LayoutInflater mInflater;

        public RecordingAdapter(Context context, int rowResourceId) {
            super(context, rowResourceId);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<Recording> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.library_fragment_row, parent, false);
            } else {
                view = convertView;
            }

            Recording r = getItem(position);
            if (r != null) {
                TextView name = (TextView) view.findViewById(R.id.recording_row_first_line);
                TextView length = (TextView) view.findViewById(R.id.recording_row_second_line);
                // TODO: handle this correctly with resources
                name.setText("Name: " + r.getName());
                length.setText("Length: " + r.getLength());
            }

            return view;
        }
    }

    public static class LibraryFragment extends ListFragment {
        private RecordingAdapter mAdapter;
        private boolean mShowAds;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
            return inflater.inflate(R.layout.library_fragment, container, false);
        }

        @Override
        public void onActivityCreated(Bundle icicle) {
            super.onActivityCreated(icicle);

            mAdapter = new RecordingAdapter(getActivity(), R.layout.library_fragment_row);
            setListAdapter(mAdapter);

            getLoaderManager().initLoader(0, null, mLoaderCallbacks);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                    getActivity());
            loadPreferences();
            sharedPrefs.registerOnSharedPreferenceChangeListener(mAdPrefListener);
        }

        @Override
        public void onViewCreated(View view, Bundle icicle) {
            super.onViewCreated(view, icicle);

            AdView ad = (AdView) view.findViewById(R.id.library_ad);
            AdHelper.GenerateAd(ad, mShowAds);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            final Recording r = mAdapter.getItem(position);
            Bundle args = new Bundle();
            args.putParcelable(
                    RecordingOptionsDialogFragment.RECORDING_OPTIONS_DIALOG_ARG_RECORDING, r);
            RecordingOptionsDialogFragment optionsDialogFragment =
                    new RecordingOptionsDialogFragment();
            optionsDialogFragment.setArguments(args);
            optionsDialogFragment.show(getFragmentManager(), "options");
        }

        private LoaderManager.LoaderCallbacks mLoaderCallbacks =
                new LoaderManager.LoaderCallbacks<List<Recording>>() {
                    @Override
                    public Loader<List<Recording>> onCreateLoader(int id, Bundle args) {
                        return new RecordingLoader(getActivity());
                    }

                    @Override
                    public void onLoadFinished(Loader<List<Recording>> loader, List<Recording> data) {
                        mAdapter.setData(data);
                    }

                    @Override
                    public void onLoaderReset(Loader loader) {
                        mAdapter.setData(null);
                    }
                };

        private void loadPreferences() {
            mShowAds = PreferenceHelper.getShowAds(getActivity());
        }

        private SharedPreferences.OnSharedPreferenceChangeListener mAdPrefListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (getString(R.string.prefs_enable_ads_key).equals(key)) {
                            mShowAds = sharedPreferences.getBoolean(getString(R.string.prefs_enable_ads_key),
                                    getResources().getBoolean(R.bool.prefs_enable_ads_default));
                            // TODO: post message to UI thread to update ad view
                        }
                    }
                };
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.library);
    }

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
        DialogHelper.showConfirmation(LibraryActivity.this,
                R.string.confirm_delete_title,
                R.string.confirm_delete_message,
                R.string.confirm_delete_btn_yes,
                R.string.confirm_delete_btn_no,
                deleteListener);
    }

    @Override
    public void onRename(Recording r) {
        Intent renameFileIntent = new Intent(getBaseContext(), FileNameEntry.class);
        Bundle recordingData = new Bundle();
        recordingData.putParcelable(Constants.INTENT_EXTRA_RECORDING, r);
        renameFileIntent.putExtras(recordingData);

        startActivityForResult(renameFileIntent, Constants.INTENT_FILENAME_ENTRY);
    }

    @Override
    public void onSetRingtone(Recording r) {
        if (RecordingOptionsHelper.setRingTone(LibraryActivity.this, r)) {
            Toast.makeText(LibraryActivity.this,
                    R.string.recording_options_ringtone_set,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LibraryActivity.this,
                    R.string.recording_options_ringtone_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSetNotification(Recording r) {
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
    }

    @Override
    public void onShare(Recording r) {
        RecordingOptionsHelper.shareRecording(LibraryActivity.this, r);
    }
}
