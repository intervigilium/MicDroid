/* Instrumental.java

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
import java.io.IOException;

import android.os.Parcel;
import android.os.Parcelable;

import com.intervigil.micdroiddonate.wave.WaveReader;


public class Instrumental implements Parcelable {
	/*
	 * this is basically the same as the recording object since we can only
	 * use wav files for the instrumental track right now
	 */
	
	public static final int WAVE_HEADER_SIZE = 44;
	public static final int MILLISECONDS_IN_SECOND = 1000;
	
	private String instrumentalPath;
	private String instrumentalName;
	private int instrumentalLength;
	private int instrumentalSize;
	
	public static final Parcelable.Creator<Instrumental> CREATOR = new Parcelable.Creator<Instrumental>() {
		public Instrumental createFromParcel(Parcel in) {
		    return new Instrumental(in);
		}
		
		public Instrumental[] newArray(int size) {
		    return new Instrumental[size];
		}
	};
	
	public Instrumental() {
	}
	
	public Instrumental(File file) throws IOException {
		WaveReader reader = new WaveReader(file);
		reader.openWave();
		this.instrumentalPath = file.getParent();
		this.instrumentalName = file.getName();
		this.instrumentalLength = reader.getLength();
		this.instrumentalSize = reader.getDataSize() + WAVE_HEADER_SIZE;
		reader.closeWaveFile();
		reader = null;
	}
	
	private Instrumental(Parcel in) {
		this.instrumentalPath = in.readString();
		this.instrumentalName = in.readString();
		this.instrumentalLength = in.readInt();
		this.instrumentalSize = in.readInt();
	}
	
	public Instrumental(String path, String name, int length, int size) {
		this.instrumentalPath = path;
		this.instrumentalName = name;
		this.instrumentalLength = length;
		this.instrumentalSize = size;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(instrumentalPath);
		out.writeString(instrumentalName);
		out.writeInt(instrumentalLength);
		out.writeInt(instrumentalSize);
	}
	
	public File asFile() {
		return new File(instrumentalPath + File.separator + instrumentalName);
	}
	
	public String getAbsolutePath() {
		return instrumentalPath + File.separator + instrumentalName;
	}
	
	public String getName() {
		// gets recording name, typically the file name
		return instrumentalName;
	}
	
	public int getLengthInMs() {
		return instrumentalLength * 1000;
	}
	
	public String getLength() {
		// gets recording length in MM:SS format
		int minutes = instrumentalLength/60;
		int seconds = instrumentalLength%60;
		return String.format("%d:%02d", minutes, seconds);
	}
	
	public int getSize() {
		return instrumentalSize;
	}
	
	public void setPath(String path) {
		// sets the recording path, where it is located
		instrumentalPath = path;
	}
	
	public void setName(String name) {
		// sets recording name, typically the file name
		instrumentalName = name;
	}

	public void setLength(int length) {
		// sets recording length, in number of seconds
		instrumentalLength = length;
	}
	
	public void setSize(int size) {
		instrumentalSize = size;
	}
}
