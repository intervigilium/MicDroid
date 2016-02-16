/* fft.c
 * A pitch-correcting LADSPA plugin.
 *
 * Free software by Thomas A. Baran.
 * http://web.mit.edu/tbaran/www/autotalent.html
 * VERSION 0.2
 * March 20, 2010
 *
 * Modified for use in Android by Ethan Chen
 * http://github.com/intervigilium/libautotalent
 * VERSION 0.01
 * January 3rd, 2011
 *
 * This program is free software; you can redistribute it and/or modify        
 * it under the terms of the GNU General Public License as published by        
 * the Free Software Foundation; either version 2 of the License, or           
 * (at your option) any later version.                                         
 *
 * This program is distributed in the hope that it will be useful,             
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               
 * GNU General Public License for more details.                                
 *
 * You should have received a copy of the GNU General Public License           
 * along with this program; if not, write to the Free Software                 
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  
 *
 */

#include "fft.h"
#include "mayer_fft.h"
#include <stdlib.h>
#include <string.h>

// Constructor for FFT routine
fft_vars *fft_con(int nfft)
{
	fft_vars *membvars = (fft_vars *) malloc(sizeof(fft_vars));

	membvars->nfft = nfft;
	membvars->numfreqs = nfft / 2 + 1;

	membvars->fft_data = (float *)calloc(nfft, sizeof(float));

	return membvars;
}

// Destructor for FFT routine
void fft_des(fft_vars * membvars)
{
	free(membvars->fft_data);
	free(membvars);
}

// Perform forward FFT of real data
// Accepts:
//   membvars - pointer to struct of FFT variables
//   input - pointer to an array of (real) input values, size nfft
//   output_re - pointer to an array of the real part of the output,
//     size nfft/2 + 1
//   output_im - pointer to an array of the imaginary part of the output,
//     size nfft/2 + 1
void
fft_forward(fft_vars * membvars, float *input, float *output_re,
	    float *output_im)
{
	int ti;
	int nfft;
	int hnfft;
	int numfreqs;

	nfft = membvars->nfft;
	hnfft = nfft / 2;
	numfreqs = membvars->numfreqs;

	for (ti = 0; ti < nfft; ti++) {
		membvars->fft_data[ti] = input[ti];
	}

	mayer_realfft(nfft, membvars->fft_data);

	output_im[0] = 0;
	for (ti = 0; ti < hnfft; ti++) {
		output_re[ti] = membvars->fft_data[ti];
		output_im[ti + 1] = membvars->fft_data[nfft - 1 - ti];
	}
	output_re[hnfft] = membvars->fft_data[hnfft];
	output_im[hnfft] = 0;
}

// Perform inverse FFT, returning real data
// Accepts:
//   membvars - pointer to struct of FFT variables
//   input_re - pointer to an array of the real part of the output,
//     size nfft/2 + 1
//   input_im - pointer to an array of the imaginary part of the output,
//     size nfft/2 + 1
//   output - pointer to an array of (real) input values, size nfft
void
fft_inverse(fft_vars * membvars, float *input_re, float *input_im,
	    float *output)
{
	int ti;
	int nfft;
	int hnfft;
	int numfreqs;

	nfft = membvars->nfft;
	hnfft = nfft / 2;
	numfreqs = membvars->numfreqs;

	for (ti = 0; ti < hnfft; ti++) {
		membvars->fft_data[ti] = input_re[ti];
		membvars->fft_data[nfft - 1 - ti] = input_im[ti + 1];
	}
	membvars->fft_data[hnfft] = input_re[hnfft];

	mayer_realifft(nfft, membvars->fft_data);

	for (ti = 0; ti < nfft; ti++) {
		output[ti] = membvars->fft_data[ti];
	}
}
