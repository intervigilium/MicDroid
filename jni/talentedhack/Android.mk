#build fftw3 static library
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

include $(LOCAL_PATH)/fftw3/api/sources.mk
include $(LOCAL_PATH)/fftw3/dft/sources.mk
include $(LOCAL_PATH)/fftw3/dft/scalar/sources.mk
include $(LOCAL_PATH)/fftw3/dft/scalar/codelets/sources.mk
include $(LOCAL_PATH)/fftw3/kernel/sources.mk
include $(LOCAL_PATH)/fftw3/rdft/sources.mk
include $(LOCAL_PATH)/fftw3/rdft/scalar/sources.mk
include $(LOCAL_PATH)/fftw3/rdft/scalar/r2cb/sources.mk
include $(LOCAL_PATH)/fftw3/rdft/scalar/r2cf/sources.mk
include $(LOCAL_PATH)/fftw3/rdft/scalar/r2r/sources.mk
include $(LOCAL_PATH)/fftw3/reodft/sources.mk

LOCAL_MODULE := fftw3
LOCAL_C_INCLUDES := $(LOCAL_PATH)/fftw3 \
					$(LOCAL_PATH)/fftw3/api \
					$(LOCAL_PATH)/fftw3/dft \
					$(LOCAL_PATH)/fftw3/dft/scalar \
					$(LOCAL_PATH)/fftw3/dft/scalar/codelets \
					$(LOCAL_PATH)/fftw3/kernel \
					$(LOCAL_PATH)/fftw3/rdft \
					$(LOCAL_PATH)/fftw3/rdft/scalar \
					$(LOCAL_PATH)/fftw3/rdft/scalar/r2cb \
					$(LOCAL_PATH)/fftw3/rdft/scalar/r2cf \
					$(LOCAL_PATH)/fftw3/rdft/scalar/r2r \
					$(LOCAL_PATH)/fftw3/reodft

include $(BUILD_STATIC_LIBRARY)

#build talentedhack shared library
include $(CLEAR_VARS)

LOCAL_MODULE := talentedhack
LOCAL_CFLAGS := -std=c99
LOCAL_SRC_FILES := circular_buffer.c fft.c formant_corrector.c lfo.c pitch_detector.c pitch_shifter.c pitch_smoother.c quantizer.c talentedhack.c
LOCAL_C_INCLUDES := . \
					$(LOCAL_PATH)/fftw3/api
LOCAL_STATIC_LIBRARIES := fftw3
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)