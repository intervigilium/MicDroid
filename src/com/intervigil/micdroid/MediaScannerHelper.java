/* MediaScannerHelper.java

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
		// set our result code
		scanSuccessful = (uri != null);  
	}
}
