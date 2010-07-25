/* -*- mode: C; mode: fold -*- */
/*
 *      LAME MP3 encoding engine
 *
 *      Copyright (c) 1999-2000 Mark Taylor
 *      Copyright (c) 2000-2005 Takehiro Tominaga
 *      Copyright (c) 2000-2005 Robert Hegemann
 *      Copyright (c) 2000-2005 Gabriel Bouvigne
 *      Copyright (c) 2000-2004 Alexander Leidinger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* $Id: lame.c,v 1.323.2.8 2010/02/20 21:08:55 robert Exp $ */


struct lame_internal_flags {

/********************************************************************
* internal variables NOT set by calling program, and should not be *
* modified by the calling program                                  *
********************************************************************/

	/*
	 * Some remarks to the Class_ID field:
	 * The Class ID is an Identifier for a pointer to this struct.
	 * It is very unlikely that a pointer to lame_global_flags has the same 32 bits
	 * in it's structure (large and other special properties, for instance prime).
	 *
	 * To test that the structure is right and initialized, use:
	 *     if ( gfc -> Class_ID == LAME_ID ) ...
	 * Other remark:
	 *     If you set a flag to 0 for uninit data and 1 for init data, the right test
	 *     should be "if (flag == 1)" and NOT "if (flag)". Unintended modification
	 *     of this element will be otherwise misinterpreted as an init.
	 */
#  define  LAME_ID   0xFFF88E3B
	unsigned long Class_ID;

	int     lame_encode_frame_init;
	int     iteration_init_init;
	int     fill_buffer_resample_init;

#ifndef  MFSIZE
# define MFSIZE  ( 3*1152 + ENCDELAY - MDCTDELAY )
#endif
	sample_t mfbuf[2][MFSIZE];


	struct {
		void    (*msgf) (const char *format, va_list ap);
		void    (*debugf) (const char *format, va_list ap);
		void    (*errorf) (const char *format, va_list ap);
	} report;

	int     mode_gr;     /* granules per frame */
	int     channels_in; /* number of channels in the input data stream (PCM or decoded PCM) */
	int     channels_out; /* number of channels in the output data stream (not used for decoding) */
	double  resample_ratio; /* input_samp_rate/output_samp_rate */

	int     mf_samples_to_encode;
	int     mf_size;
	int     VBR_min_bitrate; /* min bitrate index */
	int     VBR_max_bitrate; /* max bitrate index */
	int     bitrate_index;
	int     samplerate_index;
	int     mode_ext;


	/* lowpass and highpass filter control */
	FLOAT   lowpass1, lowpass2; /* normalized frequency bounds of passband */
	FLOAT   highpass1, highpass2; /* normalized frequency bounds of passband */

	int     noise_shaping; /* 0 = none
							  1 = ISO AAC model
							  2 = allow scalefac_select=1
							*/

	int     noise_shaping_amp; /*  0 = ISO model: amplify all distorted bands
								  1 = amplify within 50% of max (on db scale)
								  2 = amplify only most distorted band
								  3 = method 1 and refine with method 2
								*/
	int     substep_shaping; /* 0 = no substep
								1 = use substep shaping at last step(VBR only)
								(not implemented yet)
								2 = use substep inside loop
								3 = use substep inside loop and last step
							  */

	int     psymodel;    /* 1 = gpsycho. 0 = none */
	int     noise_shaping_stop; /* 0 = stop at over=0, all scalefacs amplified or
								   a scalefac has reached max value
								   1 = stop when all scalefacs amplified or
								   a scalefac has reached max value
								   2 = stop when all scalefacs amplified
								 */

	int     subblock_gain; /*  0 = no, 1 = yes */
	int     use_best_huffman; /* 0 = no.  1=outside loop  2=inside loop(slow) */

	int     full_outer_loop; /* 0 = stop early after 0 distortion found. 1 = full search */


	/* variables used by lame.c */
	Bit_stream_struc bs;
	III_side_info_t l3_side;
	FLOAT   ms_ratio[2];

	/* used for padding */
	int     padding;     /* padding for the current frame? */
	int     frac_SpF;
	int     slot_lag;


	/* optional ID3 tags, used in id3tag.c  */
	struct id3tag_spec tag_spec;
	uint16_t nMusicCRC;


	/* variables used by quantize.c */
	int     OldValue[2];
	int     CurrentStep[2];

	FLOAT   masking_lower;
	char    bv_scf[576];
	int     pseudohalf[SFBMAX];

	int     sfb21_extra; /* will be set in lame_init_params */

	/* variables used by util.c */
	/* BPC = maximum number of filter convolution windows to precompute */
#define BPC 320
	sample_t *inbuf_old[2];
	sample_t *blackfilt[2 * BPC + 1];
	double  itime[2];
	int     sideinfo_len;

	/* variables for newmdct.c */
	FLOAT   sb_sample[2][2][18][SBLIMIT];
	FLOAT   amp_filter[32];

	/* variables for bitstream.c */
	/* mpeg1: buffer=511 bytes  smallest frame: 96-38(sideinfo)=58
	 * max number of frames in reservoir:  8
	 * mpeg2: buffer=255 bytes.  smallest frame: 24-23bytes=1
	 * with VBR, if you are encoding all silence, it is possible to
	 * have 8kbs/24khz frames with 1byte of data each, which means we need
	 * to buffer up to 255 headers! */
	/* also, max_header_buf has to be a power of two */
#define MAX_HEADER_BUF 256
#define MAX_HEADER_LEN 40    /* max size of header is 38 */
	struct {
		int     write_timing;
		int     ptr;
		char    buf[MAX_HEADER_LEN];
	} header[MAX_HEADER_BUF];

	int     h_ptr;
	int     w_ptr;
	int     ancillary_flag;

	/* variables for reservoir.c */
	int     ResvSize;    /* in bits */
	int     ResvMax;     /* in bits */

	scalefac_struct scalefac_band;

	/* DATA FROM PSYMODEL.C */
/* The static variables "r", "phi_sav", "new", "old" and "oldest" have    */
/* to be remembered for the unpredictability measure.  For "r" and        */
/* "phi_sav", the first index from the left is the channel select and     */
/* the second index is the "age" of the data.                             */
	FLOAT   minval_l[CBANDS];
	FLOAT   minval_s[CBANDS];
	FLOAT   nb_1[4][CBANDS], nb_2[4][CBANDS];
	FLOAT   nb_s1[4][CBANDS], nb_s2[4][CBANDS];
	FLOAT  *s3_ss;
	FLOAT  *s3_ll;
	FLOAT   decay;

	III_psy_xmin thm[4];
	III_psy_xmin en[4];

	/* fft and energy calculation    */
	FLOAT   tot_ener[4];

	/* loudness calculation (for adaptive threshold of hearing) */
	FLOAT   loudness_sq[2][2]; /* loudness^2 approx. per granule and channel */
	FLOAT   loudness_sq_save[2]; /* account for granule delay of L3psycho_anal */


	/* Scale Factor Bands    */
	FLOAT   mld_l[SBMAX_l], mld_s[SBMAX_s];
	int     bm_l[SBMAX_l], bo_l[SBMAX_l];
	int     bm_s[SBMAX_s], bo_s[SBMAX_s];
	int     npart_l, npart_s;

	int     s3ind[CBANDS][2];
	int     s3ind_s[CBANDS][2];

	int     numlines_s[CBANDS];
	int     numlines_l[CBANDS];
	FLOAT   rnumlines_l[CBANDS];
	FLOAT   mld_cb_l[CBANDS], mld_cb_s[CBANDS];
	int     numlines_s_num1;
	int     numlines_l_num1;

	/* ratios  */
	FLOAT   pe[4];
	FLOAT   ms_ratio_s_old, ms_ratio_l_old;
	FLOAT   ms_ener_ratio_old;

	/* block type */
	int     blocktype_old[2];

	/* CPU features */
	struct {
		unsigned int MMX:1; /* Pentium MMX, Pentium II...IV, K6, K6-2,
							   K6-III, Athlon */
		unsigned int AMD_3DNow:1; /* K6-2, K6-III, Athlon      */
		unsigned int SSE:1; /* Pentium III, Pentium 4    */
		unsigned int SSE2:1; /* Pentium 4, K8             */
	} CPU_features;

	/* functions to replace with CPU feature optimized versions in takehiro.c */
	int     (*choose_table) (const int *ix, const int *const end, int *const s);
	void    (*fft_fht) (FLOAT *, int);
	void    (*init_xrpow_core) (gr_info * const cod_info, FLOAT xrpow[576], int upper,
								FLOAT * sum);



	nsPsy_t nsPsy;       /* variables used for --nspsytune */

	VBR_seek_info_t VBR_seek_table; /* used for Xing VBR header */

	ATH_t  *ATH;         /* all ATH related stuff */
	PSY_t  *PSY;

	int     nogap_total;
	int     nogap_current;


	/* ReplayGain */
	unsigned int decode_on_the_fly:1;
	unsigned int findReplayGain:1;
	unsigned int findPeakSample:1;
	sample_t PeakSample;
	int     RadioGain;
	int     AudiophileGain;
	replaygain_t *rgdata;

	int     noclipGainChange; /* gain change required for preventing clipping */
	FLOAT   noclipScale; /* user-specified scale factor required for preventing clipping */


	/* simple statistics */
	int     bitrate_stereoMode_Hist[16][4 + 1];
	int     bitrate_blockType_Hist[16][4 + 1 + 1]; /*norm/start/short/stop/mixed(short)/sum */

	/* used by the frame analyzer */
	plotting_data *pinfo;
	hip_t hip;

	int     in_buffer_nsamples;
	sample_t *in_buffer_0;
	sample_t *in_buffer_1;

	iteration_loop_t iteration_loop;
};


/***********************************************************************
*
*  Control Parameters set by User.  These parameters are here for
*  backwards compatibility with the old, non-shared lib API.
*  Please use the lame_set_variablename() functions below
*
*
***********************************************************************/
struct lame_global_struct {
    unsigned int class_id;
    /* input description */
    unsigned long num_samples; /* number of samples. default=2^32-1           */
    int     num_channels;    /* input number of channels. default=2         */
    int     in_samplerate;   /* input_samp_rate in Hz. default=44.1 kHz     */
    int     out_samplerate;  /* output_samp_rate.
                                default: LAME picks best value
                                at least not used for MP3 decoding:
                                Remember 44.1 kHz MP3s and AC97           */
    float   scale;           /* scale input by this amount before encoding
                                at least not used for MP3 decoding          */
    float   scale_left;      /* scale input of channel 0 (left) by this
                                amount before encoding                      */
    float   scale_right;     /* scale input of channel 1 (right) by this
                                amount before encoding                      */

    /* general control params */
    int     analysis;        /* collect data for a MP3 frame analyzer?      */
    int     bWriteVbrTag;    /* add Xing VBR tag?                           */
    int     decode_only;     /* use lame/mpglib to convert mp3 to wav       */
    int     quality;         /* quality setting 0=best,  9=worst  default=5 */
    MPEG_mode mode;          /* see enum in lame.h
                                default = LAME picks best value             */
    int     force_ms;        /* force M/S mode.  requires mode=1            */
    int     free_format;     /* use free format? default=0                  */
    int     findReplayGain;  /* find the RG value? default=0       */
    int     decode_on_the_fly; /* decode on the fly? default=0                */
    int     write_id3tag_automatic; /* 1 (default) writes ID3 tags, 0 not */

    /*
     * set either brate>0  or compression_ratio>0, LAME will compute
     * the value of the variable not set.
     * Default is compression_ratio = 11.025
     */
    int     brate;           /* bitrate                                    */
    float   compression_ratio; /* sizeof(wav file)/sizeof(mp3 file)          */


    /* frame params */
    int     copyright;       /* mark as copyright. default=0           */
    int     original;        /* mark as original. default=1            */
    int     extension;       /* the MP3 'private extension' bit.
                                Meaningless                            */
    int     emphasis;        /* Input PCM is emphased PCM (for
                                instance from one of the rarely
                                emphased CDs), it is STRONGLY not
                                recommended to use this, because
                                psycho does not take it into account,
                                and last but not least many decoders
                                don't care about these bits          */
    int     error_protection; /* use 2 bytes per frame for a CRC
                                 checksum. default=0                    */
    int     strict_ISO;      /* enforce ISO spec as much as possible   */

    int     disable_reservoir; /* use bit reservoir?                     */

    /* quantization/noise shaping */
    int     quant_comp;
    int     quant_comp_short;
    int     experimentalY;
    int     experimentalZ;
    int     exp_nspsytune;

    int     preset;

    /* VBR control */
    vbr_mode VBR;
    float   VBR_q_frac;      /* Range [0,...,1[ */
    int     VBR_q;           /* Range [0,...,9] */
    int     VBR_mean_bitrate_kbps;
    int     VBR_min_bitrate_kbps;
    int     VBR_max_bitrate_kbps;
    int     VBR_hard_min;    /* strictly enforce VBR_min_bitrate
                                normaly, it will be violated for analog
                                silence                                 */


    /* resampling and filtering */
    int     lowpassfreq;     /* freq in Hz. 0=lame choses.
                                -1=no filter                          */
    int     highpassfreq;    /* freq in Hz. 0=lame choses.
                                -1=no filter                          */
    int     lowpasswidth;    /* freq width of filter, in Hz
                                (default=15%)                         */
    int     highpasswidth;   /* freq width of filter, in Hz
                                (default=15%)                         */



    /*
     * psycho acoustics and other arguments which you should not change
     * unless you know what you are doing
     */
    float   maskingadjust;
    float   maskingadjust_short;
    int     ATHonly;         /* only use ATH                         */
    int     ATHshort;        /* only use ATH for short blocks        */
    int     noATH;           /* disable ATH                          */
    int     ATHtype;         /* select ATH formula                   */
    float   ATHcurve;        /* change ATH formula 4 shape           */
    float   ATHlower;        /* lower ATH by this many db            */
    int     athaa_type;      /* select ATH auto-adjust scheme        */
    int     athaa_loudapprox; /* select ATH auto-adjust loudness calc */
    float   athaa_sensitivity; /* dB, tune active region of auto-level */
    short_block_t short_blocks;
    int     useTemporal;     /* use temporal masking effect          */
    float   interChRatio;
    float   msfix;           /* Naoki's adjustment of Mid/Side maskings */

    int     tune;            /* 0 off, 1 on */
    float   tune_value_a;    /* used to pass values for debugging and stuff */

    struct {
        void    (*msgf) (const char *format, va_list ap);
        void    (*debugf) (const char *format, va_list ap);
        void    (*errorf) (const char *format, va_list ap);
    } report;

  /************************************************************************/
    /* internal variables, do not set...                                    */
    /* provided because they may be of use to calling application           */
  /************************************************************************/

    int     version;         /* 0=MPEG-2/2.5  1=MPEG-1               */
    int     encoder_delay;
    int     encoder_padding; /* number of samples of padding appended to input */
    int     framesize;
    int     frameNum;        /* number of frames encoded             */
    int     lame_allocated_gfp; /* is this struct owned by calling
                                   program or lame?                     */



  /**************************************************************************/
    /* more internal variables are stored in this structure:                  */
  /**************************************************************************/
    lame_internal_flags *internal_flags;


    struct {
        int     mmx;
        int     amd3dnow;
        int     sse;

    } asm_optimizations;
};


/********************************************************************
 *   initialize internal params based on data in gf
 *   (globalflags struct filled in by calling program)
 *
 *  OUTLINE:
 *
 * We first have some complex code to determine bitrate,
 * output samplerate and mode.  It is complicated by the fact
 * that we allow the user to set some or all of these parameters,
 * and need to determine best possible values for the rest of them:
 *
 *  1. set some CPU related flags
 *  2. check if we are mono->mono, stereo->mono or stereo->stereo
 *  3.  compute bitrate and output samplerate:
 *          user may have set compression ratio
 *          user may have set a bitrate
 *          user may have set a output samplerate
 *  4. set some options which depend on output samplerate
 *  5. compute the actual compression ratio
 *  6. set mode based on compression ratio
 *
 *  The remaining code is much simpler - it just sets options
 *  based on the mode & compression ratio:
 *
 *   set allow_diff_short based on mode
 *   select lowpass filter based on compression ratio & mode
 *   set the bitrate index, and min/max bitrates for VBR modes
 *   disable VBR tag if it is not appropriate
 *   initialize the bitstream
 *   initialize scalefac_band data
 *   set sideinfo_len (based on channels, CRC, out_samplerate)
 *   write an id3v2 tag into the bitstream
 *   write VBR tag into the bitstream
 *   set mpeg1/2 flag
 *   estimate the number of frames (based on a lot of data)
 *
 *   now we set more flags:
 *   nspsytune:
 *      see code
 *   VBR modes
 *      see code
 *   CBR/ABR
 *      see code
 *
 *  Finally, we set the algorithm flags based on the gfp->quality value
 *  lame_init_qval(gfp);
 *
 ********************************************************************/
int
lame_init_params(lame_global_flags * gfp)
{

    int     i;
    int     j;
    lame_internal_flags *const gfc = gfp->internal_flags;

    gfc->Class_ID = 0;

    /* report functions */
    gfc->report.msgf = gfp->report.msgf;
    gfc->report.debugf = gfp->report.debugf;
    gfc->report.errorf = gfp->report.errorf;

    if (gfp->asm_optimizations.amd3dnow)
        gfc->CPU_features.AMD_3DNow = has_3DNow();
    else
        gfc->CPU_features.AMD_3DNow = 0;

    if (gfp->asm_optimizations.mmx)
        gfc->CPU_features.MMX = has_MMX();
    else
        gfc->CPU_features.MMX = 0;

    if (gfp->asm_optimizations.sse) {
        gfc->CPU_features.SSE = has_SSE();
        gfc->CPU_features.SSE2 = has_SSE2();
    }
    else {
        gfc->CPU_features.SSE = 0;
        gfc->CPU_features.SSE2 = 0;
    }


    if (NULL == gfc->ATH)
        gfc->ATH = calloc(1, sizeof(ATH_t));

    if (NULL == gfc->ATH)
        return -2;      /* maybe error codes should be enumerated in lame.h ?? */

    if (NULL == gfc->PSY)
        gfc->PSY = calloc(1, sizeof(PSY_t));
    if (NULL == gfc->PSY) {
        freegfc(gfc);
        gfp->internal_flags = NULL;
        return -2;
    }

    if (NULL == gfc->rgdata)
        gfc->rgdata = calloc(1, sizeof(replaygain_t));
    if (NULL == gfc->rgdata) {
        freegfc(gfc);
        gfp->internal_flags = NULL;
        return -2;
    }

    gfc->channels_in = gfp->num_channels;
    if (gfc->channels_in == 1)
        gfp->mode = MONO;
    gfc->channels_out = (gfp->mode == MONO) ? 1 : 2;
    gfc->mode_ext = MPG_MD_MS_LR;
    if (gfp->mode == MONO)
        gfp->force_ms = 0; /* don't allow forced mid/side stereo for mono output */

    if (gfp->VBR == vbr_off && gfp->VBR_mean_bitrate_kbps != 128 && gfp->brate == 0)
        gfp->brate = gfp->VBR_mean_bitrate_kbps;

    switch (gfp->VBR) {
    case vbr_off:
    case vbr_mtrh:
    case vbr_mt:
        /* these modes can handle free format condition */
        break;
    default:
        gfp->free_format = 0; /* mode can't be mixed with free format */
        break;
    }

    if (gfp->VBR == vbr_off && gfp->brate == 0) {
        /* no bitrate or compression ratio specified, use 11.025 */
        if (EQ(gfp->compression_ratio, 0))
            gfp->compression_ratio = 11.025; /* rate to compress a CD down to exactly 128000 bps */
    }

    /* find bitrate if user specify a compression ratio */
    if (gfp->VBR == vbr_off && gfp->compression_ratio > 0) {

        if (gfp->out_samplerate == 0)
            gfp->out_samplerate = map2MP3Frequency((int) (0.97 * gfp->in_samplerate)); /* round up with a margin of 3% */

        /* choose a bitrate for the output samplerate which achieves
         * specified compression ratio
         */
        gfp->brate = gfp->out_samplerate * 16 * gfc->channels_out / (1.e3 * gfp->compression_ratio);

        /* we need the version for the bitrate table look up */
        gfc->samplerate_index = SmpFrqIndex(gfp->out_samplerate, &gfp->version);

        if (!gfp->free_format) /* for non Free Format find the nearest allowed bitrate */
            gfp->brate = FindNearestBitrate(gfp->brate, gfp->version, gfp->out_samplerate);
    }
    if (gfp->out_samplerate) {
        if (gfp->out_samplerate < 16000) {
            gfp->VBR_mean_bitrate_kbps = Max(gfp->VBR_mean_bitrate_kbps, 8);
            gfp->VBR_mean_bitrate_kbps = Min(gfp->VBR_mean_bitrate_kbps, 64);
        }
        else if (gfp->out_samplerate < 32000) {
            gfp->VBR_mean_bitrate_kbps = Max(gfp->VBR_mean_bitrate_kbps, 8);
            gfp->VBR_mean_bitrate_kbps = Min(gfp->VBR_mean_bitrate_kbps, 160);
        }
        else {
            gfp->VBR_mean_bitrate_kbps = Max(gfp->VBR_mean_bitrate_kbps, 32);
            gfp->VBR_mean_bitrate_kbps = Min(gfp->VBR_mean_bitrate_kbps, 320);
        }
    }

  /****************************************************************/
    /* if a filter has not been enabled, see if we should add one: */
  /****************************************************************/
    if (gfp->lowpassfreq == 0) {
        double  lowpass = 16000;
        double  highpass;

        switch (gfp->VBR) {
        case vbr_off:{
                optimum_bandwidth(&lowpass, &highpass, gfp->brate);
                break;
            }
        case vbr_abr:{
                optimum_bandwidth(&lowpass, &highpass, gfp->VBR_mean_bitrate_kbps);
                break;
            }
        case vbr_rh:{
                int const x[11] = {
                    19500, 19000, 18600, 18000, 17500, 16000, 15600, 14900, 12500, 10000, 3950
                };
                if (0 <= gfp->VBR_q && gfp->VBR_q <= 9) {
                    double  a = x[gfp->VBR_q], b = x[gfp->VBR_q + 1], m = gfp->VBR_q_frac;
                    lowpass = linear_int(a, b, m);
                }
                else {
                    lowpass = 19500;
                }
                break;
            }
        default:{
                int const x[11] = {
                    19500, 19000, 18500, 18000, 17500, 16500, 15500, 14500, 12500, 9500, 3950
                };
                if (0 <= gfp->VBR_q && gfp->VBR_q <= 9) {
                    double  a = x[gfp->VBR_q], b = x[gfp->VBR_q + 1], m = gfp->VBR_q_frac;
                    lowpass = linear_int(a, b, m);
                }
                else {
                    lowpass = 19500;
                }
            }
        }

        if (gfp->mode == MONO && (gfp->VBR == vbr_off || gfp->VBR == vbr_abr))
            lowpass *= 1.5;

        gfp->lowpassfreq = lowpass;
    }

    if (gfp->out_samplerate == 0) {
        if (2 * gfp->lowpassfreq > gfp->in_samplerate) {
            gfp->lowpassfreq = gfp->in_samplerate / 2;
        }
        gfp->out_samplerate = optimum_samplefreq((int) gfp->lowpassfreq, gfp->in_samplerate);
    }

    gfp->lowpassfreq = Min(20500, gfp->lowpassfreq);
    gfp->lowpassfreq = Min(gfp->out_samplerate / 2, gfp->lowpassfreq);

    if (gfp->VBR == vbr_off) {
        gfp->compression_ratio = gfp->out_samplerate * 16 * gfc->channels_out / (1.e3 * gfp->brate);
    }
    if (gfp->VBR == vbr_abr) {
        gfp->compression_ratio =
            gfp->out_samplerate * 16 * gfc->channels_out / (1.e3 * gfp->VBR_mean_bitrate_kbps);
    }

    /* do not compute ReplayGain values and do not find the peak sample
       if we can't store them */
    if (!gfp->bWriteVbrTag) {
        gfp->findReplayGain = 0;
        gfp->decode_on_the_fly = 0;
        gfc->findPeakSample = 0;
    }
    gfc->findReplayGain = gfp->findReplayGain;
    gfc->decode_on_the_fly = gfp->decode_on_the_fly;

    if (gfc->decode_on_the_fly)
        gfc->findPeakSample = 1;

    if (gfc->findReplayGain) {
        if (InitGainAnalysis(gfc->rgdata, gfp->out_samplerate) == INIT_GAIN_ANALYSIS_ERROR) {
            freegfc(gfc);
            gfp->internal_flags = NULL;
            return -6;
        }
    }

#ifdef DECODE_ON_THE_FLY
    if (gfc->decode_on_the_fly && !gfp->decode_only) {
        if (gfc->hip) {
            hip_decode_exit(gfc->hip);
        }
        gfc->hip = hip_decode_init();
    }
#endif

    gfc->mode_gr = gfp->out_samplerate <= 24000 ? 1 : 2; /* Number of granules per frame */
    gfp->framesize = 576 * gfc->mode_gr;
    gfp->encoder_delay = ENCDELAY;

    gfc->resample_ratio = (double) gfp->in_samplerate / gfp->out_samplerate;

    /*
     *  sample freq       bitrate     compression ratio
     *     [kHz]      [kbps/channel]   for 16 bit input
     *     44.1            56               12.6
     *     44.1            64               11.025
     *     44.1            80                8.82
     *     22.05           24               14.7
     *     22.05           32               11.025
     *     22.05           40                8.82
     *     16              16               16.0
     *     16              24               10.667
     *
     */
    /*
     *  For VBR, take a guess at the compression_ratio.
     *  For example:
     *
     *    VBR_q    compression     like
     *     -        4.4         320 kbps/44 kHz
     *   0...1      5.5         256 kbps/44 kHz
     *     2        7.3         192 kbps/44 kHz
     *     4        8.8         160 kbps/44 kHz
     *     6       11           128 kbps/44 kHz
     *     9       14.7          96 kbps
     *
     *  for lower bitrates, downsample with --resample
     */

    switch (gfp->VBR) {
    case vbr_mt:
    case vbr_rh:
    case vbr_mtrh:
        {
            /*numbers are a bit strange, but they determine the lowpass value */
            FLOAT const cmp[] = { 5.7, 6.5, 7.3, 8.2, 10, 11.9, 13, 14, 15, 16.5 };
            gfp->compression_ratio = cmp[gfp->VBR_q];
        }
        break;
    case vbr_abr:
        gfp->compression_ratio =
            gfp->out_samplerate * 16 * gfc->channels_out / (1.e3 * gfp->VBR_mean_bitrate_kbps);
        break;
    default:
        gfp->compression_ratio = gfp->out_samplerate * 16 * gfc->channels_out / (1.e3 * gfp->brate);
        break;
    }


    /* mode = -1 (not set by user) or
     * mode = MONO (because of only 1 input channel).
     * If mode has not been set, then select J-STEREO
     */
    if (gfp->mode == NOT_SET) {
        gfp->mode = JOINT_STEREO;
    }


    /* apply user driven high pass filter */
    if (gfp->highpassfreq > 0) {
        gfc->highpass1 = 2. * gfp->highpassfreq;

        if (gfp->highpasswidth >= 0)
            gfc->highpass2 = 2. * (gfp->highpassfreq + gfp->highpasswidth);
        else            /* 0% above on default */
            gfc->highpass2 = (1 + 0.00) * 2. * gfp->highpassfreq;

        gfc->highpass1 /= gfp->out_samplerate;
        gfc->highpass2 /= gfp->out_samplerate;
    }
    else {
        gfc->highpass1 = 0;
        gfc->highpass2 = 0;
    }
    /* apply user driven low pass filter */
    if (gfp->lowpassfreq > 0) {
        gfc->lowpass2 = 2. * gfp->lowpassfreq;
        if (gfp->lowpasswidth >= 0) {
            gfc->lowpass1 = 2. * (gfp->lowpassfreq - gfp->lowpasswidth);
            if (gfc->lowpass1 < 0) /* has to be >= 0 */
                gfc->lowpass1 = 0;
        }
        else {          /* 0% below on default */
            gfc->lowpass1 = (1 - 0.00) * 2. * gfp->lowpassfreq;
        }
        gfc->lowpass1 /= gfp->out_samplerate;
        gfc->lowpass2 /= gfp->out_samplerate;
    }
    else {
        gfc->lowpass1 = 0;
        gfc->lowpass2 = 0;
    }




  /**********************************************************************/
    /* compute info needed for polyphase filter (filter type==0, default) */
  /**********************************************************************/
    lame_init_params_ppflt(gfp);


  /*******************************************************
   * samplerate and bitrate index
   *******************************************************/
    gfc->samplerate_index = SmpFrqIndex(gfp->out_samplerate, &gfp->version);
    if (gfc->samplerate_index < 0) {
        freegfc(gfc);
        gfp->internal_flags = NULL;
        return -1;
    }

    if (gfp->VBR == vbr_off) {
        if (gfp->free_format) {
            gfc->bitrate_index = 0;
        }
        else {
            gfp->brate = FindNearestBitrate(gfp->brate, gfp->version, gfp->out_samplerate);
            gfc->bitrate_index = BitrateIndex(gfp->brate, gfp->version, gfp->out_samplerate);
            if (gfc->bitrate_index <= 0) {
                freegfc(gfc);
                gfp->internal_flags = NULL;
                return -1;
            }
        }
    }
    else {
        gfc->bitrate_index = 1;
    }

    /* for CBR, we will write an "info" tag. */
    /*    if ((gfp->VBR == vbr_off))  */
    /*  gfp->bWriteVbrTag = 0; */

    if (gfp->analysis)
        gfp->bWriteVbrTag = 0;

    /* some file options not allowed if output is: not specified or stdout */
    if (gfc->pinfo != NULL)
        gfp->bWriteVbrTag = 0; /* disable Xing VBR tag */

    init_bit_stream_w(gfc);

    j = gfc->samplerate_index + (3 * gfp->version) + 6 * (gfp->out_samplerate < 16000);
    for (i = 0; i < SBMAX_l + 1; i++)
        gfc->scalefac_band.l[i] = sfBandIndex[j].l[i];

    for (i = 0; i < PSFB21 + 1; i++) {
        int const size = (gfc->scalefac_band.l[22] - gfc->scalefac_band.l[21]) / PSFB21;
        int const start = gfc->scalefac_band.l[21] + i * size;
        gfc->scalefac_band.psfb21[i] = start;
    }
    gfc->scalefac_band.psfb21[PSFB21] = 576;

    for (i = 0; i < SBMAX_s + 1; i++)
        gfc->scalefac_band.s[i] = sfBandIndex[j].s[i];

    for (i = 0; i < PSFB12 + 1; i++) {
        int const size = (gfc->scalefac_band.s[13] - gfc->scalefac_band.s[12]) / PSFB12;
        int const start = gfc->scalefac_band.s[12] + i * size;
        gfc->scalefac_band.psfb12[i] = start;
    }
    gfc->scalefac_band.psfb12[PSFB12] = 192;

    /* determine the mean bitrate for main data */
    if (gfp->version == 1) /* MPEG 1 */
        gfc->sideinfo_len = (gfc->channels_out == 1) ? 4 + 17 : 4 + 32;
    else                /* MPEG 2 */
        gfc->sideinfo_len = (gfc->channels_out == 1) ? 4 + 9 : 4 + 17;

    if (gfp->error_protection)
        gfc->sideinfo_len += 2;

    (void) lame_init_bitstream(gfp);

    gfc->Class_ID = LAME_ID;

    /*if (gfp->exp_nspsytune & 1) */  {
        int     k;

        for (k = 0; k < 19; k++)
            gfc->nsPsy.pefirbuf[k] = 700 * gfc->mode_gr * gfc->channels_out;

        if (gfp->ATHtype == -1)
            gfp->ATHtype = 4;
    }

    assert(gfp->VBR_q <= 9);
    assert(gfp->VBR_q >= 0);

    switch (gfp->VBR) {

    case vbr_mt:
        gfp->VBR = vbr_mtrh;
        /*lint --fallthrough */
    case vbr_mtrh:{
            if (gfp->useTemporal < 0) {
                gfp->useTemporal = 0; /* off by default for this VBR mode */
            }

            (void) apply_preset(gfp, 500 - (gfp->VBR_q * 10), 0);
            /*  The newer VBR code supports only a limited
               subset of quality levels:
               9-5=5 are the same, uses x^3/4 quantization
               4-0=0 are the same  5 plus best huffman divide code
             */
            if (gfp->quality < 0)
                gfp->quality = LAME_DEFAULT_QUALITY;
            if (gfp->quality < 5)
                gfp->quality = 0;
            if (gfp->quality > 5)
                gfp->quality = 5;

            gfc->PSY->mask_adjust = gfp->maskingadjust;
            gfc->PSY->mask_adjust_short = gfp->maskingadjust_short;

            /*  sfb21 extra only with MPEG-1 at higher sampling rates
             */
            if (gfp->experimentalY)
                gfc->sfb21_extra = 0;
            else
                gfc->sfb21_extra = (gfp->out_samplerate > 44000);

            gfc->iteration_loop = VBR_new_iteration_loop;
            break;

        }
    case vbr_rh:{

            (void) apply_preset(gfp, 500 - (gfp->VBR_q * 10), 0);

            gfc->PSY->mask_adjust = gfp->maskingadjust;
            gfc->PSY->mask_adjust_short = gfp->maskingadjust_short;

            /*  sfb21 extra only with MPEG-1 at higher sampling rates
             */
            if (gfp->experimentalY)
                gfc->sfb21_extra = 0;
            else
                gfc->sfb21_extra = (gfp->out_samplerate > 44000);

            /*  VBR needs at least the output of GPSYCHO,
             *  so we have to garantee that by setting a minimum
             *  quality level, actually level 6 does it.
             *  down to level 6
             */
            if (gfp->quality > 6)
                gfp->quality = 6;


            if (gfp->quality < 0)
                gfp->quality = LAME_DEFAULT_QUALITY;

            gfc->iteration_loop = VBR_old_iteration_loop;
            break;
        }

    default:           /* cbr/abr */  {
            vbr_mode vbrmode;

            /*  no sfb21 extra with CBR code
             */
            gfc->sfb21_extra = 0;

            if (gfp->quality < 0)
                gfp->quality = LAME_DEFAULT_QUALITY;


            vbrmode = lame_get_VBR(gfp);
            if (vbrmode == vbr_off)
                (void) lame_set_VBR_mean_bitrate_kbps(gfp, gfp->brate);
            /* second, set parameters depending on bitrate */
            (void) apply_preset(gfp, gfp->VBR_mean_bitrate_kbps, 0);
            (void) lame_set_VBR(gfp, vbrmode);

            gfc->PSY->mask_adjust = gfp->maskingadjust;
            gfc->PSY->mask_adjust_short = gfp->maskingadjust_short;

            if (vbrmode == vbr_off) {
                gfc->iteration_loop = CBR_iteration_loop;
            }
            else {
                gfc->iteration_loop = ABR_iteration_loop;
            }
            break;
        }
    }

    /*initialize default values common for all modes */


    if (lame_get_VBR(gfp) != vbr_off) { /* choose a min/max bitrate for VBR */
        /* if the user didn't specify VBR_max_bitrate: */
        gfc->VBR_min_bitrate = 1; /* default: allow   8 kbps (MPEG-2) or  32 kbps (MPEG-1) */
        gfc->VBR_max_bitrate = 14; /* default: allow 160 kbps (MPEG-2) or 320 kbps (MPEG-1) */
        if (gfp->out_samplerate < 16000)
            gfc->VBR_max_bitrate = 8; /* default: allow 64 kbps (MPEG-2.5) */
        if (gfp->VBR_min_bitrate_kbps) {
            gfp->VBR_min_bitrate_kbps =
                FindNearestBitrate(gfp->VBR_min_bitrate_kbps, gfp->version, gfp->out_samplerate);
            gfc->VBR_min_bitrate =
                BitrateIndex(gfp->VBR_min_bitrate_kbps, gfp->version, gfp->out_samplerate);
            if (gfc->VBR_min_bitrate < 0)
                return -1;
        }
        if (gfp->VBR_max_bitrate_kbps) {
            gfp->VBR_max_bitrate_kbps =
                FindNearestBitrate(gfp->VBR_max_bitrate_kbps, gfp->version, gfp->out_samplerate);
            gfc->VBR_max_bitrate =
                BitrateIndex(gfp->VBR_max_bitrate_kbps, gfp->version, gfp->out_samplerate);
            if (gfc->VBR_max_bitrate < 0)
                return -1;
        }
        gfp->VBR_min_bitrate_kbps = bitrate_table[gfp->version][gfc->VBR_min_bitrate];
        gfp->VBR_max_bitrate_kbps = bitrate_table[gfp->version][gfc->VBR_max_bitrate];
        gfp->VBR_mean_bitrate_kbps =
            Min(bitrate_table[gfp->version][gfc->VBR_max_bitrate], gfp->VBR_mean_bitrate_kbps);
        gfp->VBR_mean_bitrate_kbps =
            Max(bitrate_table[gfp->version][gfc->VBR_min_bitrate], gfp->VBR_mean_bitrate_kbps);
    }


    /*  just another daily changing developer switch  */
    if (gfp->tune) {
        gfc->PSY->mask_adjust += gfp->tune_value_a;
        gfc->PSY->mask_adjust_short += gfp->tune_value_a;
    }

    /* initialize internal qval settings */
    lame_init_qval(gfp);


    /*  automatic ATH adjustment on
     */
    if (gfp->athaa_type < 0)
        gfc->ATH->use_adjust = 3;
    else
        gfc->ATH->use_adjust = gfp->athaa_type;


    /* initialize internal adaptive ATH settings  -jd */
    gfc->ATH->aa_sensitivity_p = pow(10.0, gfp->athaa_sensitivity / -10.0);


    if (gfp->short_blocks == short_block_not_set) {
        gfp->short_blocks = short_block_allowed;
    }

    /*Note Jan/2003: Many hardware decoders cannot handle short blocks in regular
       stereo mode unless they are coupled (same type in both channels)
       it is a rare event (1 frame per min. or so) that LAME would use
       uncoupled short blocks, so lets turn them off until we decide
       how to handle this.  No other encoders allow uncoupled short blocks,
       even though it is in the standard.  */
    /* rh 20040217: coupling makes no sense for mono and dual-mono streams
     */
    if (gfp->short_blocks == short_block_allowed
        && (gfp->mode == JOINT_STEREO || gfp->mode == STEREO)) {
        gfp->short_blocks = short_block_coupled;
    }


    if (lame_get_quant_comp(gfp) < 0)
        (void) lame_set_quant_comp(gfp, 1);
    if (lame_get_quant_comp_short(gfp) < 0)
        (void) lame_set_quant_comp_short(gfp, 0);

    if (lame_get_msfix(gfp) < 0)
        lame_set_msfix(gfp, 0);

    /* select psychoacoustic model */
    (void) lame_set_exp_nspsytune(gfp, lame_get_exp_nspsytune(gfp) | 1);

    if (lame_get_short_threshold_lrm(gfp) < 0)
        (void) lame_set_short_threshold_lrm(gfp, NSATTACKTHRE);
    if (lame_get_short_threshold_s(gfp) < 0)
        (void) lame_set_short_threshold_s(gfp, NSATTACKTHRE_S);

    if (gfp->scale < 0)
        gfp->scale = 1;

    if (gfp->ATHtype < 0)
        gfp->ATHtype = 4;

    if (gfp->ATHcurve < 0)
        gfp->ATHcurve = 4;

    if (gfp->athaa_loudapprox < 0)
        gfp->athaa_loudapprox = 2;

    if (gfp->interChRatio < 0)
        gfp->interChRatio = 0;

    if (gfp->useTemporal < 0)
        gfp->useTemporal = 1; /* on by default */



    /* padding method as described in
     * "MPEG-Layer3 / Bitstream Syntax and Decoding"
     * by Martin Sieler, Ralph Sperschneider
     *
     * note: there is no padding for the very first frame
     *
     * Robert Hegemann 2000-06-22
     */
    gfc->slot_lag = gfc->frac_SpF = 0;
    if (gfp->VBR == vbr_off)
        gfc->slot_lag = gfc->frac_SpF
            = ((gfp->version + 1) * 72000L * gfp->brate) % gfp->out_samplerate;

    iteration_init(gfp);
    (void) psymodel_init(gfp);

    return 0;
}


/*
 * THE MAIN LAME ENCODING INTERFACE
 * mt 3/00
 *
 * input pcm data, output (maybe) mp3 frames.
 * This routine handles all buffering, resampling and filtering for you.
 * The required mp3buffer_size can be computed from num_samples,
 * samplerate and encoding rate, but here is a worst case estimate:
 *
 * mp3buffer_size in bytes = 1.25*num_samples + 7200
 *
 * return code = number of bytes output in mp3buffer.  can be 0
 *
 * NOTE: this routine uses LAME's internal PCM data representation,
 * 'sample_t'.  It should not be used by any application.
 * applications should use lame_encode_buffer(),
 *                         lame_encode_buffer_float()
 *                         lame_encode_buffer_int()
 * etc... depending on what type of data they are working with.
*/
static int
lame_encode_buffer_sample_t(lame_global_flags * gfp,
                            sample_t buffer_l[],
                            sample_t buffer_r[],
                            int nsamples, unsigned char *mp3buf, const int mp3buf_size)
{
    lame_internal_flags *const gfc = gfp->internal_flags;
    int     mp3size = 0, ret, i, ch, mf_needed;
    int     mp3out;
    sample_t *mfbuf[2];
    sample_t *in_buffer[2];

    if (gfc->Class_ID != LAME_ID)
        return -3;

    if (nsamples == 0)
        return 0;

    /* copy out any tags that may have been written into bitstream */
    mp3out = copy_buffer(gfc, mp3buf, mp3buf_size, 0);
    if (mp3out < 0)
        return mp3out;  /* not enough buffer space */
    mp3buf += mp3out;
    mp3size += mp3out;


    in_buffer[0] = buffer_l;
    in_buffer[1] = buffer_r;


    /* Apply user defined re-scaling */

    /* user selected scaling of the samples */
    if (NEQ(gfp->scale, 0) && NEQ(gfp->scale, 1.0)) {
        for (i = 0; i < nsamples; ++i) {
            in_buffer[0][i] *= gfp->scale;
            if (gfc->channels_out == 2)
                in_buffer[1][i] *= gfp->scale;
        }
    }

    /* user selected scaling of the channel 0 (left) samples */
    if (NEQ(gfp->scale_left, 0) && NEQ(gfp->scale_left, 1.0)) {
        for (i = 0; i < nsamples; ++i) {
            in_buffer[0][i] *= gfp->scale_left;
        }
    }

    /* user selected scaling of the channel 1 (right) samples */
    if (NEQ(gfp->scale_right, 0) && NEQ(gfp->scale_right, 1.0)) {
        for (i = 0; i < nsamples; ++i) {
            in_buffer[1][i] *= gfp->scale_right;
        }
    }

    /* Downsample to Mono if 2 channels in and 1 channel out */
    if (gfp->num_channels == 2 && gfc->channels_out == 1) {
        for (i = 0; i < nsamples; ++i) {
            in_buffer[0][i] = 0.5 * ((FLOAT) in_buffer[0][i] + in_buffer[1][i]);
            in_buffer[1][i] = 0.0;
        }
    }

    mf_needed = calcNeeded(gfp);

    mfbuf[0] = gfc->mfbuf[0];
    mfbuf[1] = gfc->mfbuf[1];

    while (nsamples > 0) {
        sample_t const *in_buffer_ptr[2];
        int     n_in = 0;    /* number of input samples processed with fill_buffer */
        int     n_out = 0;   /* number of samples output with fill_buffer */
        /* n_in <> n_out if we are resampling */

        in_buffer_ptr[0] = in_buffer[0];
        in_buffer_ptr[1] = in_buffer[1];
        /* copy in new samples into mfbuf, with resampling */
        fill_buffer(gfp, mfbuf, &in_buffer_ptr[0], nsamples, &n_in, &n_out);

        /* compute ReplayGain of resampled input if requested */
        if (gfc->findReplayGain && !gfc->decode_on_the_fly)
            if (AnalyzeSamples
                (gfc->rgdata, &mfbuf[0][gfc->mf_size], &mfbuf[1][gfc->mf_size], n_out,
                 gfc->channels_out) == GAIN_ANALYSIS_ERROR)
                return -6;



        /* update in_buffer counters */
        nsamples -= n_in;
        in_buffer[0] += n_in;
        if (gfc->channels_out == 2)
            in_buffer[1] += n_in;

        /* update mfbuf[] counters */
        gfc->mf_size += n_out;
        assert(gfc->mf_size <= MFSIZE);

        /* lame_encode_flush may have set gfc->mf_sample_to_encode to 0
         * so we have to reinitialize it here when that happened.
         */
        if (gfc->mf_samples_to_encode < 1) {
            gfc->mf_samples_to_encode = ENCDELAY + POSTDELAY;
        }
        gfc->mf_samples_to_encode += n_out;


        if (gfc->mf_size >= mf_needed) {
            /* encode the frame.  */
            /* mp3buf              = pointer to current location in buffer */
            /* mp3buf_size         = size of original mp3 output buffer */
            /*                     = 0 if we should not worry about the */
            /*                       buffer size because calling program is  */
            /*                       to lazy to compute it */
            /* mp3size             = size of data written to buffer so far */
            /* mp3buf_size-mp3size = amount of space avalable  */

            int     buf_size = mp3buf_size - mp3size;
            if (mp3buf_size == 0)
                buf_size = 0;

            ret = lame_encode_frame(gfp, mfbuf[0], mfbuf[1], mp3buf, buf_size);

            if (ret < 0)
                return ret;
            mp3buf += ret;
            mp3size += ret;

            /* shift out old samples */
            gfc->mf_size -= gfp->framesize;
            gfc->mf_samples_to_encode -= gfp->framesize;
            for (ch = 0; ch < gfc->channels_out; ch++)
                for (i = 0; i < gfc->mf_size; i++)
                    mfbuf[ch][i] = mfbuf[ch][i + gfp->framesize];
        }
    }
    assert(nsamples == 0);

    return mp3size;
}


int
lame_encode_buffer(lame_global_flags * gfp,
                   const short int buffer_l[],
                   const short int buffer_r[],
                   const int nsamples, unsigned char *mp3buf, const int mp3buf_size)
{
    lame_internal_flags *const gfc = gfp->internal_flags;
    int     i;
    sample_t *in_buffer[2];

    if (gfc->Class_ID != LAME_ID)
        return -3;

    if (nsamples == 0)
        return 0;

    if (update_inbuffer_size(gfc, nsamples) != 0) {
        return -2;
    }

    in_buffer[0] = gfc->in_buffer_0;
    in_buffer[1] = gfc->in_buffer_1;

    /* make a copy of input buffer, changing type to sample_t */
    for (i = 0; i < nsamples; i++) {
        in_buffer[0][i] = buffer_l[i];
        if (gfc->channels_in > 1)
            in_buffer[1][i] = buffer_r[i];
    }

    return lame_encode_buffer_sample_t(gfp, in_buffer[0], in_buffer[1],
                                       nsamples, mp3buf, mp3buf_size);
}


int
lame_encode_buffer_float(lame_global_flags * gfp,
                         const float buffer_l[],
                         const float buffer_r[],
                         const int nsamples, unsigned char *mp3buf, const int mp3buf_size)
{
    lame_internal_flags *const gfc = gfp->internal_flags;
    int     i;
    sample_t *in_buffer[2];

    if (gfc->Class_ID != LAME_ID)
        return -3;

    if (nsamples == 0)
        return 0;

    if (update_inbuffer_size(gfc, nsamples) != 0) {
        return -2;
    }

    in_buffer[0] = gfc->in_buffer_0;
    in_buffer[1] = gfc->in_buffer_1;

    /* make a copy of input buffer, changing type to sample_t */
    for (i = 0; i < nsamples; i++) {
        in_buffer[0][i] = buffer_l[i];
        if (gfc->channels_in > 1)
            in_buffer[1][i] = buffer_r[i];
    }

    return lame_encode_buffer_sample_t(gfp, in_buffer[0], in_buffer[1],
                                       nsamples, mp3buf, mp3buf_size);
}


int
lame_encode_buffer_int(lame_global_flags * gfp,
                       const int buffer_l[],
                       const int buffer_r[],
                       const int nsamples, unsigned char *mp3buf, const int mp3buf_size)
{
    lame_internal_flags *const gfc = gfp->internal_flags;
    int     i;
    sample_t *in_buffer[2];

    if (gfc->Class_ID != LAME_ID)
        return -3;

    if (nsamples == 0)
        return 0;

    if (update_inbuffer_size(gfc, nsamples) != 0) {
        return -2;
    }

    in_buffer[0] = gfc->in_buffer_0;
    in_buffer[1] = gfc->in_buffer_1;

    /* make a copy of input buffer, changing type to sample_t */
    for (i = 0; i < nsamples; i++) {
        /* internal code expects +/- 32768.0 */
        in_buffer[0][i] = buffer_l[i] * (1.0 / (1L << (8 * sizeof(int) - 16)));
        if (gfc->channels_in > 1)
            in_buffer[1][i] = buffer_r[i] * (1.0 / (1L << (8 * sizeof(int) - 16)));
    }

    return lame_encode_buffer_sample_t(gfp, in_buffer[0], in_buffer[1],
                                       nsamples, mp3buf, mp3buf_size);
}

