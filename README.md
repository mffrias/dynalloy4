dynalloy4
=========

## MiniSat 2.2 for ARM

DynAlloy solves with MiniSat 2.2 (Kodkod's `MiniSat220` class, through JNI)
when `lib/alloy4.jar` (Alloy 4.2) has its native library for the platform and
it loads, and with SAT4J otherwise; `-Ddynalloy.solver=minisat` or `=sat4j`
forces one of them. alloy4.jar used to have MiniSat 2.2 only for x86 and
x86_64. `scripts/build-minisat220.sh` adds it for ARM:

- `x86-mac/libminisat220.jnilib` is now a universal library. Its x86_64 slice
  is the library alloy4.jar has shipped since 2012, byte for byte; the arm64
  slice is built with Apple clang.
- `aarch64-linux/libminisat220.so` is new, for arm64 Linux. It is built in a
  linux/arm64 Docker container (native on Apple Silicon): Debian 10 pinned by
  image digest, with g++ 8.3.0 from a fixed date of snapshot.debian.org. It
  needs only glibc (symbol versions up to 2.17, the first for aarch64) and
  links the C++ runtime statically, so it loads on any arm64 Linux with glibc.

Both are compiled from MiniSat 2.2.0 (github.com/niklasso/minisat, tag
`releases/2.2.0`, commit `eb01ad68`) and Kodkod's JNI glue for it, as published
with Alloy 5.1.0 (github.com/AlloyTools/org.alloytools.alloy, commit `05328e25`,
`org.alloytools.kodkod.nativesat/lib/minisat-2.2.0`), with `-O3
-ffp-contract=off` (no fused multiply-add, so the floating point arithmetic
rounds as in the x86_64 build). The script has the URLs, checksums, images and
compiler flags; the only source change is a one-line move of a default argument
that current compilers reject. Nothing else in alloy4.jar changed. There is no
MiniSat 2.2 for Windows.

MiniSat and Kodkod are under the MIT license: `LICENSES/MiniSat-2.2.txt`
(MiniSat 2.2, as in its sources), `LICENSES/MiniSat.txt` and
`LICENSES/Kodkod.txt`.

To rebuild them, on macOS with the Xcode command line tools, Docker and
`JAVA_HOME` set to a JDK 8 (for the JNI headers of the macOS library):

    scripts/build-minisat220.sh                  # both, into lib/alloy4.jar
    scripts/build-minisat220.sh --mac            # only the macOS library
    scripts/build-minisat220.sh --linux-aarch64  # only the Linux one (Docker only)
    scripts/build-minisat220.sh path/to/alloy4.jar

The Linux library comes out byte for byte the same; the macOS one does with
the same toolchain (Apple clang 17.0.0). The script says so, or prints the new
checksum. Starting from the original alloy4.jar or from the committed one, it
gives the committed jar. Then rebuild dynalloy4.jar (`ant jar`), which bundles
everything in `lib/`, and what is built on it (jDynAlloy, TACO).
