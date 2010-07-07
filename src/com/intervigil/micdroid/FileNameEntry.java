package com.intervigil.micdroid;

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
			result.putExtra(getString(R.string.filename_entry_result), fileName);
			
			if (fileName == null || fileName.length() == 0) {
				invalidNameAlert.show();
			} else {
				setResult(Activity.RESULT_OK, result);
				finish();
			}
		}
	};
}
