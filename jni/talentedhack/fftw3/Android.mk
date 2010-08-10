LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

include $(LOCAL_PATH)/api/sources.mk
include $(LOCAL_PATH)/dft/sources.mk
include $(LOCAL_PATH)/dft/scalar/sources.mk
include $(LOCAL_PATH)/dft/scalar/codelets/sources.mk
include $(LOCAL_PATH)/kernel/sources.mk
include $(LOCAL_PATH)/rdft/sources.mk
include $(LOCAL_PATH)/rdft/scalar/sources.mk
include $(LOCAL_PATH)/rdft/scalar/r2cb/sources.mk
include $(LOCAL_PATH)/rdft/scalar/r2cf/sources.mk
include $(LOCAL_PATH)/rdft/scalar/r2r/sources.mk
include $(LOCAL_PATH)/reodft/sources.mk

LOCAL_MODULE := fftw3
LOCAL_C_INCLUDES := . \
					$(LOCAL_PATH)/api \
					$(LOCAL_PATH)/dft \
					$(LOCAL_PATH)/dft/scalar \
					$(LOCAL_PATH)/dft/scalar/codelets \
					$(LOCAL_PATH)/kernel \
					$(LOCAL_PATH)/rdft \
					$(LOCAL_PATH)/rdft/scalar \
					$(LOCAL_PATH)/rdft/scalar/r2cb \
					$(LOCAL_PATH)/rdft/scalar/r2cf \
					$(LOCAL_PATH)/rdft/scalar/r2r \
					$(LOCAL_PATH)/reodft

include $(BUILD_STATIC_LIBRARY)