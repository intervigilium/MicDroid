/* autotalent.h
 * Autotalent library for Android
 *
 * Copyright (c) 2010 Ethan Chen
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
/*****************************************************************************/

#include "fft.h"

#define AT_A 0
#define AT_Bb 1
#define AT_B 2
#define AT_C 3
#define AT_Db 4
#define AT_D 5
#define AT_Eb 6
#define AT_E 7
#define AT_F 8
#define AT_Gb 9
#define AT_G 10
#define AT_Ab 11

#define KEY_Ab_A -1
#define KEY_Ab_Bb 1
#define KEY_Ab_B -1
#define KEY_Ab_C 1
#define KEY_Ab_Db 1
#define KEY_Ab_D -1
#define KEY_Ab_Eb 1
#define KEY_Ab_E -1
#define KEY_Ab_F 1
#define KEY_Ab_Gb -1
#define KEY_Ab_G 1
#define KEY_Ab_Ab 1

#define KEY_A_A 1
#define KEY_A_Bb -1
#define KEY_A_B 1
#define KEY_A_C -1
#define KEY_A_Db 1
#define KEY_A_D 1
#define KEY_A_Eb -1
#define KEY_A_E 1
#define KEY_A_F -1
#define KEY_A_Gb 1
#define KEY_A_G -1
#define KEY_A_Ab 1

#define KEY_Bb_A 1
#define KEY_Bb_Bb 1
#define KEY_Bb_B -1
#define KEY_Bb_C 1
#define KEY_Bb_Db -1
#define KEY_Bb_D 1
#define KEY_Bb_Eb 1
#define KEY_Bb_E -1
#define KEY_Bb_F 1
#define KEY_Bb_Gb -1
#define KEY_Bb_G 1
#define KEY_Bb_Ab -1

#define KEY_B_A -1
#define KEY_B_Bb 1
#define KEY_B_B 1
#define KEY_B_C -1
#define KEY_B_Db 1
#define KEY_B_D -1
#define KEY_B_Eb 1
#define KEY_B_E 1
#define KEY_B_F -1
#define KEY_B_Gb 1
#define KEY_B_G -1
#define KEY_B_Ab 1

#define KEY_C_A 1
#define KEY_C_Bb -1
#define KEY_C_B 1
#define KEY_C_C 1
#define KEY_C_Db -1
#define KEY_C_D 1
#define KEY_C_Eb -1
#define KEY_C_E 1
#define KEY_C_F 1
#define KEY_C_Gb -1
#define KEY_C_G 1
#define KEY_C_Ab -1

#define KEY_Db_A -1
#define KEY_Db_Bb 1
#define KEY_Db_B -1
#define KEY_Db_C 1
#define KEY_Db_Db 1
#define KEY_Db_D -1
#define KEY_Db_Eb 1
#define KEY_Db_E -1
#define KEY_Db_F 1
#define KEY_Db_Gb 1
#define KEY_Db_G -1
#define KEY_Db_Ab 1

#define KEY_D_A 1
#define KEY_D_Bb -1
#define KEY_D_B 1
#define KEY_D_C -1
#define KEY_D_Db 1
#define KEY_D_D 1
#define KEY_D_Eb -1
#define KEY_D_E 1
#define KEY_D_F -1
#define KEY_D_Gb 1
#define KEY_D_G 1
#define KEY_D_Ab -1

#define KEY_Eb_A -1
#define KEY_Eb_Bb 1
#define KEY_Eb_B -1
#define KEY_Eb_C 1
#define KEY_Eb_Db -1
#define KEY_Eb_D 1
#define KEY_Eb_Eb 1
#define KEY_Eb_E -1
#define KEY_Eb_F 1
#define KEY_Eb_Gb -1
#define KEY_Eb_G 1
#define KEY_Eb_Ab 1

#define KEY_E_A 1
#define KEY_E_Bb -1
#define KEY_E_B 1
#define KEY_E_C -1
#define KEY_E_Db 1
#define KEY_E_D -1
#define KEY_E_Eb 1
#define KEY_E_E 1
#define KEY_E_F -1
#define KEY_E_Gb 1
#define KEY_E_G -1
#define KEY_E_Ab 1

#define KEY_F_A 1
#define KEY_F_Bb 1
#define KEY_F_B -1
#define KEY_F_C 1
#define KEY_F_Db -1
#define KEY_F_D 1
#define KEY_F_Eb -1
#define KEY_F_E 1
#define KEY_F_F 1
#define KEY_F_Gb -1
#define KEY_F_G 1
#define KEY_F_Ab -1

#define KEY_Gb_A -1
#define KEY_Gb_Bb 1
#define KEY_Gb_B 1
#define KEY_Gb_C -1
#define KEY_Gb_Db 1
#define KEY_Gb_D -1
#define KEY_Gb_Eb 1
#define KEY_Gb_E -1
#define KEY_Gb_F 1
#define KEY_Gb_Gb 1
#define KEY_Gb_G -1
#define KEY_Gb_Ab 1

#define KEY_G_A 1
#define KEY_G_Bb -1
#define KEY_G_B 1
#define KEY_G_C 1
#define KEY_G_Db -1
#define KEY_G_D 1
#define KEY_G_Eb -1
#define KEY_G_E 1
#define KEY_G_F -1
#define KEY_G_Gb 1
#define KEY_G_G 1
#define KEY_G_Ab -1

// chromatic scale, X because it's unique
#define KEY_X_A 1
#define KEY_X_Bb 1
#define KEY_X_B 1
#define KEY_X_C 1
#define KEY_X_Db 1
#define KEY_X_D 1
#define KEY_X_Eb 1
#define KEY_X_E 1
#define KEY_X_F 1
#define KEY_X_Gb 1
#define KEY_X_G 1
#define KEY_X_Ab 1

#define FP_DIGITS 15
#define FP_FACTOR (1 << FP_DIGITS)

typedef struct {
	float *m_pfTune;
	float *m_pfFixed;
	float *m_pfPull;
	int *m_pfKey;
	float *m_pfAmount;
	float *m_pfSmooth;
	float *m_pfShift;
	int *m_pfScwarp;
	float *m_pfLfoamp;
	float *m_pfLforate;
	float *m_pfLfoshape;
	float *m_pfLfosymm;
	int *m_pfLfoquant;
	int *m_pfFcorr;
	float *m_pfFwarp;
	float *m_pfMix;

	short *m_pfInputBuffer1;
	short *m_pfOutputBuffer1;

	fft_vars *fmembvars;	// member variables for fft routine

	unsigned long fs;	// Sample rate

	unsigned long cbsize;	// size of circular buffer
	unsigned long corrsize;	// cbsize/2 + 1
	unsigned long cbiwr;
	unsigned long cbord;
	float *cbi;		// circular input buffer
	float *cbf;		// circular formant correction buffer
	float *cbo;		// circular output buffer

	float *cbwindow;	// hann of length N/2, zeros for the rest
	float *acwinv;		// inverse of autocorrelation of window
	float *hannwindow;	// length-N hann
	int noverlap;

	float *ffttime;
	float *fftfreqre;
	float *fftfreqim;

	// VARIABLES FOR LOW-RATE SECTION
	float aref;		// A tuning reference (Hz)
	float inpitch;		// Input pitch (semitones)
	float conf;		// Confidence of pitch period estimate (between 0 and 1)
	float outpitch;		// Output pitch (semitones)
	float vthresh;		// Voiced speech threshold

	float pmax;		// Maximum allowable pitch period (seconds)
	float pmin;		// Minimum allowable pitch period (seconds)
	unsigned long nmax;	// Maximum period index for pitch prd est
	unsigned long nmin;	// Minimum period index for pitch prd est

	float lrshift;		// Shift prescribed by low-rate section
	int ptarget;		// Pitch target, between 0 and 11
	float sptarget;		// Smoothed pitch target

	float lfophase;

	// VARIABLES FOR PITCH SHIFTER
	float phprdd;		// default (unvoiced) phase period
	double inphinc;		// input phase increment
	double outphinc;	// input phase increment
	double phincfact;	// factor determining output phase increment
	double phasein;
	double phaseout;
	float *frag;		// windowed fragment of speech
	unsigned long fragsize;	// size of fragment in samples

	// VARIABLES FOR FORMANT CORRECTOR
	int ford;
	float falph;
	float flamb;
	float *fk;
	float *fb;
	float *fc;
	float *frb;
	float *frc;
	float *fsig;
	float *fsmooth;
	float fhp;
	float flp;
	float flpa;
	float **fbuff;
	float *ftvec;
	float fmute;
	float fmutealph;

} Autotalent;

Autotalent *instantiateAutotalent(unsigned long sampleRate);

void setAutotalentKey(Autotalent * autotalent, char *keyPtr);

void
setAutotalentBuffers(Autotalent * autotalent, short *inputBuffer,
		     short *outputBuffer);

void runAutotalent(Autotalent * instance, unsigned long sampleCount);

void cleanupAutotalent(Autotalent * instance);
