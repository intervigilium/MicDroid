/* StartupDialog.java

   Copyright (c) 2016 Ethan Chen

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

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StartupDialogFragment extends DialogFragment {

    private static final String TAG = "StartupDialog";

    private AppCompatButton mDismissBtn;

    public StartupDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View view = inflater.inflate(R.layout.startup_dialog_fragment, container);

        AppCompatTextView textView = (AppCompatTextView) view.findViewById(R.id.startup_dialog_text);
        textView.setMovementMethod(new ScrollingMovementMethod());

        mDismissBtn = (AppCompatButton) view.findViewById(R.id.startup_dialog_accept_btn);
        mDismissBtn.setOnClickListener(mBtnListener);

        return view;
    }

    private View.OnClickListener mBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().dismiss();
        }
    };
}
