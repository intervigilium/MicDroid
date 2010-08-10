C_SOURCES := bluestein.c  dftw-direct.c      direct.c              kdft.c        nop.c      rank-geq2.c \
				buffered.c   dftw-directsq.c    generic.c             kdft-dif.c    plan.c     solve.c \
				conf.c       dftw-genericbuf.c  indirect.c            kdft-difsq.c  problem.c  vrank-geq1.c \
				ct.c         dftw-generic.c     indirect-transpose.c  kdft-dit.c    rader.c    zero.c

LOCAL_SRC_FILES += $(addprefix dft/, $(C_SOURCES))