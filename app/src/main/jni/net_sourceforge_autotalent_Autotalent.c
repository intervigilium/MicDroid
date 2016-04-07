/*
 * Autotalent library for Android
 *
 * Copyright (c) 2010-2016 Ethan Chen
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

#define LOG_TAG "Autotalent"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include <android/log.h>

#include "autotalent.h"
#include "jni.h"
#include "ndk_log.h"

#define MAX_SHORT 32767
#define MIN_SHORT -32768

#define ARRAY_SIZE(x) (sizeof(x) / sizeof((x)[0]))

static Autotalent *instance;

static void
mixBuffers(int16_t *out, int16_t *buf1, int16_t *buf2, size_t len)
{
    for (int i = 0; i < len; i++) {
        // formula for mixing from: http://www.vttoth.com/digimix.htm
        int sum = (int)(buf1[i] + buf2[i]);
        int mul = (int)(buf1[i] * buf2[i]) >> FP_DIGITS;
        int res = sum - mul;
        out[i] = (int16_t)res;
    }
}

static void
native_create_autotalent(JNIEnv *env,
        jobject thiz, jint sampleRate)
{
    if (instance == NULL) {
        instance = instantiateAutotalent(sampleRate);
        ALOGD("instantiated autotalent with sample rate: %d",
                sampleRate);
    }
}

static void
native_set_concert_a(JNIEnv *env, jobject thiz, jfloat concertA)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfTune) = (float)concertA;
}

static void
native_set_key(JNIEnv *env, jobject thiz, jchar key)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    setAutotalentKey(instance, (char *)&key);
}

static void
native_set_fixed_pitch(JNIEnv *env, jobject thiz, jfloat fixed)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfFixed) = (float)fixed;
}

static void
native_set_fixed_pull(JNIEnv *env, jobject thiz, jfloat pull)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfPull) = (float)pull;
}

static void
native_set_strength(JNIEnv *env, jobject thiz, jfloat strength)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfAmount) = (float)strength;
}

static void
native_set_smoothness(JNIEnv *env, jobject thiz, jfloat smooth)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfSmooth) = (float)smooth;
}

static void
native_set_pitch_shift(JNIEnv *env, jobject thiz, jfloat shift)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfShift) = (float)shift;
}

static void
native_set_scale_rotate(JNIEnv *env, jobject thiz, jint rotate)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfScwarp) = (int)rotate;
}

static void
native_set_lfo_depth(JNIEnv *env, jobject thiz, jfloat depth)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfLfoamp) = (float)depth;
}

static void
native_set_lfo_rate(JNIEnv *env, jobject thiz, jfloat rate)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfLforate) = (float)rate;
}

static void
native_set_lfo_shape(JNIEnv *env, jobject thiz, jfloat shape)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfLfoshape) = (float)shape;
}

static void
native_set_lfo_symmetric(JNIEnv *env, jobject thiz, jfloat symmetric)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfLfosymm) = (float)symmetric;
}

static void
native_set_lfo_quantization(JNIEnv *env, jobject thiz, jint quantization)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfLfoquant) = (int)quantization;
}

static void
native_enable_formant_correction(JNIEnv *env, jobject thiz, jboolean enabled)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfFcorr) = (int)enabled;
}

static void
native_enable_formant_warp(JNIEnv *env, jobject thiz, jfloat warp)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }
    *(instance->m_pfFwarp) = (float)warp;
}

static void
native_set_mix(JNIEnv *env, jobject thiz, jfloat mix)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    *(instance->m_pfMix) = (float)mix;
}

static void
native_process_samples(JNIEnv *env, jobject thiz, jshortArray samples,
        jint numSamples)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    // copy buffers
    int16_t *samplebuf =
        (int16_t *)(*env)->GetPrimitiveArrayCritical(env, samples, 0);

    setAutotalentBuffers(instance, samplebuf, samplebuf);

    // process samples
    runAutotalent(instance, numSamples);

    // copy results back up to java array
    (*env)->ReleasePrimitiveArrayCritical(env, samples, samplebuf, 0);
}

static void
native_process_samples_instrumental(JNIEnv *env, jobject thiz,
        jshortArray samples, jshortArray instrumental, jint numSamples)
{
    if (instance == NULL) {
        ALOGW("No suitable autotalent instance found!");
        return;
    }

    int16_t *samplebuf =
        (int16_t *)(*env)->GetPrimitiveArrayCritical(env, samples, 0);
    setAutotalentBuffers(instance, samplebuf, samplebuf);

    // process samples
    runAutotalent(instance, numSamples);

    // mix instrumental samples with tuned recorded samples
    int16_t *instrumentalbuf =
        (int16_t *)(*env)->GetPrimitiveArrayCritical(env, instrumental, 0);
    mixBuffers(samplebuf, samplebuf, instrumentalbuf, numSamples);
    (*env)->ReleasePrimitiveArrayCritical(env, instrumental,
            instrumentalbuf, 0);

    // copy results back up to java array
    (*env)->ReleasePrimitiveArrayCritical(env, samples, samplebuf, 0);
}

static void
native_destroy_autotalent(JNIEnv *env, jobject thiz)
{
    if (instance != NULL) {
        cleanupAutotalent(instance);
        ALOGD("cleaned up autotalent at 0x%p", instance);
        instance = NULL;
    }
}

static const char *classPathName = "net/sourceforge/autotalent/Autotalent";

static JNINativeMethod gMethods[] = {
    { "native_createAutotalent", "(I)V", (void *) native_create_autotalent },
    { "native_setConcertA", "(F)V", (void *) native_set_concert_a },
    { "native_setKey", "(C)V", (void *) native_set_key },
    { "native_setFixedPitch", "(F)V", (void *) native_set_fixed_pitch },
    { "native_setFixedPull", "(F)V", (void *) native_set_fixed_pull },
    { "native_setStrength", "(F)V", (void *) native_set_strength },
    { "native_setSmoothness", "(F)V", (void *) native_set_smoothness },
    { "native_setPitchShift", "(F)V", (void *) native_set_pitch_shift },
    { "native_setScaleRotate", "(I)V", (void *) native_set_scale_rotate },
    { "native_setLfoDepth", "(F)V", (void *) native_set_lfo_depth },
    { "native_setLfoRate", "(F)V", (void *) native_set_lfo_rate },
    { "native_setLfoShape", "(F)V", (void *) native_set_lfo_shape },
    { "native_setLfoSymmetric", "(F)V", (void *) native_set_lfo_symmetric },
    { "native_setLfoQuantization", "(I)V",
        (void *) native_set_lfo_quantization },
    { "native_enableFormantCorrection", "(Z)V",
        (void *) native_enable_formant_correction },
    { "native_setFormantWarp", "(F)V", (void *) native_enable_formant_warp },
    { "native_setMix", "(F)V", (void *) native_set_mix },
    { "native_processSamples", "([SI)V", (void *) native_process_samples },
    { "native_processSamples", "([S[SI)V",
        (void *) native_process_samples_instrumental },
    { "native_destroyAutotalent", "()V", (void *) native_destroy_autotalent },
};

static int registerNativeMethods(JNIEnv *env, const char *className,
        JNINativeMethod *methods, int numMethods)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, methods, numMethods) < 0) {
        ALOGE("registerNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env)
{
    if (!registerNativeMethods(env, classPathName,
                gMethods, ARRAY_SIZE(gMethods))) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

// ----------------------------------------------------------------------------
/*
 * This is called by the VM when the shared library is first loaded.
 */

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv *env = NULL;
    ALOGI("JNI_OnLoad");
    if ((*vm)->GetEnv(vm, &uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;
    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}
