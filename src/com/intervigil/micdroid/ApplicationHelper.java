/* ApplicationHelper.java

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
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class ApplicationHelper {

	public static int getPackageVersion(Context context) {
    	int versionCode = -1;
    	try {
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
    }
	
	private static String getApplicationDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Android" + File.separator + "data" + File.separator + "com.intervigil.micdroid";
	}
	
	public static String getOldLibraryDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Music";
	}
	
	public static String getOutputDirectory() {
		return getApplicationDirectory() + File.separator + "temp";
	}
	
	public static String getLibraryDirectory() {
		return Environment.getExternalStorageDirectory() + File.separator + "MicDroid" + File.separator + "recordings";
	}
	
	public static String getInstrumentalDirectory() {
		return Environment.getExternalStorageDirectory() + File.separator + "MicDroid" + File.separator + "instrumental";
	}
}
