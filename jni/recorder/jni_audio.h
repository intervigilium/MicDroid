/* jni_audio.h
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

#include <jni.h>
#include <pthread.h>
#include "jvm_wrapper.h"

typedef struct {
  int sample_rate;
  jobject r_obj;
  jclass r_class;
  pthread_mutex_t *lock;
  pthread_t *r_thread;
  int running;
} jni_record;

typedef struct {
  int sample_rate;
  jobject p_obj;
  jclass p_class;
  pthread_mutex_t *lock;
  pthread_t *p_thread;
  int running;
} jni_play;

typedef struct {
  jni_record *record;
  jni_play *play;
} jni_audio;

jni_audio * init_jni_audio(int sample_rate, jobject audio_record, jobject audio_track);

void start_record(jni_audio *audio);

void start_play(jni_audio *audio);

void stop_record(jni_audio *audio);

void stop_play(jni_audio *audio);

void cleanup_jni_audio(jni_audio *audio);
