LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)

include $(CLEAR_VARS)

LOCAL_MODULE := talentedhack
LOCAL_SRC_FILES := circular_buffer.c fft.c formant_corrector.c lfo.c pitch_detector.c pitch_shifter.c pitch_smoother.c quantizer.c autotalent.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_STATIC_LIBRARIES := fftw3

include $(BUILD_SHARED_LIBRARY)
