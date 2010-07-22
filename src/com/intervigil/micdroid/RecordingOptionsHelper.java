/* RecordingOptionsHelper.java

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

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

public class RecordingOptionsHelper {
	
	public static void setRingTone(Context context, Recording recording) {
    	Uri recordingUri = MediaStoreHelper.getRecordingUri(context, recording);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, recordingUri);
    }
	
	public static void sendEmailAttachment(Context context, Recording recording) {
		String recordingPath = ((MicApplication)context.getApplicationContext()).getLibraryDirectory() + File.separator + recording.getRecordingName();
		File recordingFile = new File(recordingPath);
		
		Intent sendEmailIntent = new Intent(Intent.ACTION_SEND);
		sendEmailIntent.setType(Constants.AUDIO_WAVE);
		sendEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "MicDroid");
		sendEmailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(recordingFile));
		context.startActivity(Intent.createChooser(sendEmailIntent, "Email:"));
	}
	
	public static void sendMms(Context context, Recording recording) {
		String recordingPath = ((MicApplication)context.getApplicationContext()).getLibraryDirectory() + File.separator + recording.getRecordingName();
		File recordingFile = new File(recordingPath);
		
		Intent sendMmsIntent = new Intent(Intent.ACTION_SEND);
		sendMmsIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(recordingFile));
		sendMmsIntent.setType(Constants.AUDIO_WAVE);
		context.startActivity(Intent.createChooser(sendMmsIntent, "MMS:"));
	}
}
