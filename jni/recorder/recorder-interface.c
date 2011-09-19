/* recorder-interface.c
 * JNIRecorder library for Android
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

#include "recorder-interface.h"


JNIEXPORT void JNICALL Java_com_intervigil_micdroid_recorder_JniRecorder_jniRecorderStart
  (JNIEnv *env, jobject obj)
{


}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_recorder_JniRecorder_jniRecorderStop
  (JNIEnv *env, jobject obj)
{

}

JNIEXPORT jboolean JNICALL Java_com_intervigil_micdroid_recorder_JNIRecorder_jniIsRunning
  (JNIEnv *env, jobject obj)
{

}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_recorder_JniRecorder_jniRecorderCleanup
  (JNIEnv *env, jobject obj)
{

}

JNIEXPORT void JNICALL Java_com_intervigil_micdroid_recorder_JniRecorder_jniRecorderInitCallback
  (JNIEnv *env, jobject obj, jint sampleRate, jobject record, jobject track)
{

}
