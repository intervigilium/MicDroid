/* FileNameEntry.java

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

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.intervigil.micdroid.model.Recording;

public class FileNameEntry extends Activity {

    private AlertDialog invalidNameAlert;

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
        setContentView(R.layout.filename_entry);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
        String defaultName = formatter.format(new Date());
        ((EditText) findViewById(R.id.filename_entry_field))
                .setText(defaultName);

        invalidNameAlert = new AlertDialog.Builder(this).setTitle(
                getString(R.string.invalid_name_alert_title)).setMessage(
                getString(R.string.invalid_name_alert_message))
                .setNeutralButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        invalidNameAlert.dismiss();
                    }
                }).create();

        ((Button) findViewById(R.id.filename_entry_btn_ok))
                .setOnClickListener(mOkBtnListener);
    }

    private OnClickListener mOkBtnListener = new OnClickListener() {
        public void onClick(View v) {
            EditText fileNameInput = (EditText) findViewById(R.id.filename_entry_field);
            String fileName = fileNameInput.getText().toString();
            Intent result = new Intent();
            result.putExtra(Constants.INTENT_EXTRA_FILE_NAME, fileName);

            Bundle data = getIntent().getExtras();
            if (data != null) {
                Recording r = data
                        .getParcelable(Constants.INTENT_EXTRA_RECORDING);
                result.putExtra(Constants.INTENT_EXTRA_RECORDING, r);
            }

            if (fileName == null || fileName.length() == 0) {
                invalidNameAlert.show();
            } else {
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        }
    };
}
