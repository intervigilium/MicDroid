#include "quantizer.h"


static void copyNotesToBuffer(Notes * notes, int buffer[12]) {
	buffer[0] = notes->A;
	buffer[1] = notes->Bb;
	buffer[2] = notes->B;
	buffer[3] = notes->C;
	buffer[4] = notes->Db;
	buffer[5] = notes->D;
	buffer[6] = notes->Eb;
	buffer[7] = notes->E;
	buffer[8] = notes->F;
	buffer[9] = notes->Gb;
	buffer[10] = notes->G;
	buffer[11] = notes->Ab;
}

void QuantizerInit(Quantizer * q) {
	q->InPitch.note = 0;
	q->OutPitch.note = 0;
}

void UpdateQuantizer(Quantizer * q) {
	copyNotesToBuffer(&q->inotes, q->iNotes);
	copyNotesToBuffer(&q->onotes, q->oNotes);
	int numin = 0;
	int numout = 0;
	int i;
	for (i = 0; i < 12; i++) {
		if (q->iNotes[i] >= 0) {
			numin++;
		}
		if (q->oNotes[i] >= 0) {
			numout++;
		}
	}
	// If no notes are selected as being in the scale, instead snap to all notes
	if (numin == 0) {
		for (i = 0; i < 12; i++) {
			q->iNotes[i] = 1;
		}
	}
	if (numout == 0) {
		for (i = 0; i < 12; i++) {
			q->oNotes[i] = 1;
		}
	}
}

void PullToInTune(Quantizer * q, MidiPitch * pitch) {
	pitch->pitchbend *= (1 - *q->p_amount);
}

int SnapToKey(int notes[12], int note, int snapup) {
	int index = note - 69;
	if(notes[positive_mod(index,12)] >= 0) {
		return note;
	}
	int lower = index - 1;
	int higher = index + 1;
	while (notes[positive_mod(lower,12)] < 0) {
		lower--;
	}
	while (notes[positive_mod(higher,1)] < 0) {
		higher++;
	}
	if (higher - index < index - lower) {
		return higher + 69;
	}
	if (higher - index > index - lower) {
		return lower + 69;
	}
	if (notes[positive_mod(lower, 12)] >= 1) {
		return lower + 69;
	}
	if (notes[positive_mod(higher, 12)] >= 1) {
		return higher + 69;
	}
	if (snapup) {
		return higher + 69;
	} else {
		return lower + 69;
	}
}

