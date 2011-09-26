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
#include <android/log.h>
#include "jvm_wrapper.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "libjniaudio", \
                                      __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "libjniaudio", \
                                      __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "libjniaudio", \
                                      __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_WARN, "libjniaudio", \
                                      __VA_ARGS__)
#define CALLBACK_SUCCESS 0
#define THREAD_PRIORITY_URGENT_AUDIO -19

typedef struct {
  int sample_rate;
  jobject r_obj;
  jclass r_class;
  pthread_mutex_t *lock;
  pthread_t *r_thread;
  int running;
  void (*r_callback)(jbyte *);
} jni_record;

typedef struct {
  int sample_rate;
  jobject p_obj;
  jclass p_class;
  pthread_mutex_t *lock;
  pthread_t *p_thread;
  int running;
  void (*p_callback)(jbyte *);
} jni_play;

typedef struct {
  jni_record *record;
  jni_play *play;
} jni_audio;

jni_audio * init_jni_audio(int sample_rate, jobject audio_record, jobject audio_track);

void set_record_callback(jni_audio *audio, int (*callback)(jbyte *, int));

void set_play_callback(jni_audio *audio, int (*callback)(jbyte *, int));

int start_record(jni_audio *audio);

int start_play(jni_audio *audio);

int stop_record(jni_audio *audio);

int stop_play(jni_audio *audio);

void cleanup_jni_audio(jni_audio *audio);
