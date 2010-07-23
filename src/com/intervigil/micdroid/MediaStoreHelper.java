/* MediaStoreHelper.java

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class MediaStoreHelper {

	public static boolean isInserted(Context context, Recording r) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, r.getName());
		values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());
		
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_display_name" }, "_display_name=?", new String[] { r.getName() }, null);

        int count = 0;
        if (results != null) {
        	count = results.getCount();
        }
        results.close();
        
        return (count > 0);
	}
	
	public static void insertFile(Context context, File file) {
		WaveReader reader = new WaveReader(file);
		
		try {
			reader.openWave();
			
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
	        values.put(MediaStore.MediaColumns.TITLE, file.getName());
	        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
	        values.put(MediaStore.MediaColumns.SIZE, reader.getDataSize() + Recording.WAVE_HEADER_SIZE);
	        values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.AUDIO_WAVE);

	        values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
	        values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
	        values.put(MediaStore.Audio.Media.DURATION, reader.getLength() * Recording.MILLISECONDS_IN_SECOND);

	        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
	        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
	        values.put(MediaStore.Audio.Media.IS_ALARM, false);
	        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
	        
	        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

	        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_display_name" }, "_display_name=?", new String[] { file.getName() }, null);
	        if (results != null && results.getCount() > 0) {
	        	context.getContentResolver().delete(contentUri, "_display_name=?", new String[] { file.getName() });   
	        }
	        results.close();
	        context.getContentResolver().insert(contentUri, values);

	        reader.closeWaveFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void insertRecording(Context context, Recording r) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, r.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());
        values.put(MediaStore.MediaColumns.SIZE, r.getSize());
        values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.AUDIO_WAVE);

        values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
        values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
        values.put(MediaStore.Audio.Media.DURATION, r.getLengthInMs() * Recording.MILLISECONDS_IN_SECOND);

        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        
        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_display_name" }, "_display_name=?", new String[] { r.getName() }, null);
        if (results != null && results.getCount() > 0) {
        	context.getContentResolver().delete(contentUri, "_display_name=?", new String[] { r.getName() });   
        }
        results.close();
        context.getContentResolver().insert(contentUri, values);
	}
	
	public static void removeFile(Context context, File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, file.getName());
		values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
		
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_display_name" }, "_display_name=?", new String[] { file.getName() }, null);
        if (results != null && results.getCount() > 0) {
        	context.getContentResolver().delete(contentUri, "_display_name=?", new String[] { file.getName() });
        }
        results.close();
	}
	
	public static void removeRecording(Context context, Recording r) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, r.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, r.getName());
		values.put(MediaStore.MediaColumns.DISPLAY_NAME, r.getName());
		
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(r.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_display_name" }, "_display_name=?", new String[] { r.getName() }, null);
        if (results != null && results.getCount() > 0) {
        	context.getContentResolver().delete(contentUri, "_display_name=?", new String[] { r.getName() });
        }
        results.close();
	}
	
	public static Uri getRecordingUri(Context context, Recording recording) {	
    	ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, recording.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, recording.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, recording.getName());
        values.put(MediaStore.MediaColumns.SIZE, recording.getSize());
        values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.AUDIO_WAVE);

        values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
        values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
        values.put(MediaStore.Audio.Media.DURATION, recording.getLengthInMs());

        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        
        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(recording.getAbsolutePath());
        context.getContentResolver().delete(contentUri, "_display_name=?", new String[] { recording.getName() });
        return context.getContentResolver().insert(contentUri, values);
	}
}
