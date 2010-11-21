/* UpdateHelper.java

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

import android.content.Context;

import com.intervigil.micdroid.R;
import com.intervigil.micdroid.StartupDialog;

public class UpdateHelper {
    /*
     * One time updates when app has been updated goes here Currently we always
     * show startup dialog, and reset default recording settings
     */

    public static boolean isAppUpdated(Context context) {
        return PreferenceHelper.getLastVersionCode(context) < ApplicationHelper
                .getPackageVersion(context);
    }

    public static void onAppUpdate(Context context) {
        resetRecordingSettings(context);
        resetFormantCorrection(context);
        resetPitchShift(context);
        showStartupDialog(context);

        // update the version code so we know not to do this again next time
        PreferenceHelper.setLastVersionCode(context, ApplicationHelper
                .getPackageVersion(context));
    }

    private static void showStartupDialog(Context context) {
        StartupDialog startupDialog = new StartupDialog(context,
                R.string.startup_dialog_title, R.string.startup_dialog_text,
                R.string.startup_dialog_accept_btn);
        startupDialog.show();
    }

    private static void resetRecordingSettings(Context context) {
        PreferenceHelper.unsetRecordingSettings(context);
        AudioHelper.configureRecorder(context);
    }

    private static void resetPitchShift(Context context) {
        PreferenceHelper.resetPitchShiftDefault(context);
    }

    private static void resetFormantCorrection(Context context) {
        PreferenceHelper.resetFormantCorrectionDefault(context);
    }
}
