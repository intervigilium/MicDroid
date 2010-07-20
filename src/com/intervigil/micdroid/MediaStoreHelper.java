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

	private static final int WAVE_HEADER_SIZE = 44;
	private static final int MILLISECONDS_IN_SECOND = 1000;
	
	public static boolean isInserted(Context context, File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, file.getName());
		
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_data", "title" }, "_data=? and title=?", new String[] { file.getAbsolutePath(), file.getName() }, null);
        return (results.getCount() > 0);
	}
	
	public static void insertFile(Context context, File file) {
		WaveReader reader = new WaveReader(file);
		
		try {
			reader.openWave();
			
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
	        values.put(MediaStore.MediaColumns.TITLE, file.getName());
	        values.put(MediaStore.MediaColumns.SIZE, reader.getDataSize() + WAVE_HEADER_SIZE);
	        values.put(MediaStore.MediaColumns.MIME_TYPE, Constants.AUDIO_WAVE);

	        values.put(MediaStore.Audio.Media.ARTIST, "MicDroid");
	        values.put(MediaStore.Audio.Media.ALBUM, "MicDroid");
	        values.put(MediaStore.Audio.Media.DURATION, reader.getLength() * MILLISECONDS_IN_SECOND);

	        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
	        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
	        values.put(MediaStore.Audio.Media.IS_ALARM, false);
	        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
	        
	        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

	        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_data", "title" }, "_data=? and title=?", new String[] { file.getAbsolutePath(), file.getName() }, null);
	        if (results.getCount() > 0) {
	        	context.getContentResolver().delete(contentUri, "_data=? and title=?", new String[] { file.getAbsolutePath(), file.getName() });   
	        }
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
	
	public static void removeFile(Context context, File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, file.getName());
		
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());

        Cursor results = context.getContentResolver().query(contentUri, new String[] { "_data", "title" }, "_data=? and title=?", new String[] { file.getAbsolutePath(), file.getName() }, null);
        if (results.getCount() > 0) {
        	context.getContentResolver().delete(contentUri, "_data=? and title=?", new String[] { file.getAbsolutePath(), file.getName() });
        }
	
	}
}
