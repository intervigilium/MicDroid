/* talentedhack.c
   An auto-tuning LV2 plugin.
   
   by Jeremy A. Salwen
   VERSION 1.82
   Jul 30, 2010

   Based on Autotalent, which is

   Free software by Thomas A. Baran.
   http://web.mit.edu/tbaran/www/autotalent.html
   VERSION 0.2
   March 20, 2010

  
	   
   This program is free software; you can redistribute it and/or modify        
   it under the terms of the GNU General Public License as published by        
   the Free Software Foundation; either version 2 of the License, or           
   (at your option) any later version.                                         
                                                                                
   This program is distributed in the hope that it will be useful,             
   but WITHOUT ANY WARRANTY; without even the implied warranty of              
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               
   GNU General Public License for more details.                                
                                                                                
   You should have received a copy of the GNU General Public License           
   along with this program; if not, write to the Free Software                 
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  

 */

#include "talentedhack.h"
#include <android/log.h>


void setInputKey(TalentedHack * instance, char * keyPtr) {
  switch (*keyPtr) {
    case 'a':
      instance->quantizer.inotes.A = KEY_Ab_A;
      instance->quantizer.inotes.Bb = KEY_Ab_Bb;
      instance->quantizer.inotes.B = KEY_Ab_B;
      instance->quantizer.inotes.C = KEY_Ab_C;
      instance->quantizer.inotes.Db = KEY_Ab_Db;
      instance->quantizer.inotes.D = KEY_Ab_D;
      instance->quantizer.inotes.Eb = KEY_Ab_Eb;
      instance->quantizer.inotes.E = KEY_Ab_E;
      instance->quantizer.inotes.F = KEY_Ab_F;
      instance->quantizer.inotes.Gb = KEY_Ab_Gb;
      instance->quantizer.inotes.G = KEY_Ab_G;
      instance->quantizer.inotes.Ab = KEY_Ab_Ab;
      break;
    case 'A':
      instance->quantizer.inotes.A = KEY_A_A;
      instance->quantizer.inotes.Bb = KEY_A_Bb;
      instance->quantizer.inotes.B = KEY_A_B;
      instance->quantizer.inotes.C = KEY_A_C;
      instance->quantizer.inotes.Db = KEY_A_Db;
      instance->quantizer.inotes.D = KEY_A_D;
      instance->quantizer.inotes.Eb = KEY_A_Eb;
      instance->quantizer.inotes.E = KEY_A_E;
      instance->quantizer.inotes.F = KEY_A_F;
      instance->quantizer.inotes.Gb = KEY_A_Gb;
      instance->quantizer.inotes.G = KEY_A_G;
      instance->quantizer.inotes.Ab = KEY_A_Ab;
  	  break;
    case 'b':
      instance->quantizer.inotes.A = KEY_Bb_A;
      instance->quantizer.inotes.Bb = KEY_Bb_Bb;
      instance->quantizer.inotes.B = KEY_Bb_B;
      instance->quantizer.inotes.C = KEY_Bb_C;
      instance->quantizer.inotes.Db = KEY_Bb_Db;
      instance->quantizer.inotes.D = KEY_Bb_D;
      instance->quantizer.inotes.Eb = KEY_Bb_Eb;
      instance->quantizer.inotes.E = KEY_Bb_E;
      instance->quantizer.inotes.F = KEY_Bb_F;
      instance->quantizer.inotes.Gb = KEY_Bb_Gb;
      instance->quantizer.inotes.G = KEY_Bb_G;
      instance->quantizer.inotes.Ab = KEY_Bb_Ab;
	  break;
    case 'B':
      instance->quantizer.inotes.A = KEY_B_A;
      instance->quantizer.inotes.Bb = KEY_B_Bb;
      instance->quantizer.inotes.B = KEY_B_B;
      instance->quantizer.inotes.C = KEY_B_C;
      instance->quantizer.inotes.Db = KEY_B_Db;
      instance->quantizer.inotes.D = KEY_B_D;
      instance->quantizer.inotes.Eb = KEY_B_Eb;
      instance->quantizer.inotes.E = KEY_B_E;
      instance->quantizer.inotes.F = KEY_B_F;
      instance->quantizer.inotes.Gb = KEY_B_Gb;
      instance->quantizer.inotes.G = KEY_B_G;
      instance->quantizer.inotes.Ab = KEY_B_Ab;
  	  break;
    case 'C':
      instance->quantizer.inotes.A = KEY_C_A;
      instance->quantizer.inotes.Bb = KEY_C_Bb;
      instance->quantizer.inotes.B = KEY_C_B;
      instance->quantizer.inotes.C = KEY_C_C;
      instance->quantizer.inotes.Db = KEY_C_Db;
      instance->quantizer.inotes.D = KEY_C_D;
      instance->quantizer.inotes.Eb = KEY_C_Eb;
      instance->quantizer.inotes.E = KEY_C_E;
      instance->quantizer.inotes.F = KEY_C_F;
      instance->quantizer.inotes.Gb = KEY_C_Gb;
      instance->quantizer.inotes.G = KEY_C_G;
      instance->quantizer.inotes.Ab = KEY_C_Ab;
	  break;
    case 'd':
      instance->quantizer.inotes.A = KEY_Db_A;
      instance->quantizer.inotes.Bb = KEY_Db_Bb;
      instance->quantizer.inotes.B = KEY_Db_B;
      instance->quantizer.inotes.C = KEY_Db_C;
      instance->quantizer.inotes.Db = KEY_Db_Db;
      instance->quantizer.inotes.D = KEY_Db_D;
      instance->quantizer.inotes.Eb = KEY_Db_Eb;
      instance->quantizer.inotes.E = KEY_Db_E;
      instance->quantizer.inotes.F = KEY_Db_F;
      instance->quantizer.inotes.Gb = KEY_Db_Gb;
      instance->quantizer.inotes.G = KEY_Db_G;
      instance->quantizer.inotes.Ab = KEY_Db_Ab;
	  break;
    case 'D':
      instance->quantizer.inotes.A = KEY_D_A;
      instance->quantizer.inotes.Bb = KEY_D_Bb;
      instance->quantizer.inotes.B = KEY_D_B;
      instance->quantizer.inotes.C = KEY_D_C;
      instance->quantizer.inotes.Db = KEY_D_Db;
      instance->quantizer.inotes.D = KEY_D_D;
      instance->quantizer.inotes.Eb = KEY_D_Eb;
      instance->quantizer.inotes.E = KEY_D_E;
      instance->quantizer.inotes.F = KEY_D_F;
      instance->quantizer.inotes.Gb = KEY_D_Gb;
      instance->quantizer.inotes.G = KEY_D_G;
      instance->quantizer.inotes.Ab = KEY_D_Ab;
      break;
    case 'e':
      instance->quantizer.inotes.A = KEY_Eb_A;
      instance->quantizer.inotes.Bb = KEY_Eb_Bb;
      instance->quantizer.inotes.B = KEY_Eb_B;
      instance->quantizer.inotes.C = KEY_Eb_C;
      instance->quantizer.inotes.Db = KEY_Eb_Db;
      instance->quantizer.inotes.D = KEY_Eb_D;
      instance->quantizer.inotes.Eb = KEY_Eb_Eb;
      instance->quantizer.inotes.E = KEY_Eb_E;
      instance->quantizer.inotes.F = KEY_Eb_F;
      instance->quantizer.inotes.Gb = KEY_Eb_Gb;
      instance->quantizer.inotes.G = KEY_Eb_G;
      instance->quantizer.inotes.Ab = KEY_Eb_Ab;
	  break;
    case 'E':
      instance->quantizer.inotes.A = KEY_E_A;
      instance->quantizer.inotes.Bb = KEY_E_Bb;
      instance->quantizer.inotes.B = KEY_E_B;
      instance->quantizer.inotes.C = KEY_E_C;
      instance->quantizer.inotes.Db = KEY_E_Db;
      instance->quantizer.inotes.D = KEY_E_D;
      instance->quantizer.inotes.Eb = KEY_E_Eb;
      instance->quantizer.inotes.E = KEY_E_E;
      instance->quantizer.inotes.F = KEY_E_F;
      instance->quantizer.inotes.Gb = KEY_E_Gb;
      instance->quantizer.inotes.G = KEY_E_G;
      instance->quantizer.inotes.Ab = KEY_E_Ab;
	  break;
    case 'F':
      instance->quantizer.inotes.A = KEY_F_A;
      instance->quantizer.inotes.Bb = KEY_F_Bb;
      instance->quantizer.inotes.B = KEY_F_B;
      instance->quantizer.inotes.C = KEY_F_C;
      instance->quantizer.inotes.Db = KEY_F_Db;
      instance->quantizer.inotes.D = KEY_F_D;
      instance->quantizer.inotes.Eb = KEY_F_Eb;
      instance->quantizer.inotes.E = KEY_F_E;
      instance->quantizer.inotes.F = KEY_F_F;
      instance->quantizer.inotes.Gb = KEY_F_Gb;
      instance->quantizer.inotes.G = KEY_F_G;
      instance->quantizer.inotes.Ab = KEY_F_Ab;
	  break;
    case 'g':
      instance->quantizer.inotes.A = KEY_Gb_A;
      instance->quantizer.inotes.Bb = KEY_Gb_Bb;
      instance->quantizer.inotes.B = KEY_Gb_B;
      instance->quantizer.inotes.C = KEY_Gb_C;
      instance->quantizer.inotes.Db = KEY_Gb_Db;
      instance->quantizer.inotes.D = KEY_Gb_D;
      instance->quantizer.inotes.Eb = KEY_Gb_Eb;
      instance->quantizer.inotes.E = KEY_Gb_E;
      instance->quantizer.inotes.F = KEY_Gb_F;
      instance->quantizer.inotes.Gb = KEY_Gb_Gb;
      instance->quantizer.inotes.G = KEY_Gb_G;
      instance->quantizer.inotes.Ab = KEY_Gb_Ab;
	  break;
    case 'G':
      instance->quantizer.inotes.A = KEY_G_A;
      instance->quantizer.inotes.Bb = KEY_G_Bb;
      instance->quantizer.inotes.B = KEY_G_B;
      instance->quantizer.inotes.C = KEY_G_C;
      instance->quantizer.inotes.Db = KEY_G_Db;
      instance->quantizer.inotes.D = KEY_G_D;
      instance->quantizer.inotes.Eb = KEY_G_Eb;
      instance->quantizer.inotes.E = KEY_G_E;
      instance->quantizer.inotes.F = KEY_G_F;
      instance->quantizer.inotes.Gb = KEY_G_Gb;
      instance->quantizer.inotes.G = KEY_G_G;
      instance->quantizer.inotes.Ab = KEY_G_Ab;
	  break;
    case 'X':
      instance->quantizer.inotes.A = KEY_X_A;
      instance->quantizer.inotes.Bb = KEY_X_Bb;
      instance->quantizer.inotes.B = KEY_X_B;
      instance->quantizer.inotes.C = KEY_X_C;
      instance->quantizer.inotes.Db = KEY_X_Db;
      instance->quantizer.inotes.D = KEY_X_D;
      instance->quantizer.inotes.Eb = KEY_X_Eb;
      instance->quantizer.inotes.E = KEY_X_E;
      instance->quantizer.inotes.F = KEY_X_F;
      instance->quantizer.inotes.Gb = KEY_X_Gb;
      instance->quantizer.inotes.G = KEY_X_G;
      instance->quantizer.inotes.Ab = KEY_X_Ab;
	  break;
  }

  __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "Input Notes\nA: %d, Bb: %d, B: %d, C: %d, Db: %d, D: %d, Eb: %d, E: %d, F: %d, Gb: %d, G: %d, Ab: %d",
      instance->quantizer.inotes.A, instance->quantizer.inotes.Bb, instance->quantizer.inotes.B, instance->quantizer.inotes.C, instance->quantizer.inotes.Db, instance->quantizer.inotes.D, instance->quantizer.inotes.Eb, instance->quantizer.inotes.E, instance->quantizer.inotes.F, instance->quantizer.inotes.Gb, instance->quantizer.inotes.G, instance->quantizer.inotes.Ab);
}

void setOutputKey(TalentedHack * instance, char * keyPtr) {
  switch (*keyPtr) {
    case 'a':
      instance->quantizer.onotes.A = KEY_Ab_A;
      instance->quantizer.onotes.Bb = KEY_Ab_Bb;
      instance->quantizer.onotes.B = KEY_Ab_B;
      instance->quantizer.onotes.C = KEY_Ab_C;
      instance->quantizer.onotes.Db = KEY_Ab_Db;
      instance->quantizer.onotes.D = KEY_Ab_D;
      instance->quantizer.onotes.Eb = KEY_Ab_Eb;
      instance->quantizer.onotes.E = KEY_Ab_E;
      instance->quantizer.onotes.F = KEY_Ab_F;
      instance->quantizer.onotes.Gb = KEY_Ab_Gb;
      instance->quantizer.onotes.G = KEY_Ab_G;
      instance->quantizer.onotes.Ab = KEY_Ab_Ab;
      break;
    case 'A':
      instance->quantizer.onotes.A = KEY_A_A;
      instance->quantizer.onotes.Bb = KEY_A_Bb;
      instance->quantizer.onotes.B = KEY_A_B;
      instance->quantizer.onotes.C = KEY_A_C;
      instance->quantizer.onotes.Db = KEY_A_Db;
      instance->quantizer.onotes.D = KEY_A_D;
      instance->quantizer.onotes.Eb = KEY_A_Eb;
      instance->quantizer.onotes.E = KEY_A_E;
      instance->quantizer.onotes.F = KEY_A_F;
      instance->quantizer.onotes.Gb = KEY_A_Gb;
      instance->quantizer.onotes.G = KEY_A_G;
      instance->quantizer.onotes.Ab = KEY_A_Ab;
  	  break;
    case 'b':
      instance->quantizer.onotes.A = KEY_Bb_A;
      instance->quantizer.onotes.Bb = KEY_Bb_Bb;
      instance->quantizer.onotes.B = KEY_Bb_B;
      instance->quantizer.onotes.C = KEY_Bb_C;
      instance->quantizer.onotes.Db = KEY_Bb_Db;
      instance->quantizer.onotes.D = KEY_Bb_D;
      instance->quantizer.onotes.Eb = KEY_Bb_Eb;
      instance->quantizer.onotes.E = KEY_Bb_E;
      instance->quantizer.onotes.F = KEY_Bb_F;
      instance->quantizer.onotes.Gb = KEY_Bb_Gb;
      instance->quantizer.onotes.G = KEY_Bb_G;
      instance->quantizer.onotes.Ab = KEY_Bb_Ab;
	  break;
    case 'B':
      instance->quantizer.onotes.A = KEY_B_A;
      instance->quantizer.onotes.Bb = KEY_B_Bb;
      instance->quantizer.onotes.B = KEY_B_B;
      instance->quantizer.onotes.C = KEY_B_C;
      instance->quantizer.onotes.Db = KEY_B_Db;
      instance->quantizer.onotes.D = KEY_B_D;
      instance->quantizer.onotes.Eb = KEY_B_Eb;
      instance->quantizer.onotes.E = KEY_B_E;
      instance->quantizer.onotes.F = KEY_B_F;
      instance->quantizer.onotes.Gb = KEY_B_Gb;
      instance->quantizer.onotes.G = KEY_B_G;
      instance->quantizer.onotes.Ab = KEY_B_Ab;
  	  break;
    case 'C':
      instance->quantizer.onotes.A = KEY_C_A;
      instance->quantizer.onotes.Bb = KEY_C_Bb;
      instance->quantizer.onotes.B = KEY_C_B;
      instance->quantizer.onotes.C = KEY_C_C;
      instance->quantizer.onotes.Db = KEY_C_Db;
      instance->quantizer.onotes.D = KEY_C_D;
      instance->quantizer.onotes.Eb = KEY_C_Eb;
      instance->quantizer.onotes.E = KEY_C_E;
      instance->quantizer.onotes.F = KEY_C_F;
      instance->quantizer.onotes.Gb = KEY_C_Gb;
      instance->quantizer.onotes.G = KEY_C_G;
      instance->quantizer.onotes.Ab = KEY_C_Ab;
	  break;
    case 'd':
      instance->quantizer.onotes.A = KEY_Db_A;
      instance->quantizer.onotes.Bb = KEY_Db_Bb;
      instance->quantizer.onotes.B = KEY_Db_B;
      instance->quantizer.onotes.C = KEY_Db_C;
      instance->quantizer.onotes.Db = KEY_Db_Db;
      instance->quantizer.onotes.D = KEY_Db_D;
      instance->quantizer.onotes.Eb = KEY_Db_Eb;
      instance->quantizer.onotes.E = KEY_Db_E;
      instance->quantizer.onotes.F = KEY_Db_F;
      instance->quantizer.onotes.Gb = KEY_Db_Gb;
      instance->quantizer.onotes.G = KEY_Db_G;
      instance->quantizer.onotes.Ab = KEY_Db_Ab;
	  break;
    case 'D':
      instance->quantizer.onotes.A = KEY_D_A;
      instance->quantizer.onotes.Bb = KEY_D_Bb;
      instance->quantizer.onotes.B = KEY_D_B;
      instance->quantizer.onotes.C = KEY_D_C;
      instance->quantizer.onotes.Db = KEY_D_Db;
      instance->quantizer.onotes.D = KEY_D_D;
      instance->quantizer.onotes.Eb = KEY_D_Eb;
      instance->quantizer.onotes.E = KEY_D_E;
      instance->quantizer.onotes.F = KEY_D_F;
      instance->quantizer.onotes.Gb = KEY_D_Gb;
      instance->quantizer.onotes.G = KEY_D_G;
      instance->quantizer.onotes.Ab = KEY_D_Ab;
      break;
    case 'e':
      instance->quantizer.onotes.A = KEY_Eb_A;
      instance->quantizer.onotes.Bb = KEY_Eb_Bb;
      instance->quantizer.onotes.B = KEY_Eb_B;
      instance->quantizer.onotes.C = KEY_Eb_C;
      instance->quantizer.onotes.Db = KEY_Eb_Db;
      instance->quantizer.onotes.D = KEY_Eb_D;
      instance->quantizer.onotes.Eb = KEY_Eb_Eb;
      instance->quantizer.onotes.E = KEY_Eb_E;
      instance->quantizer.onotes.F = KEY_Eb_F;
      instance->quantizer.onotes.Gb = KEY_Eb_Gb;
      instance->quantizer.onotes.G = KEY_Eb_G;
      instance->quantizer.onotes.Ab = KEY_Eb_Ab;
	  break;
    case 'E':
      instance->quantizer.onotes.A = KEY_E_A;
      instance->quantizer.onotes.Bb = KEY_E_Bb;
      instance->quantizer.onotes.B = KEY_E_B;
      instance->quantizer.onotes.C = KEY_E_C;
      instance->quantizer.onotes.Db = KEY_E_Db;
      instance->quantizer.onotes.D = KEY_E_D;
      instance->quantizer.onotes.Eb = KEY_E_Eb;
      instance->quantizer.onotes.E = KEY_E_E;
      instance->quantizer.onotes.F = KEY_E_F;
      instance->quantizer.onotes.Gb = KEY_E_Gb;
      instance->quantizer.onotes.G = KEY_E_G;
      instance->quantizer.onotes.Ab = KEY_E_Ab;
	  break;
    case 'F':
      instance->quantizer.onotes.A = KEY_F_A;
      instance->quantizer.onotes.Bb = KEY_F_Bb;
      instance->quantizer.onotes.B = KEY_F_B;
      instance->quantizer.onotes.C = KEY_F_C;
      instance->quantizer.onotes.Db = KEY_F_Db;
      instance->quantizer.onotes.D = KEY_F_D;
      instance->quantizer.onotes.Eb = KEY_F_Eb;
      instance->quantizer.onotes.E = KEY_F_E;
      instance->quantizer.onotes.F = KEY_F_F;
      instance->quantizer.onotes.Gb = KEY_F_Gb;
      instance->quantizer.onotes.G = KEY_F_G;
      instance->quantizer.onotes.Ab = KEY_F_Ab;
	  break;
    case 'g':
      instance->quantizer.onotes.A = KEY_Gb_A;
      instance->quantizer.onotes.Bb = KEY_Gb_Bb;
      instance->quantizer.onotes.B = KEY_Gb_B;
      instance->quantizer.onotes.C = KEY_Gb_C;
      instance->quantizer.onotes.Db = KEY_Gb_Db;
      instance->quantizer.onotes.D = KEY_Gb_D;
      instance->quantizer.onotes.Eb = KEY_Gb_Eb;
      instance->quantizer.onotes.E = KEY_Gb_E;
      instance->quantizer.onotes.F = KEY_Gb_F;
      instance->quantizer.onotes.Gb = KEY_Gb_Gb;
      instance->quantizer.onotes.G = KEY_Gb_G;
      instance->quantizer.onotes.Ab = KEY_Gb_Ab;
	  break;
    case 'G':
      instance->quantizer.onotes.A = KEY_G_A;
      instance->quantizer.onotes.Bb = KEY_G_Bb;
      instance->quantizer.onotes.B = KEY_G_B;
      instance->quantizer.onotes.C = KEY_G_C;
      instance->quantizer.onotes.Db = KEY_G_Db;
      instance->quantizer.onotes.D = KEY_G_D;
      instance->quantizer.onotes.Eb = KEY_G_Eb;
      instance->quantizer.onotes.E = KEY_G_E;
      instance->quantizer.onotes.F = KEY_G_F;
      instance->quantizer.onotes.Gb = KEY_G_Gb;
      instance->quantizer.onotes.G = KEY_G_G;
      instance->quantizer.onotes.Ab = KEY_G_Ab;
	  break;
    case 'X':
      instance->quantizer.onotes.A = KEY_X_A;
      instance->quantizer.onotes.Bb = KEY_X_Bb;
      instance->quantizer.onotes.B = KEY_X_B;
      instance->quantizer.onotes.C = KEY_X_C;
      instance->quantizer.onotes.Db = KEY_X_Db;
      instance->quantizer.onotes.D = KEY_X_D;
      instance->quantizer.onotes.Eb = KEY_X_Eb;
      instance->quantizer.onotes.E = KEY_X_E;
      instance->quantizer.onotes.F = KEY_X_F;
      instance->quantizer.onotes.Gb = KEY_X_Gb;
      instance->quantizer.onotes.G = KEY_X_G;
      instance->quantizer.onotes.Ab = KEY_X_Ab;
	  break;
  }

  __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "Output Notes\nA: %d, Bb: %d, B: %d, C: %d, Db: %d, D: %d, Eb: %d, E: %d, F: %d, Gb: %d, G: %d, Ab: %d",
      instance->quantizer.onotes.A, instance->quantizer.onotes.Bb, instance->quantizer.onotes.B, instance->quantizer.onotes.C, instance->quantizer.onotes.Db, instance->quantizer.onotes.D, instance->quantizer.onotes.Eb, instance->quantizer.onotes.E, instance->quantizer.onotes.F, instance->quantizer.onotes.Gb, instance->quantizer.onotes.G, instance->quantizer.onotes.Ab);
}

// Set input and output buffers
static void setTalentedHackBuffers(TalentedHack * instance, float * inputBuffer, float * outputBuffer) {
	instance->p_InputBuffer = inputBuffer;
	instance->p_OutputBuffer = outputBuffer;
}

static TalentedHack * instantiateTalentedHack(double s_rate) {
	TalentedHack *membvars = (TalentedHack *)malloc(sizeof(TalentedHack));
	InstantiateCircularBuffer(&membvars->buffer,s_rate);
	unsigned long N=membvars->buffer.cbsize;
	membvars->fmembvars = fft_con(N);
	membvars->fs = s_rate;
	membvars->noverlap = 4;
	
    InstantiatePitchDetector(&membvars->pdetector, membvars->fmembvars, N, s_rate);
	InstantiateLFO(&membvars->lfo);
	FormantCorrectorInit(&membvars->fcorrector,s_rate,N);
	
	PitchShifterInit(&membvars->pshifter, s_rate,N);
	InitializePitchSmoother(&membvars->psmoother, N, membvars->noverlap, s_rate);
	QuantizerInit(&membvars->quantizer);

	return membvars;
}

inline void IncrementPointer(CircularBuffer * buffer) {
	// Input write pointer logic
	buffer->cbiwr++;
	if (buffer->cbiwr >= buffer->cbsize) {
		buffer->cbiwr = 0;
	}
}

static void runTalentedHack(TalentedHack * instance, uint32_t sample_count) {
	TalentedHack* psTalentedHack = instance;
	
	unsigned long N = psTalentedHack->buffer.cbsize;
	unsigned long Nf = psTalentedHack->buffer.corrsize;
	float fs = psTalentedHack->fs;
	
	UpdateFormantWarp(&psTalentedHack->fcorrector);
	UpdateQuantizer(&psTalentedHack->quantizer);
	UpdateLFO(&psTalentedHack->lfo,N,psTalentedHack->noverlap,fs);
	
	const float* pfInput=psTalentedHack->p_InputBuffer;
	float* pfOutput=psTalentedHack->p_OutputBuffer;
	
	int fcorr=*(psTalentedHack->fcorrector.p_Fcorr);
	
	/*******************
	 *  MAIN DSP LOOP  *
	 *******************/
	unsigned long lSampleIndex;
	for (lSampleIndex = 0; lSampleIndex < sample_count; lSampleIndex++)  {
		// load data into circular buffer
		float in = (float) *(pfInput++);
		
		psTalentedHack->buffer.cbi[psTalentedHack->buffer.cbiwr] = in;
		if (fcorr>=1) {
			RemoveFormants(&psTalentedHack->fcorrector,&psTalentedHack->buffer,in);
		}
		else {
			psTalentedHack->buffer.cbf[psTalentedHack->buffer.cbiwr] = in;
		}
		
		IncrementPointer(&psTalentedHack->buffer);
		
		// Every N/noverlap samples, run pitch estimation / manipulation code
		if ((psTalentedHack->buffer.cbiwr)%(N/psTalentedHack->noverlap) == 0) {
			//  ---- Calculate pitch and confidence ----
			float pperiod=get_pitch_period(&psTalentedHack->pdetector, obtain_autocovariance(&psTalentedHack->pdetector,psTalentedHack->fmembvars,&psTalentedHack->buffer,N),Nf,fs);
			
			if (pperiod > 0) {
				MidiPitch note;
				//Now we begin to modify the note, to determine what pitch we want to shift to
				MidiPitch input = FetchLatestMidiNote(&psTalentedHack->quantizer, lSampleIndex);
				note = MixMidiIn(&psTalentedHack->quantizer, note, input);
				note.note = SnapToKey(psTalentedHack->quantizer.oNotes, note.note, note.pitchbend>0);

				PullToInTune(&psTalentedHack->quantizer, &note);

				note.note = addquantizedLFO(&psTalentedHack->lfo,psTalentedHack->quantizer.oNotes,note.note);
				
				float outpitch = midi_to_semitones(note);
				
				outpitch = addunquantizedLFO(&psTalentedHack->lfo,outpitch);
				outpitch = SmoothPitch(&psTalentedHack->psmoother,outpitch);
				float outpperiod=semitones_to_pperiod(&psTalentedHack->quantizer, outpitch);
				// Compute variables for pitch shifter that depend on pitch
				ComputePitchShifterVariables(&psTalentedHack->pshifter, pperiod,outpperiod,fs);
				psTalentedHack->pshifter.active=1;
			} else { 
				UnVoiceMidi(&psTalentedHack->quantizer,lSampleIndex);
				ResetPitchSmoother(&psTalentedHack->psmoother);
				psTalentedHack->pshifter.active=0;
			}
		}
		
		if(psTalentedHack->pshifter.active) {
			in=ShiftPitch(&psTalentedHack->pshifter,&psTalentedHack->buffer, N);
		}
		unsigned int twoahead = (psTalentedHack->buffer.cbiwr + 2)%N;
		if (*psTalentedHack->fcorrector.p_Fcorr>=1) {
			in=AddFormants(&psTalentedHack->fcorrector,in,twoahead);
		} else {
			psTalentedHack->fcorrector.fmute = 0;
		}

		// Write audio to output of plugin
		// Mix (blend between original (delayed) =0 and processed =1)
		*(pfOutput++)=(*psTalentedHack->p_mix)*in + (1-(*psTalentedHack->p_mix))*psTalentedHack->buffer.cbi[twoahead];
		
	}
	FetchLatestMidiNote(&psTalentedHack->quantizer,sample_count-1);
}

static void cleanupTalentedHack(TalentedHack* instance) {
	TalentedHack * ATInstance = instance;
	fft_des(ATInstance->fmembvars);
 	free(ATInstance->buffer.cbi);
	free(ATInstance->buffer.cbf);
	free(ATInstance->pshifter.cbo);
	free(ATInstance->pdetector.cbwindow);
	free(ATInstance->pshifter.hannwindow);
	free(ATInstance->pdetector.acwinv);
	free(ATInstance->pshifter.frag);
	free(ATInstance->fcorrector.fk);
	free(ATInstance->fcorrector.fb);
 	free(ATInstance->fcorrector.fc);
 	free(ATInstance->fcorrector.frb);
 	free(ATInstance->fcorrector.frc);
 	free(ATInstance->fcorrector.fsmooth);
 	free(ATInstance->fcorrector.fsig);
	int i;
  	for (i=0; i<ATInstance->fcorrector.ford; i++) {
  		free(ATInstance->fcorrector.fbuff[i]);
  	}
  	free(ATInstance->fcorrector.fbuff);
  	free(ATInstance->fcorrector.ftvec);
	free(ATInstance);
}

/********************
 * HELPER FUNCTIONS *
 ********************/

float * getFloatBuffer(JNIEnv* env, jshortArray shortArray, jsize arraySize) {
	int i;
	short* shortBuffer = (short *)(*env)->GetPrimitiveArrayCritical(env, shortArray, 0);
	float* floatBuffer = calloc(arraySize, sizeof(float));

	for (i = 0; i < arraySize; i++) {
	floatBuffer[i] = ((float)(shortBuffer[i])/32768.0f);
	}

	(*env)->ReleasePrimitiveArrayCritical(env, shortArray, shortBuffer, 0);

	return floatBuffer;
}


jshort * getShortBuffer(float* floatBuffer, jsize size) {
	int i;
	jshort* shortBuffer = calloc(size, sizeof(jshort));

	for (i = 0; i < size; i++) {
	  shortBuffer[i] = (short)(floatBuffer[i]*32767.0f);
	}

	return shortBuffer;
}

/********************
 *  JNI INTERFACE   *
 ********************/

static TalentedHack * instance;

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_instantiateTalentedHack
  (JNIEnv* env, jclass class, jint sampleRate) {
  if (instance == NULL) {
    instance = instantiateTalentedHack(sampleRate);
    __android_log_print(ANDROID_LOG_DEBUG, "libautotalent.so", "instantiated autotalent at %d with sample rate: %d", instance, (instance->fs));
  }
}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_initializeTalentedHack
  (JNIEnv* env, jclass class, jfloat concertA, jchar key,
		  jfloat correctStrength, jfloat correctSmooth,
		  jfloat lfoDepth, jfloat lfoRate, jfloat lfoShape, jfloat lfoSym, jint lfoQuant,
		  jint formCorr, jfloat formWarp, jfloat mix) {
  if (instance != NULL) {
	// set our keys
    setInputKey(instance, (char *)&key);
    setOutputKey(instance, (char *)&key);

    __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "setting parameters");

    // set concert A
    *(instance->quantizer.p_aref) = (float)concertA;
    __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "Concert A: %f", *(instance->quantizer.p_aref));

    // set pitch correction parameters
    *(instance->quantizer.p_amount) = (float)correctStrength;
    *(instance->psmoother.p_pitchsmooth) = (float)correctSmooth;
    __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "CorrectStrength: %f, CorrectSmooth: %f", *(instance->quantizer.p_amount), *(instance->psmoother.p_pitchsmooth));

    // set LFO parameters
    *(instance->lfo.p_amp) = (float)lfoDepth;
    *(instance->lfo.p_rate) = (float)lfoRate;
    *(instance->lfo.p_shape) = (float)lfoShape;
    *(instance->lfo.p_symm) = (float)lfoSym;
    *(instance->lfo.p_quant) = (int)lfoQuant;

    // set formant corrector parameters
    *(instance->fcorrector.p_Fcorr) = (int)formCorr;
    *(instance->fcorrector.p_Fwarp) = (float)formWarp;

    // set mix parameter
    *(instance->p_mix) = (float)mix;

    __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "LFODepth: %f, LFORate: %f, LFOShape %f, LFOSym: %f, LFOQuant: %d, FormCorr: %d, FormWarp: %f, Mix: %f",
    		*(instance->lfo.p_amp), *(instance->lfo.p_rate), *(instance->lfo.p_shape), *(instance->lfo.p_symm), *(instance->lfo.p_quant), *(instance->fcorrector.p_Fcorr), *(instance->fcorrector.p_Fwarp), *(instance->p_mix));
  } else {
    __android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "No suitable autotalent instance found!");
  }
}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_processSamples
	(JNIEnv* env , jclass class, jshortArray samples, jint sampleSize) {
	if (instance != NULL) {
		// copy buffers
		float* sampleBuffer = getFloatBuffer(env, samples, sampleSize);
		setTalentedHackBuffers(instance, sampleBuffer, sampleBuffer);

		// process samples
		runTalentedHack(instance, sampleSize);

		// copy results back up to java array
		short* shortBuffer = getShortBuffer(sampleBuffer, sampleSize);
		(*env)->SetShortArrayRegion(env, samples, 0, sampleSize, shortBuffer);

		free(shortBuffer);
		free(sampleBuffer);
	} else {
		__android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "No suitable talentedhack instance found!");
	}
}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_destroyTalentedHack
	(JNIEnv* env, jclass class) {
	if (instance != NULL) {
		cleanupTalentedHack(instance);
		__android_log_print(ANDROID_LOG_DEBUG, "libtalentedhack.so", "cleaned up talentedhack at %d", instance);
		instance = NULL;
	}
}
