/* JNIRecorder.java
   An auto-tune app for Android

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

package com.intervigil.micdroid.recorder;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.intervigil.micdroid.helper.AudioHelper;
import com.intervigil.micdroid.helper.PreferenceHelper;
import com.intervigil.micdroid.interfaces.DependentTask;
import com.intervigil.micdroid.interfaces.Recorder;

public class JNIRecorder implements Recorder {

    public JNIRecorder(Context context, DependentTask postRecordTask, boolean isLiveMode) {
	jniRecorderInit(PreferenceHelper.getSampleRate(context),
		AudioHelper.getRecorder(context),
		AudioHelper.getPlayer(context));
    }

    @Override
    public void cleanup() {
	jniRecorderCleanup();
    }

    @Override
    public boolean isRunning() {
        return jniIsRunning();
    }

    @Override
    public void start() {
	jniRecorderStart();
    }

    @Override
    public void stop() {
	jniRecorderStop();
    }

    private native int jniRecorderStart();
    
    private native int jniRecorderStop();
    
    private native boolean jniIsRunning();
    
    private native int jniRecorderCleanup();
    
    private native int jniRecorderInit(int sampleRate, AudioRecord record, AudioTrack track);
}
