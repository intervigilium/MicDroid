package com.intervigil.micdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogHelper {
	
	public static void showWarning(Context context, int titleId, int warningId) {
		AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
		warningBuilder.setMessage(context.getString(warningId))
			.setTitle(context.getString(titleId))
			.setCancelable(false)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		warningBuilder.create().show();
	}
}
