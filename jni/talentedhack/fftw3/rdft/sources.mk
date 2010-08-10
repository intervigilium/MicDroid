C_SOURCES := buffered2.c       direct2.c        khc2c.c   problem2.c               rdft2-strides.c \
				buffered.c        direct-r2c.c     khc2hc.c  problem.c                rdft2-tensor-max-index.c \
				conf.c            direct-r2r.c     kr2c.c    rank0.c                  rdft-dht.c \
				ct-hc2c.c         generic.c        kr2r.c    rank0-rdft2.c            solve2.c \
				ct-hc2c-direct.c  hc2hc.c          nop2.c    rank-geq2.c              solve.c \
				dft-r2hc.c        hc2hc-direct.c   nop.c     rank-geq2-rdft2.c        vrank3-transpose.c \
				dht-r2hc.c        hc2hc-generic.c  plan2.c   rdft2-inplace-strides.c  vrank-geq1.c \
				dht-rader.c       indirect.c       plan.c    rdft2-rdft.c             vrank-geq1-rdft2.c

LOCAL_SRC_FILES += $(addprefix rdft/, $(C_SOURCES))