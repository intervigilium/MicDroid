/* Recording.java

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

public class Recording {
	private String recordingName;
	private int recordingLength;
	private int recordingSize;
	
	public Recording() {
	}
	
	public Recording(String name, int length, int size) {
		this.recordingName = name;
		this.recordingLength = length;
		this.recordingSize = size;
	}
	
	public String getRecordingName() {
		// gets recording name, typically the file name
		return recordingName;
	}
	
	public int getRecordingMs() {
		return recordingLength * 1000;
	}
	
	public String getRecordingLength() {
		// gets recording length in MM:SS format
		int minutes = recordingLength/60;
		int seconds = recordingLength%60;
		return String.format("%d:%02d", minutes, seconds);
	}
	
	public int getRecordingSize() {
		return recordingSize;
	}
	
	public void setRecordingName(String name) {
		// sets recording name, typically the file name
		recordingName = name;
	}

	public void setRecordingLength(int length) {
		// sets recording length, in number of seconds
		recordingLength = length;
	}
	
	public void setRecordingSize(int size) {
		recordingSize = size;
	}
}
