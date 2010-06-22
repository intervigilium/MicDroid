package com.intervigil.micdroid;

public class Recording {
	private String recordingName;
	private int recordingLength;
	
	public Recording() {
	}
	
	public Recording(String name, int length) {
		this.recordingName = name;
		this.recordingLength = length;
	}
	
	public String getRecordingName() {
		// gets recording name, typically the file name
		return recordingName;
	}
	
	public String getRecordingLength() {
		// gets recording length in MM:SS format
		int minutes = recordingLength/60;
		int seconds = recordingLength%60;
		return String.format("%d:%02d", minutes, seconds);
	}
	
	public void setRecordingName(String name) {
		// sets recording name, typically the file name
		recordingName = name;
	}

	public void setRecordingLength(int length) {
		// sets recording length, in number of seconds
		recordingLength = length;
	}
}
