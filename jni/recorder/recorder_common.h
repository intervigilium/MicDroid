/* recorder_common.h
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

#ifndef RECORDER_COMMON_H
#define RECORDER_COMMON_H

#include <android/log.h>
#include <errno.h>

// TODO(echen): fix the formatting to conform to 80-char column
// Below code originally from c.learncodethehardway.org
#define JNI_AUDIO_LIBRARY "libjnirecorder"
#define clean_errno() (errno == 0 ? "None" : strerror(errno))

#ifdef NDEBUG
#define LOG_DEBUG(M, ...)
#else
#define LOG_DEBUG(M, ...) __android_log_print(ANDROID_LOG_DEBUG, \
                                             JNI_AUDIO_LIBRARY, \
                                             "DEBUG %s:%d: " M, __FILE__, __LINE__, ##__VA_ARGS__)
#endif
#define LOG_ERROR(M, ...) __android_log_print(ANDROID_LOG_ERROR, \
                                              JNI_AUDIO_LIBRARY, \
                                              "[ERROR] (%s:%d: errno: %s)" M, \
                                              __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)
#define LOG_WARN(M, ...) __android_log_print(ANDROID_LOG_WARN, \
                                             JNI_AUDIO_LIBRARY, \
                                             "[WARN] (%s:%d: errno: %s)" M, \
                                             __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)
#define LOG_INFO(M, ...) __android_log_print(ANDROID_LOG_INFO, \
                                             JNI_AUDIO_LIBRARY, \
                                             "[INFO] %s:%d: " M, __FILE__, __LINE__, ##__VA_ARGS__)

#define check(A, M, ...) if (!(A)) { LOG_ERROR(M, ##__VA_ARGS__); errno = 0; goto on_error; }
#define sentinel(M, ...) { LOG_ERROR(M, ##__VA_ARGS__); errno = 0; goto on_error; }
#define check_mem(A) check((A), "Out of memory.")
#define check_debug(A, M, ...) if (!(A)) { LOG_DEBUG(M, ##__VA_ARGS__); errno = 0; goto on_error; }

#endif
