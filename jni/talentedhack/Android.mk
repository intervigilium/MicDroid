LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := talentedhack
LOCAL_SRC_FILES := circular_buffer.c fft.c formant_corrector.c lfo.c pitch_detector.c pitch_shifter.c pitch_smoother.c quantizer.c autotalent.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
