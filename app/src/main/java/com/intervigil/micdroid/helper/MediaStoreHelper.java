/* MediaStoreHelper.java

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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.intervigil.micdroid.Constants;
import com.intervigil.micdroid.model.Recording;

import java.io.File;

public class MediaStoreHelper {

    private MediaStoreHelper() {}

    public static Uri getRecordingUri(Context context, Recording recording) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            ContentValues values = new ContentValues();
            File privateRootDir = context.getFilesDir();
            File recordingFile = new File(privateRootDir, recording.getName());
            values.put(MediaStore.MediaColumns.DATA, recordingFile.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, recording.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, recording
                    .getName());
            values.put(MediaStore.MediaColumns.SIZE, recording.getSize());
            values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.MIME_AUDIO_WAV);

            values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
            values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
            values.put(MediaStore.Audio.Media.DURATION, recording
                    .getLengthInMs());

            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);

            Uri contentUri = MediaStore.Audio.Media
                    .getContentUriForPath(recordingFile.getAbsolutePath());
            resolver.delete(contentUri, "_display_name=?",
                    new String[]{recording.getName()});
            Uri recordingUri = resolver.insert(contentUri, values);
            return recordingUri;
        }
        return null;
    }
}
