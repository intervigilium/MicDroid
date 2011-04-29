LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := micdroid
LOCAL_SRC_FILES := micdroid-interface.c
LOCAL_C_INCLUDES := micdroid-interface.h
LOCAL_STATIC_LIBRARIES := cpufeatures
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
$(call import-module,cpufeatures)
