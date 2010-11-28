/* RecordingOptionsHelper.java

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

package com.intervigil.micdroiddonate.helper;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.intervigil.micdroiddonate.Constants;
import com.intervigil.micdroiddonate.model.Recording;

public class RecordingOptionsHelper {

    public static boolean setRingTone(Context context, Recording recording) {
        Uri recordingUri = MediaStoreHelper.getRecordingUri(context, recording);
        if (recordingUri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_RINGTONE, recordingUri);
            return true;
        }
        return false;
    }

    public static boolean setNotificationTone(Context context,
            Recording recording) {
        Uri recordingUri = MediaStoreHelper.getRecordingUri(context, recording);
        if (recordingUri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(context,
                    RingtoneManager.TYPE_NOTIFICATION, recordingUri);
            return true;
        }
        return false;
    }

    public static void sendEmailAttachment(Context context, Recording recording) {
        String recordingPath = ApplicationHelper.getLibraryDirectory()
                + File.separator + recording.getName();
        File recordingFile = new File(recordingPath);

        Intent sendEmailIntent = new Intent(Intent.ACTION_SEND);
        sendEmailIntent.setType(Constants.AUDIO_WAVE);
        sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "MicDroid");
        sendEmailIntent.putExtra(Intent.EXTRA_STREAM, Uri
                .fromFile(recordingFile));
        context.startActivity(Intent.createChooser(sendEmailIntent, "Email:"));
    }

    public static void sendMms(Context context, Recording recording) {
        String recordingPath = ApplicationHelper.getLibraryDirectory()
                + File.separator + recording.getName();
        File recordingFile = new File(recordingPath);

        Intent sendMmsIntent = new Intent(Intent.ACTION_SEND);
        sendMmsIntent
                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(recordingFile));
        sendMmsIntent.setType(Constants.AUDIO_WAVE);
        context.startActivity(Intent.createChooser(sendMmsIntent, "MMS:"));
    }
}
