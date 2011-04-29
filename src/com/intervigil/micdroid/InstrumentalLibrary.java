/* InstrumentalLibrary.java

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

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.ads.AdView;
import com.intervigil.micdroid.helper.AdHelper;
import com.intervigil.micdroid.helper.DialogHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;

public class InstrumentalLibrary extends Activity implements OnClickListener {

    private static final String CLASS_INSTRUMENTAL_LIBRARY = "InstrumentalLibrary";

    private EditText mCurrentTrack;
    private EditText mInputFilename;
    private Boolean mShowAds;
    private AdView mAdView;

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
        setContentView(R.layout.instrumental_library);

        mShowAds = PreferenceHelper.getShowAds(InstrumentalLibrary.this);

        mAdView = (AdView) findViewById(R.id.instrumental_ad);
        AdHelper.GenerateAd(mAdView, mShowAds);

        mCurrentTrack = (EditText) findViewById(R.id.instrumental_current);
        mInputFilename = (EditText) findViewById(R.id.instrumental_selected);

        String currentTrack = PreferenceHelper.getInstrumentalTrack(InstrumentalLibrary.this);
        if (!currentTrack.equals(Constants.EMPTY_STRING)) {
            mCurrentTrack.setText(currentTrack);
        }

        ((Button) findViewById(R.id.instrumental_clear_btn)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.instrumental_select_btn)).setOnClickListener(this);
        ((Button) findViewById(R.id.instrumental_set_btn)).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        Log.i(CLASS_INSTRUMENTAL_LIBRARY, "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(CLASS_INSTRUMENTAL_LIBRARY, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(CLASS_INSTRUMENTAL_LIBRARY, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(CLASS_INSTRUMENTAL_LIBRARY, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(CLASS_INSTRUMENTAL_LIBRARY, "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.INTENT_OPEN_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    // obtain the filename
                    String filename = data.getDataString();
                    if (filename != null) {
                        // Get rid of URI prefix:
                        if (filename.startsWith("file://")) {
                            filename = filename.substring(7);
                        }
                        mInputFilename.setText(filename);
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.instrumental_clear_btn:
                PreferenceHelper.setInstrumentalTrack(InstrumentalLibrary.this,
                        Constants.EMPTY_STRING);
                mCurrentTrack.setText(R.string.instrumental_not_selected);
                Toast.makeText(InstrumentalLibrary.this,
                        R.string.instrumental_cleared_msg, Toast.LENGTH_SHORT);
                break;
            case R.id.instrumental_select_btn:
                openFile(mInputFilename.getText().toString());
                break;
            case R.id.instrumental_set_btn:
                File inputFile = new File(mInputFilename.getText().toString());
                if (inputFile.exists()) {
                    PreferenceHelper.setInstrumentalTrack(InstrumentalLibrary.this,
                            mInputFilename.getText().toString());
                    mCurrentTrack.setText(mInputFilename.getText());
                    Toast.makeText(InstrumentalLibrary.this,
                            R.string.instrumental_set_msg, Toast.LENGTH_SHORT);
                    finish();
                } else {
                    DialogHelper.showWarning(InstrumentalLibrary.this,
                            R.string.instrumental_input_invalid_title,
                            R.string.instrumental_input_invalid_error);
                }
                break;
        }
    }

    private void openFile(String fileName) {
        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);

        // Construct URI from file name.
        intent.setData(Uri.parse("file://" + fileName));

        // Set fancy title and button (optional)
        intent.putExtra(FileManagerIntents.EXTRA_TITLE,
                getString(R.string.instrumental_open_file_title));
        intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT,
                getString(R.string.instrumental_open_file_btn_label));

        try {
            startActivityForResult(intent, Constants.INTENT_OPEN_FILE);
        } catch (ActivityNotFoundException e) {
         // No compatible file manager was found.
            DialogInterface.OnClickListener marketIntentListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent marketSearchIntent = new Intent(Intent.ACTION_SEARCH);
                            marketSearchIntent.setPackage("com.android.vending");
                            marketSearchIntent.putExtra("query", "pname:org.openintents.filemanager");
                            marketSearchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(marketSearchIntent);
                            dialog.dismiss();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };
            DialogHelper.showConfirmation(InstrumentalLibrary.this,
                    R.string.confirm_market_filemanager_title,
                    R.string.confirm_market_filemanager_message,
                    R.string.confirm_market_filemanager_btn_yes,
                    R.string.confirm_market_filemanager_btn_no,
                    marketIntentListener);
        }
    }
}
