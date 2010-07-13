package com.intervigil.micdroid;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaScannerHelper implements MediaScannerConnectionClient {

	private MediaScannerConnection scanner;
	private String filePath;
	private boolean scanSuccessful;
	
	public MediaScannerHelper(Context context, String filePath) {
		this.filePath = filePath;
		this.scanner = new MediaScannerConnection(context, this);
		this.scanSuccessful = false;
	}
	
	public void scanFile() {
		// connect to media scanner service, then the rest is handled by callbacks
		scanner.connect();
	}
	
	public boolean getScanSuccessful() {
		return scanSuccessful;
	}
	
	public void onMediaScannerConnected() {
		// scan file right after scanFile connects to media scanner service
		scanner.scanFile(filePath, null);
	}

	public void onScanCompleted(String path, Uri uri) {
		// set our scan result code
		scanSuccessful = (uri != null);
	}

}
