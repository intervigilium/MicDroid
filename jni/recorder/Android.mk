LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := recorder
LOCAL_SRC_FILES := recorder-interface.c
LOCAL_C_INCLUDES := recorder-interface.h
LOCAL_STATIC_LIBRARIES := autotalent
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
$(call import-module,autotalent)
