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

public class MediaStoreHelper {

    public static boolean isInserted(Context context, Recording r) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, r.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());

            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r
                    .getAbsolutePath());

            Cursor results = resolver.query(contentUri,
                    new String[] { "_display_name" }, "_display_name=?",
                    new String[] { r.getName() }, null);

            int count = 0;
            if (results != null) {
                count = results.getCount();
                results.close();
            }

            return (count > 0);
        }
        return false;
    }

    public static void insertRecording(Context context, Recording r) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, r.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());
            values.put(MediaStore.MediaColumns.SIZE, r.getSize());
            values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.MIME_AUDIO_WAV);

            values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
            values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
            values.put(MediaStore.Audio.Media.DURATION, r.getLengthInMs()
                    * Recording.MILLISECONDS_IN_SECOND);

            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);

            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r
                    .getAbsolutePath());

            Cursor results = resolver.query(contentUri,
                    new String[] { "_display_name" }, "_display_name=?",
                    new String[] { r.getName() }, null);
            if (results != null && results.getCount() > 0) {
                resolver.delete(contentUri, "_display_name=?", new String[] { r
                        .getName() });
                results.close();
            }

            Log.i("MediaStoreHelper", String.format("is contentUri null? %b",
                    contentUri == null));
            Log.i("MediaStoreHelper", String.format("is values null? %b",
                    values == null));
            Log.i("MediaStoreHelper", String.format("is resolver null? %b",
                    resolver == null));

            resolver.insert(contentUri, values);
            resolver = null;
        }
    }

    public static void removeRecording(Context context, Recording r) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, r.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());

            Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r
                    .getAbsolutePath());

            Cursor results = resolver.query(contentUri,
                    new String[] { "_display_name" }, "_display_name=?",
                    new String[] { r.getName() }, null);
            if (results != null && results.getCount() > 0) {
                resolver.delete(contentUri, "_display_name=?", new String[] { r
                        .getName() });
                results.close();
            }
            resolver = null;
        }
    }

    public static Uri getRecordingUri(Context context, Recording recording) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, recording
                    .getAbsolutePath());
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
                    .getContentUriForPath(recording.getAbsolutePath());
            resolver.delete(contentUri, "_display_name=?",
                    new String[] { recording.getName() });
            Uri recordingUri = resolver.insert(contentUri, values);
            resolver = null;
            return recordingUri;
        }
        return null;
    }
}
