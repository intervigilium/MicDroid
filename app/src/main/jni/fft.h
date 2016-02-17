/* fft.h
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

typedef struct {
	int nfft;		// size of FFT
	int numfreqs;		// number of frequencies represented (nfft/2 +1)
	float *fft_data;	// array for writing/reading to/from FFT function
} fft_vars;

fft_vars *fft_con(int nfft);

void fft_des(fft_vars * membvars);

void
fft_forward(fft_vars * membvars, float *input, float *output_re,
	    float *output_im);

void
fft_inverse(fft_vars * membvars, float *input_re, float *input_im,
	    float *output);
