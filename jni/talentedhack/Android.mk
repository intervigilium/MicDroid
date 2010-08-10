JNIPATH := $(call my-dir)
LOCAL_PATH := $(JNIPATH)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(JNIPATH)
include $(CLEAR_VARS)

LOCAL_MODULE := talentedhack
LOCAL_CFLAGS := -std=c99
LOCAL_SRC_FILES := circular_buffer.c fft.c formant_corrector.c lfo.c pitch_detector.c pitch_shifter.c pitch_smoother.c quantizer.c talentedhack.c
LOCAL_C_INCLUDES := . \
					$(LOCAL_PATH)/fftw3
LOCAL_STATIC_LIBRARIES := fftw3
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)