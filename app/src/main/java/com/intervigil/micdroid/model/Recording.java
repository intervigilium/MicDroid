/* Recording.java

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

package com.intervigil.micdroid.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Parcel;
import android.os.Parcelable;

import com.intervigil.wave.WaveReader;
import com.intervigil.wave.exception.InvalidWaveException;

public class Recording implements Parcelable {
    public static final int WAVE_HEADER_SIZE = 44;
    public static final int MILLISECONDS_IN_SECOND = 1000;

    private String recordingPath;
    private String recordingName;
    private int recordingLength;
    private int recordingSize;

    public static final Parcelable.Creator<Recording> CREATOR = new Parcelable.Creator<Recording>() {
        public Recording createFromParcel(Parcel in) {
            return new Recording(in);
        }

        public Recording[] newArray(int size) {
            return new Recording[size];
        }
    };

    public Recording() {
    }

    public Recording(File file) throws FileNotFoundException, InvalidWaveException, IOException {
        WaveReader reader = new WaveReader(file);
        reader.openWave();
        this.recordingPath = file.getParent();
        this.recordingName = file.getName();
        this.recordingLength = reader.getLength();
        this.recordingSize = reader.getDataSize() + WAVE_HEADER_SIZE;
        reader.closeWaveFile();
        reader = null;
    }

    private Recording(Parcel in) {
        this.recordingPath = in.readString();
        this.recordingName = in.readString();
        this.recordingLength = in.readInt();
        this.recordingSize = in.readInt();
    }

    public Recording(String path, String name, int length, int size) {
        this.recordingPath = path;
        this.recordingName = name;
        this.recordingLength = length;
        this.recordingSize = size;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(recordingPath);
        out.writeString(recordingName);
        out.writeInt(recordingLength);
        out.writeInt(recordingSize);
    }

    public File asFile() {
        return new File(recordingPath + File.separator + recordingName);
    }

    public String getAbsolutePath() {
        return recordingPath + File.separator + recordingName;
    }

    public String getName() {
        // gets recording name, typically the file name
        return recordingName;
    }

    public int getLengthInMs() {
        return recordingLength * 1000;
    }

    public String getLength() {
        // gets recording length in MM:SS format
        int minutes = recordingLength / 60;
        int seconds = recordingLength % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getSize() {
        return recordingSize;
    }

    public void setPath(String path) {
        // sets the recording path, where it is located
        recordingPath = path;
    }

    public void setName(String name) {
        // sets recording name, typically the file name
        recordingName = name;
    }

    public void setLength(int length) {
        // sets recording length, in number of seconds
        recordingLength = length;
    }

    public void setSize(int size) {
        recordingSize = size;
    }

    public boolean delete() {
        return new File(recordingPath + File.separator + recordingName).delete();
    }

    public void moveTo(File destination) {
        File recordingFile = new File(recordingPath + File.separator
                + recordingName);
        recordingPath = destination.getParent();
        recordingName = destination.getName();
        recordingFile.renameTo(destination);
    }
}
