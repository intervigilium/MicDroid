/* recorder.h
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

#ifndef RECORDER_H
#define RECORDER_H

#include <jni.h>
#include <pthread.h>
#include "jni_audio.h"

typedef struct {
  jni_audio_frame *frame;
  jni_audio_frame_node *next;
  jni_audio_frame_node *prev;
} jni_audio_frame_node;

typedef struct {
  jni_record *record;
  jni_play *play;
  pthread_mutex_t *lock;
  jni_audio_frame_node *queue;
  jni_audio_frame_node *queue_end;
} jni_recorder;

int recorder_init(jni_recorder *rec,
                  int sample_rate, jobject record, jobject track);

int recorder_start(jni_recorder *rec);

int recorder_stop(jni_recorder *rec);

int recorder_cleanup(jni_recorder *rec);


#endif  // end RECORDER_H
