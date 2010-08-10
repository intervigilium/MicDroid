LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := autotalent
LOCAL_C_INCLUDES := mayer_fft.h
LOCAL_SRC_FILES := mayer_fft.c autotalent.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
