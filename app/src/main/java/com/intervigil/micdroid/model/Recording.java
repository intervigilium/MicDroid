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

import android.os.Parcel;
import android.os.Parcelable;

import com.intervigil.wave.WaveReader;

import java.io.FileInputStream;
import java.io.IOException;

public class Recording implements Parcelable {

    private String mName;
    private int mLength;
    private int mSize;

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

    public Recording(String name, FileInputStream stream) throws IOException {
        WaveReader reader = new WaveReader(stream);
        reader.openWave();
        mName = name;
        mLength = reader.getLength();
        mSize = reader.getSize();
        reader.closeWaveFile();
    }

    private Recording(Parcel in) {
        mName = in.readString();
        mLength = in.readInt();
        mSize = in.readInt();
    }

    public Recording(String name, int length, int size) {
        mName = name;
        mLength = length;
        mSize = size;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeInt(mLength);
        out.writeInt(mSize);
    }

    public String getName() {
        // gets recording name, typically the file name
        return mName;
    }

    public int getLengthInMs() {
        return mLength * 1000;
    }

    public String getLength() {
        // gets recording length in MM:SS format
        int minutes = mLength / 60;
        int seconds = mLength % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getSize() {
        return mSize;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setSize(int size) {
        mSize = size;
    }
}