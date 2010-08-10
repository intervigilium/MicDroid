C_SOURCES := hc2c.c  hfb.c  r2c.c  r2r.c

LOCAL_SRC_FILES += $(addprefix fftw3/rdft/scalar/, $(C_SOURCES))