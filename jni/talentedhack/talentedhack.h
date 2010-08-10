#ifndef AUTOTALENT_LADSPA_H
#define AUTOTALENT_LADSPA_H

#include <jni.h>
#include "formant_corrector.h"
#include "pitch_detector.h"
#include "pitch_shifter.h"
#include "quantizer.h"
#include "lfo.h"
#include "pitch_smoother.h"

#define TALENTEDHACK_URI "http://jeremy.salwen/plugins/talentedhack"

/*************************
 *      THE SCALES       *
 *************************/

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

/*************************
 *  THE MEMBER VARIABLES *
 *************************/

typedef struct {
	float* p_mix;
	float* p_InputBuffer;
	float* p_OutputBuffer;
	fft_vars* fmembvars; // member variables for fft routine

	unsigned long fs; // Sample rate
    int noverlap;
	
	FormantCorrector fcorrector;
	PitchDetector pdetector;
	PitchShifter pshifter;
	PitchSmoother psmoother;
	Quantizer quantizer;
	LFO lfo;
	CircularBuffer buffer;
} TalentedHack
;

/* Header for class com_intervigil_micdroid_AutoTalent */

#ifndef _Included_com_intervigil_micdroid_pitch_TalentedHack
#define _Included_com_intervigil_micdroid_pitch_TalentedHack
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_intervigil_micdroid_TalentedHack
 * Method:    instantiateTalentedHack
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_instantiateTalentedHack
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_intervigil_micdroid_TalentedHack
 * Method:    initializeTalentedHack
 * Signature: (FCFFFFFFIIFF)V
 */
JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_initializeTalentedHack
  (JNIEnv *, jclass, jfloat, jchar, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jint, jint, jfloat, jfloat);

/*
 * Class:     com_intervigil_micdroid_TalentedHack
 * Method:    processSamples
 * Signature: ([SI)V
 */
JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_processSamples
  (JNIEnv *, jclass, jshortArray, jint);

/*
 * Class:     com_intervigil_micdroid_TalentedHack
 * Method:    destroyTalentedHack
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_intervigil_micdroid_pitch_TalentedHack_destroyTalentedHack
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
#endif
