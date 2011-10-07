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

#ifndef JNI_AUDIO_H
#define JNI_AUDIO_H

#include <jni.h>
#include <pthread.h>

#define THREAD_PRIORITY_URGENT_AUDIO -19

typedef struct {
  int timestamp;
  int size;
  jbyte *buf;
} jni_audio_frame;

typedef struct {
  int samples_per_sec;
  jobject r_obj;
  jclass r_class;
  pthread_mutex_t *lock;
  pthread_t *r_thread;
  int running;
  void (*r_callback)(jni_audio_frame *);
} jni_record;

typedef struct {
  int samples_per_sec;
  jobject p_obj;
  jclass p_class;
  pthread_mutex_t *lock;
  pthread_t *p_thread;
  int running;
  void (*p_callback)(jni_audio_frame *);
} jni_play;

int init_jni_record(jni_record *rec, int samples_per_sec, jobject audio_record);

int init_jni_play(jni_play *play, int samples_per_sec, jobject_audio_track);

void set_record_callback(jni_rec *rec, int (*callback)(jni_audio_frame *));

void set_play_callback(jni_play *play, int (*callback)(jni_audio_frame *));

int start_record(jni_record *rec);

int start_play(jni_play *play);

int stop_record(jni_record *rec);

int stop_play(jni_play *play);

void cleanup_jni_record(jni_record *rec);

void cleanup jni_play(jni_play *play);

#endif  // end JNI_AUDIO_H
