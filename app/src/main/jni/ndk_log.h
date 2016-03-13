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

#ifndef _NDK_LOG_H_
#define _NDK_LOG_H_

#ifndef LOG_TAG
#define LOG_TAG "Autotalent"
#endif

#define ALOGV(...) (__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__));
#define ALOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__));
#define ALOGI(...) (__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__));
#define ALOGW(...) (__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__));
#define ALOGE(...) (__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__));

#endif
