package com.intervigil.micdroid;

import java.io.File;

import android.app.Application;
import android.os.Environment;

public class MicApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		File outputDir = new File(this.getOutputDirectory());
        if (!outputDir.exists()) {
        	outputDir.mkdir();
        }
        File libraryDir = new File(this.getLibraryDirectory());
        if (!libraryDir.exists()) {
        	libraryDir.mkdir();
        }
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	
	public String getOutputDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName();
	}
	
	public String getLibraryDirectory() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName() + File.separator + "library";
	}
}
