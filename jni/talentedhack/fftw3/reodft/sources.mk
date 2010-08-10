C_SOURCES := conf.c redft00e-r2hc-pad.c reodft010e-r2hc.c reodft11e-r2hc-odd.c rodft00e-r2hc.c \
			 redft00e-r2hc.c reodft00e-splitradix.c reodft11e-r2hc.c reodft11e-radix2.c rodft00e-r2hc-pad.c

LOCAL_SRC_FILES += $(addprefix fftw3/reodft/, $(C_SOURCES))