/* FileNameEntry.java

   Copyright (c) 2010 Ethan Chen

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.
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

public class FileNameEntry extends Activity {
	
	private AlertDialog invalidNameAlert;
	
	/**
     * Called when the activity is starting.  This is where most
     * initialization should go: calling setContentView(int) to inflate
     * the activity's UI, etc.
     * 
     * @param   savedInstanceState	Activity's saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filename_entry);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
        String defaultName = formatter.format(new Date());
        ((EditText)findViewById(R.id.filename_entry_field)).setText(defaultName);
        
        invalidNameAlert = new AlertDialog.Builder(this)
        	.setTitle(getString(R.string.invalid_name_alert_title))
        	.setMessage(getString(R.string.invalid_name_alert_message))
        	.setNeutralButton("ok", 
        		new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						invalidNameAlert.dismiss();
					}
				})
        	.create();
        
        ((Button)findViewById(R.id.filename_entry_btn_ok)).setOnClickListener(mOkBtnListener);
    }
    
    private OnClickListener mOkBtnListener = new OnClickListener() {	
		public void onClick(View v) {
			EditText fileNameInput = (EditText)findViewById(R.id.filename_entry_field);
			String fileName = fileNameInput.getText().toString();
			Intent result = new Intent();
			result.putExtra(Constants.NAME_ENTRY_INTENT_FILE_NAME, fileName);
			
			Bundle data = getIntent().getExtras();
			if (data != null) {
				Recording r = data.getParcelable(Constants.NAME_ENTRY_INTENT_RECORDING);
				result.putExtra(Constants.NAME_ENTRY_INTENT_RECORDING, r);
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
