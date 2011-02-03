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

package com.intervigil.micdroid.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.RecordingPlayer;
import com.intervigil.micdroid.model.Recording;

public class RecordingOptionsHelper {

    public static void playRecording(Context context, Recording recording) {
        try {
            Intent playIntent = new Intent(Intent.ACTION_VIEW);
            playIntent.setDataAndType(Uri.fromFile(recording.asFile()), Constants.MIME_AUDIO_WAV);
            context.startActivity(playIntent);
        } catch (ActivityNotFoundException e) {
            Intent playIntent = new Intent(context, RecordingPlayer.class);
            Bundle playData = new Bundle();
            playData.putParcelable(Constants.INTENT_EXTRA_RECORDING, recording);
            playIntent.putExtras(playData);
            context.startActivity(playIntent);
        }
    }

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

    public static void shareRecording(Context context, Recording recording) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(recording.asFile()));
        shareIntent.setType(Constants.MIME_AUDIO_WAV);
        context.startActivity(Intent.createChooser(shareIntent, "Share"));
    }
}
