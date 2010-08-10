C_SOURCES := align.c     cpy1d.c       extract-reim.c  md5.c      planner.c  scan.c     tensor2.c  tensor8.c  transpose.c \
				alloc.c     cpy2d.c       hash.c          minmax.c   primes.c   solver.c   tensor3.c  tensor9.c  trig.c \
				assert.c    cpy2d-pair.c  iabs.c          ops.c      print.c    solvtab.c  tensor4.c  tensor.c   twiddle.c \
				awake.c     ct.c          kalloc.c        pickdim.c  problem.c  stride.c   tensor5.c  tile2d.c \
				buffered.c  debug.c       md5-1.c         plan.c     rader.c    tensor1.c  tensor7.c  timer.c

LOCAL_SRC_FILES += $(addprefix fftw3/kernel/, $(C_SOURCES))