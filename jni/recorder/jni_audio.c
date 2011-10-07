/* jni_audio.c
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

#include <time.h>
#include "jni_common.h"
#include "jni_audio.h"
#include "jvm_wrapper.h"

static int get_timestamp_ms()
{
  int ts;
  struct timespec now;
  // clock_gettime has ns resolution
  clock_gettime(CLOCK_MONOTONIC, &now);
  ts = now.tv_sec;
  ts *= 1000;
  ts += now.tv_nsec / 1000000;
  return res;
}

static void pthread_sleep(int sleep_time_ms)
{
  pthread_mutex_t wait_mutex = PTHREAD_MUTEX_INITIALIZER;
  pthread_cond_t wait_cond = PTHREAD_COND_INITIALIZER;
  struct timespec wait_time;
  int rt;

  clock_gettime(CLOCK_MONOTONIC, &wait_time);
  wait_time.tv_sec += sleep_time_ms / 1000;
  wait_time.tv_nsec += (sleep_time_ms % 1000) * 1000000;

  pthread_mutex_lock(&wait_mutex);
  rt = pthread_cond_timedwait(&wait_cond, &wait_mutex, &wait_time);
  pthread_mutex_unlock(&wait_mutex);
  LOG_INFO("Thread slept for %d milliseconds", sleep_time_ms);
}

static void set_thread_priority(int priority)
{
  JNIEnv *jni_env = NULL;
  jclass process_class;
  jmethodID set_priority_method;

  ATTACH_JVM(jni_env);

  process_class = (*jni_env)->NewGlobalRef(
      (*jni_env)->FindClass("android/os/Process"));
  check(process_class != NULL, "Unable to find android/os/Process class");
  set_priority_method = (*jni_env)->GetStaticMethodID(
      process_class, "SetThreadPriority", "(I)V");
  check(set_priority_method != NULL, "Unable to find set priority method");

  (*jni_env)->CallStaticVoidMethod(process_class,
                                   set_priority_method,
                                   priority);
on_error:
  DETACH_JVM(jni_env);
}

static int is_running(jni_play *play)
{
  int ret = 0;
  pthread_mutex_lock(play->lock);
  ret = play->running;
  pthread_mutex_unlock(play->lock);
  return ret;
}

static int is_running(jni_record *record)
{
  int ret = 0;
  pthread_mutex_lock(record->lock);
  ret = record->running;
  pthread_mutex_unlock(record->lock);
  return ret;
}

static void record_function(void *ptr)
{
  jni_record *record = (jni_record *) ptr;
  JNIEnv *jni_env = NULL;
  jbyteArray j_in_buf;
  jbyte *in_buf;
  jmethodID read_method, record_method;
  int status;
  int bytes_read;
  long now, last_frame;
  int elapsed_ms, to_wait_ms;
  jni_audio_frame *frame;
  // samples_per_frame = samples_per_sec * 20 / 1000
  // bytes_per_sample = 16 bits = 2 bytes
  int size = record->samples_per_sec * 20 / 1000 * 2;
  int nframes = record->samples_per_sec * 20 / 1000 / 1;
  // for frame time calculation
  int frame_time = nframes * 1000 / record->samples_per_sec;
  int missed_time = frame_time

  ATTACH_JVM(jni_env);

  read_method = (*jni_env)->GetMethodID(record->r_class, "read", "([BII)I");
  record_method = (*jni_env)->GetMethodID(record->r_class,
                                          "startRecording", "()V");
  check(read_method != NULL && record_method != NULL,
        "Record thread: Unable to find AudioRecord functions");
  j_in_buf = (*jni_env)->NewByteArray(size);
  check_mem(j_in_buf);
  in_buf = (*jni_env)->GetByteArrayElements(j_in_buf, 0);

  set_thread_priority(THREAD_PRIORITY_URGENT_AUDIO);

  (*jni_env)->CallVoidMethod(record->r_obj, record_method);
  last_frame = get_timestamp_ms();

  while (is_running(record)) {
    now = get_timestamp_ms();
    elapsed_ms = now - last_frame;
    last_frame = get_timestamp_ms();
    // adjust time if we are filling faster than time
    missed_time = missed_time / 2 + elapsed_ms - frame_time;
    if (missed_time <= 0) {
      to_wait_ms = (-1 * missed_time) - 2;
      if (to_wait_ms > 0) {
        pthread_sleep(to_wait_ms);
      }
    }
    bytes_read = (*jni_env)->CallIntMethod(record->r_obj,
                                           read_method,
                                           j_in_buf,
                                           0, size);
    if (bytes_read <= 0) {
      LOG_WARN("Record thread: error reading data...");
      continue;
    }
    if (bytes_read != size) {
      LOG_WARN("Record thread: Overrun...");
      continue;
    }

    // in_buf is aliased to j_in_buf, callback is responsible for copying buf
    frame = (jni_audio_frame *) malloc(sizeof(jni_audio_frame));
    frame->timestamp = now;
    frame->size = size;
    frame->buf = in_buf;
    status = (*record->r_callback)(frame);
    check(status == 0, "Record thread: Error in record callback");
  }

on_error:
  (*jni_env)->ReleaseByteArrayElements(j_in_buf, in_buf, 0);
  (*jni_env)->DeleteLocalRef(j_in_buf);
  DETACH_JVM(jni_env);
  return 0;
}

static void play_function(void *ptr)
{
  jni_play *play = (jni_play *) ptr;
  JNIEnv *jni_env = NULL;
  int status;
  jmethodID write_method, play_method;
  jbyteArray j_out_buf;
  jbyte *out_buf;
  jni_audio_frame *frame;
  // samples_per_frame = samples_per_sec * 20 / 1000
  // bytes_per_sample = 16 bits = 2 bytes
  int size = play->samples_per_sec * 20 / 1000 * 2;
  int status;

  ATTACH_JVM(jni_env);

  write_method = (*jni_env)->GetMethodID(play->p_class, "write", "([BII)I");
  play_method = (*jni_env)->GetMethodID(play->p_class, "play", "()V");
  check(write_method != NULL && play_method != NULL,
        "Playback thread: Unable to find AudioTrack functions");

  j_out_buf = (*jni_env)->NewByteArray(size);
  check_mem(j_out_buf);
  out_buf = (*jni_env)->GetByteArrayElements(j_out_buf, 0);

  set_thread_priority(THREAD_PRIORITY_URGENT_AUDIO);

  (*jni_env)->CallVoidMethod(play->p_obj, play_method);

  while (is_running(play)) {
    // fill buffer from callback, callback responsible for copying buf
    frame = (jni_audio_frame *) malloc(sizeof(jni_audio_frame));
    status = (*play->p_callback)(frame);
    check(status == 0, "Playback thread: Error retrieving frame from callback.");
    status = (*jni_env)->CallIntMethod(play->p_obj,
                                       write_method,
                                       j_out_buf,
                                       0, size);
    if (status < 0) {
      LOG_WARN("Playback thread: Error writing output buffer: %d", status);
      continue;
    } else if (status != size) {
      LOG_INFO("Playback thread: Only wrote %d of %d bytes!", status, size);
    }
  }

on_error:
  (*jni_env)->ReleaseByteArrayElements(j_out_buf, out_buf, 0);
  (*jni_env)->DeleteLocalRef(j_out_buf);
  DETACH_JVM(jni_env);
  return 0;
}

int init_jni_record(jni_record *rec, int samples_per_sec, jobject audio_record)
{
  int status = -1;
  JNIEnv *jni_env = NULL;

  ATTACH_JVM(jni_env);

  if (rec) {
    // init record object
    rec->r_thread = (pthread_t *) malloc(sizeof(pthread_t));
    rec->lock = (pthread_mutex_t *) malloc(sizeof(pthread_mutex_t));
    pthread_mutex_init(rec->lock, NULL);
    rec->running = 0;
    rec->r_obj = (*jni_env)->NewGlobalRef(audio_record);
    rec->r_class = (jclass) (*jni_env)->NewGlobalRef(
        jni_env->FindClass("android/media/AudioRecord"));
    rec->r_callback = NULL;
    status = 0;
  }

  DETACH_JVM(jni_env);
  return status;
}

int init_jni_play(jni_play *play, int samples_per_sec, jobject audio_track)
{
  int status = -1;
  JNIEnv *jni_env = NULL;

  ATTACH_JVM(jni_env);

  if (play) {
    // init play object
    play->p_thread = (pthread_t *) malloc(sizeof(pthread_t));
    play->lock = (pthread_mutex_t *) malloc(sizeof(pthread_mutex_t));
    pthread_mutex_init(play->lock, NULL);
    play->running = 0;
    play->p_obj = (*jni_env)->NewGlobalRef(audio_track);
    play->p_class = (jclass) (*jni_env)->NewGlobalRef(
        jni_env->FindClass("android/media/AudioTrack"));
    play->p_callback = NULL;
    status = 0;
  }

  DETACH_JVM(jni_env);
  return status;

}

void set_record_callback(jni_record *rec,
                         int (*callback)(jni_audio_frame *frame))
{
  if (record) {
    rec->r_callback = callback;
  }
}

void set_play_callback(jni_play *play, int (*callback)(jni_audio_frame *frame))
{
  if (play) {
    play->p_callback = callback;
  }
}

int start_record(jni_record *rec)
{
  int res;

  rec->running = 1;
  res = pthread_create(rec->r_thread,
                       NULL,
                       record_function,
                       (void *) rec);
  if (res) {
    LOG_ERROR("Error occurred starting record thread: %d", res);
    rec->running = 0;
  }
  return res;
}

int start_play(jni_play *play)
{
  int res;

  play->running = 1;
  res = pthread_create(play->p_thread,
                       NULL,
                       play_function,
                       (void *) play);
  if (res) {
    LOG_ERROR("Error occurred starting playback thread: %d", res);
    play->running = 0;
  }
  return res;
}

int stop_record(jni_record *rec)
{
  int res;

  pthread_mutex_lock(rec->lock);
  rec->running = 0;
  pthread_mutex_unlock(rec->lock);
  res = pthread_join(*(rec->r_thread), NULL);
  if (res) {
    LOG_ERROR("Error occurred joining record thread: %d", res);
  }
  return res;
}

int stop_play(jni_play *play)
{
  int res;

  pthread_mutex_lock(play->lock);
  play->running = 0;
  pthread_mutex_unlock(play->lock);
  res = pthread_join(*(play->p_thread), NULL);
  if (res) {
    LOG_ERROR("Error occurred joining playback thread: %d", res);
  }
  return res;
}

void cleanup_jni_record(jni_record *rec)
{
  JNIEnv *jni_env = NULL;
  jmethodID release_method = NULL;

  ATTACH_JVM(jni_env);

  if (rec) {
    if (is_running(rec)) {
      stop_record(rec);
    }
    free(rec->r_thread);
    pthread_mutex_destroy(record->lock);
    release_method = (*jni_env)->GetMethodID(rec->r_class, "release", "()V");
    (*jni_env)->CallVoidMethod(rec->r_obj, release_method);
    (*jni_env)->DeleteGlobalRef(rec->r_obj);
    (*jni_env)->DeleteGlobalRef(rec->r_class);

    rec->r_obj = NULL;
    rec->r_class = NULL;

    free(record);
  }

  DETACH_JVM(jni_env);
}

void cleanup_jni_play(jni_play *play)
{
  JNIEnv *jni_env = NULL;
  jmethodID release_method = NULL;

  ATTACH_JVM(jni_env);

  if (play) {
    if (is_running(play)) {
      stop_play(play);
    }
    free(play->p_thread);
    pthread_mutex_destroy(play->lock);
    release_method = (*jni_env)->GetMethodID(play->p_class,
                                             "release",
                                             "()V");
    (*jni_env)->CallVoidMethod(play->p_obj, release_method);
    (*jni_env)->DeleteGlobalRef(play->p_obj);
    (*jni_env)->DeleteGlobalRef(play->p_class);

    play->p_obj = NULL;
    play->p_class = NULL;

    free(play);
  }

  DETACH_JVM(jni_env);
}
