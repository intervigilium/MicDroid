/* DialogHelper.java

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

package com.intervigil.micdroid.helper;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.intervigil.micdroid.R;

public class DialogHelper {

    public static void showWarning(Context context, int titleId, int warningId) {
        Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setMessage(context.getString(warningId)).setTitle(
                context.getString(titleId)).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        warningBuilder.create().show();
    }

    public static void showConfirmation(Context context, int titleId, int messageId,
            int positiveId, int negativeId, OnClickListener clickListener) {
        Builder confirmDialogBuilder = new Builder(context);
        confirmDialogBuilder.setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.confirm_delete_btn_yes, clickListener)
                .setNegativeButton(R.string.confirm_delete_btn_no, clickListener);
        confirmDialogBuilder.create().show();
    }
}
