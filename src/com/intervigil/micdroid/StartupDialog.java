/* StartupDialog.java

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

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class StartupDialog extends Dialog {
	private static final String SEEN_STARTUP_DIALOG = "seenStartupDialog";

	private Activity parent;
	private TextView textView;
	private LinearLayout buttonHolder;
	private int titleId;
	private int textId;
	private int buttonLabelId;
	
	public StartupDialog(Activity parent, int titleId, int textId, int buttonLabelId) {
		super(parent);
		this.parent = parent;
		this.titleId = titleId;
		this.textId = textId;
		this.buttonLabelId = buttonLabelId;
		
		View dialog = createDialog();
		setContentView(dialog);
	}
	
	private View createDialog() { 
		// Set title text
		setTitle(titleId);
		
        // Create the overall layout.
        LinearLayout layout = new LinearLayout(parent);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        // Create a ScrollView to put the text in.  Shouldn't be necessary...
        ScrollView tscroll = new ScrollView(parent);
        tscroll.setVerticalScrollBarEnabled(true);
        tscroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(tscroll);

        // Now create the text view and add it to the scroller.
        textView = new TextView(parent);
        textView.setTextSize(16);
        textView.setTextColor(0xffffffff);
        textView.setText(textId);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        tscroll.addView(textView);

        // Add a layout to hold the buttons.
        buttonHolder = new LinearLayout(parent);
        buttonHolder.setBackgroundColor(0xf08080);
        buttonHolder.setOrientation(LinearLayout.HORIZONTAL);
        buttonHolder.setPadding(6, 3, 3, 3);
        layout.addView(buttonHolder, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Add the OK button.
        Button btn = new Button(parent);
        btn.setText(buttonLabelId);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                okButtonPressed();
            }
        });
        buttonHolder.addView(btn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        return layout;
    }
	
	public void show() {
		if (!isAccepted()) {
			// check to see if this is first run
			super.show();
		}
	}
	
	protected void okButtonPressed() {
		// write to settings
		setSeen();
		dismiss();
	}

    private boolean isAccepted() {
        int seen = -1;
        try {
            seen = PreferenceManager.getDefaultSharedPreferences(parent).getInt(SEEN_STARTUP_DIALOG, seen);
        } catch (Exception e) { }

        return seen == getPackageVersion();
    }
    
    private void setSeen() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(parent).edit();
        editor.putInt(SEEN_STARTUP_DIALOG, getPackageVersion());
        editor.commit();
    }
    
    private int getPackageVersion() {
    	int versionCode = -1;
    	try {
			versionCode = parent.getPackageManager().getPackageInfo(parent.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
    }
}
