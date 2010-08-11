/* PitchCorrector.java
   An auto-tune app for Android

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

import com.intervigil.micdroid.Constants;

public class PitchCorrector {
	
	private int type;
	private int sampleRate;
	
	public PitchCorrector(int type, int sampleRate) {
		this.type = type;
		this.sampleRate = sampleRate;
		
		switch (type) {
			default:
				this.type = Constants.PITCH_CORRECTOR_AUTOTALENT;
			case Constants.PITCH_CORRECTOR_AUTOTALENT:
				AutoTalent.instantiateAutoTalent(sampleRate);
				break;
			case Constants.PITCH_CORRECTOR_TALENTEDHACK:
				TalentedHack.instantiateTalentedHack(sampleRate);
				break;
		}
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public int getType() {
		return type;
	}
	
	public void initialize(float concertA, char key, 
			float fixedPitch, float fixedPull, 
			float correctStrength, float correctSmooth, float pitchShift, int scaleRotate,
			float lfoDepth, float lfoRate, float lfoShape, float lfoSym, int lfoQuant, 
			int formCorr, float formWarp, float mix) {
		switch (type) {
			default:
			case Constants.PITCH_CORRECTOR_AUTOTALENT:
				AutoTalent.initializeAutoTalent(concertA, key, fixedPitch, fixedPull, correctStrength, correctSmooth, pitchShift, scaleRotate, lfoDepth, lfoRate, lfoShape, lfoSym, lfoQuant, formCorr, formWarp, mix);
				break;
			case Constants.PITCH_CORRECTOR_TALENTEDHACK:
				TalentedHack.initializeTalentedHack(concertA, key, correctStrength, correctSmooth, lfoDepth, lfoRate, lfoShape, lfoSym, lfoQuant, formCorr, formWarp, mix);
				break;
		}
	}
	
	public void processSamples(short[] samples, int sampleSize) {
		switch (type) {
			default:
			case Constants.PITCH_CORRECTOR_AUTOTALENT:
				AutoTalent.processSamples(samples, sampleSize);
				break;
			case Constants.PITCH_CORRECTOR_TALENTEDHACK:
				TalentedHack.processSamples(samples, sampleSize);
				break;
		}
	}
	
	public void cleanup() {
		switch (type) {
			case Constants.PITCH_CORRECTOR_AUTOTALENT:
				AutoTalent.destroyAutoTalent();
				break;
			case Constants.PITCH_CORRECTOR_TALENTEDHACK:
				TalentedHack.destroyTalentedHack();
				break;
			default:
				AutoTalent.destroyAutoTalent();
				break;
		}
	}
}
