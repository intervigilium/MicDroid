/* AutoTalent.java

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

package com.intervigil.micdroid.pitch;

public class AutoTalent {
	private static final String AUTOTALENT_LIB = "autotalent";
	
	static {
		System.loadLibrary(AUTOTALENT_LIB);
	}
	
	public static native void instantiateAutoTalent(int sampleRate);
	
	public static native void initializeAutoTalent(float concertA, char key, 
			float fixedPitch, float fixedPull, 
			float correctStrength, float correctSmooth, float pitchShift, int scaleRotate,
			float lfoDepth, float lfoRate, float lfoShape, float lfoSym, int lfoQuant, 
			int formCorr, float formWarp, float mix);
	
	public static native void processSamples(short[] samples, int sampleSize);
	
	public static native void destroyAutoTalent();
}
