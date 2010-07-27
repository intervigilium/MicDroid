/* MicApplication.java

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

package com.intervigil.micdroid;

import java.io.File;
import java.io.IOException;

import android.app.Application;

public class MicApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		File outputDir = new File(ApplicationHelper.getOutputDirectory());
        if (!outputDir.exists()) {
        	outputDir.mkdirs();
        	try {
        		// it's ok if this fails, all it's supposed to do is prevent the user from seeing the temp file
				new File(ApplicationHelper.getOutputDirectory() + File.separator + ".nomedia").createNewFile();
			} catch (IOException e) { }
        }
        
        File libraryDir = new File(ApplicationHelper.getLibraryDirectory());
        if (!libraryDir.exists()) {
        	libraryDir.mkdirs();
        }
        
        File instrumentalDir = new File(ApplicationHelper.getInstrumentalDirectory());
        if (!instrumentalDir.exists()) {
        	instrumentalDir.mkdirs();
        }
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
