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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.intervigil.micdroid.helper.AdHelper;
import com.intervigil.micdroid.model.Recording;
import com.intervigil.wave.exception.InvalidWaveException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends ListFragment {

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
                for (Recording r : data) {
                    add(r);
                }
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
                name.setText(String.format(getContext().getResources().getString(
                        R.string.library_recording_name), r.getName()));
                length.setText(String.format(getContext().getResources().getString(
                        R.string.library_recording_length), r.getLength()));
            }

            return view;
        }
    }

    private RecordingAdapter mAdapter;
    private AdView mAdView;
    private boolean mShowAds;

    public LibraryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        return inflater.inflate(R.layout.library_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle icicle) {
        super.onViewCreated(view, icicle);

        mAdView = (AdView) view.findViewById(R.id.library_ad);
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        mAdapter = new RecordingAdapter(getActivity(), R.layout.library_fragment_row);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, mLoaderCallbacks);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        loadPreferences(sharedPrefs);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mAdPrefListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAdView();
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(mAdPrefListener);

        super.onDestroy();
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

    private void updateAdView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdHelper.updateAdView(mAdView, mShowAds);
            }
        });
    }

    private void loadPreferences(SharedPreferences sharedPrefs) {
        mShowAds = sharedPrefs.getBoolean(
                getResources().getString(R.string.prefs_enable_ads_key),
                getResources().getBoolean(R.bool.prefs_enable_ads_default));
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mAdPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (getString(R.string.prefs_enable_ads_key).equals(key)) {
                        mShowAds = sharedPreferences.getBoolean(getString(R.string.prefs_enable_ads_key),
                                getResources().getBoolean(R.bool.prefs_enable_ads_default));
                        updateAdView();
                    }
                }
            };
}
