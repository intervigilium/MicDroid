#ifndef AUTOTALENT_QUANTIZER_H
#define AUTOTALENT_QUANTIZER_H
#include <math.h> 
#include <stdio.h>
#include <stdlib.h>

#define PI (float)3.14159265358979323846
//The log base 2 of 10 (or 1/log(2))
#define L2SC (float)3.32192809488736218171 

typedef struct {
	int A;
	int Bb;
	int B;
	int C;
	int Db;
	int D;
	int Eb;
	int E;
	int F;
	int Gb;
	int G;
	int Ab;
} Notes;

typedef struct {
	int note;
	float pitchbend; //scaled from -1 to 1, representing -6 to 6 semitone shift.
} MidiPitch;


typedef struct {
	Notes inotes; //The notes to be detected, should be the set of notes that the singer is attempting to sing
	Notes onotes; //The set of notes to be output, i.e. the scale you want the output to be in.
	
	float* p_amount;
	float* p_accept_midi;
	
	int iNotes[12];
	int oNotes[127];

	float* p_aref; // A tuning reference (Hz)
	
	MidiPitch InPitch;
	MidiPitch OutPitch;
} Quantizer;


void UpdateQuantizer(Quantizer * q);

void QuantizerInit(Quantizer* q);

void PullToInTune(Quantizer* q, MidiPitch* pitch);

void SendMidiOutput(Quantizer* q, MidiPitch pitch, int samplenum);

MidiPitch FetchLatestMidiNote(Quantizer* q, int samplenum);

MidiPitch pperiod_to_midi(Quantizer* q, float pperiod);

float midi_to_semitones(MidiPitch pitch);

float semitones_to_pperiod(Quantizer* q, float semitones);

int SnapToKey(int notes[12], int note, int snapup);

MidiPitch MixMidiIn(Quantizer* q, MidiPitch detected, MidiPitch in);

inline int positive_mod(int A, int B) {
	int C = A%B;
	if (C < 0) {
		C+=B;
	}
	return C;
};

#endif
