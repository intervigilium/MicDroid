/* recorder.c
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

#include "recorder.h"
#include "autotalent/autotalent.h"
#include "lame/lame.h"
#include "resample/resample.h"
#include "wave/wave.h"
#include "jni_audio.h"
#include "recorder_common.h"

static int
add_frame(jni_recorder *rec, jni_audio_frame *frame)
{
  check(rec != NULL, "Recorder: Recorder is uninitialized.");
  // TODO(echen): check if frame needs to be copied here
  pthread_mutex_lock(rec->lock);
  rec->queue_end->next = frame;
  frame->prev = rec->queue_end;
  frame->next = NULL;
  rec->queue_end = frame;
  pthread_mutex_unlock(rec->lock);
  return 0;

on_error:
  return -1;
}

static int
remove_frame(jni_recorder *rec, jni_audio_frame *frame)
{
  check(rec != NULL, "Recorder: Recorder is uninitialized.");
  // TODO(echen): check if frame needs to be copied here
  pthread_mutex_lock(rec->lock);
  frame = rec->queue;
  rec->queue = frame->next;
  frame->next = NULL;
  frame->prev = NULL;
  pthread_mutex_unlock(rec->lock);
  return 0;

on_error:
  return -1;
}

static int
record_callback(jni_audio_frame *frame)
{

  return 0;
on_error:
  return -1;
}

static int
play_callback(jni_audio_frame *frame)
{

  return 0;

on_error:
  return -1;
}

int
recorder_init(jni_recorder *rec, int sample_rate, jobject record, jobject track)
{
  int status;

  check(rec != NULL, "Recorder: No memory allocated.");
  rec->lock = (pthread_mutex_t *) malloc(sizeof(pthread_mutex_t));
  check_mem(rec->lock);
  status = pthread_mutex_init(rec->lock, NULL);
  check(status == 0, "Recorder: Unable to initialize queue lock");
  rec->queue = NULL;
  rec->queue_end = NULL;
  rec->record = (jni_record *) malloc(sizeof(jni_record));
  check_mem(rec->record);
  status = init_jni_record(rec->record, sample_rate, record);
  check(status == 0, "Recorder: Unable to initialize jni_record.");
  if (track) {
    // live playback mode requires audio track
    rec->play = (jni_play *) malloc(sizeof(jni_play));
    check_mem(rec->play);
    status = init_jni_play(rec->play, sample_rate, track);
    check(status == 0, "Recorder: Unable to initialize jni_play.");
  } else {
    rec->play = NULL;
  }
  return 0;

on_error:
  if (rec) {
    pthread_mutex_destroy(rec->lock);
    cleanup_jni_record(rec->record);
    cleanup_jni_play(rec->play);
  }
  return -1;
}

int
recorder_start(jni_recorder *rec)
{

}

int
recorder_stop(jni_recorder *rec)
{

}

int
recorder_cleanup(jni_recorder *rec)
{
  check(rec != NULL, "Recorder: No instance to cleanup.");
  // TODO(echen): stop playback, join threads, etc before cleaning memory
  while (rec->queue) {
    jni_audio_frame_node *f = rec->queue->next;
    free(rec->queue);
    rec->queue = f;
  }
  pthread_mutex_destroy(rec->lock);
  cleanup_jni_record(rec->record);
  cleanup_jni_play(rec->play);
  free(rec);
  return 0;

on_error:
  return -1;
}
