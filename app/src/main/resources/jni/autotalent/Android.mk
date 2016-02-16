LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := autotalent
LOCAL_SRC_FILES := mayer_fft.c fft.c autotalent.c autotalent-interface.c
LOCAL_C_INCLUDES := mayer_fft.h fft.h autotalent.h autotalent-interface.h
LOCAL_STATIC_LIBRARIES := cpufeatures
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
$(call import-module,cpufeatures)
