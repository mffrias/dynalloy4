dynalloy4
=========

## MiniSat 2.2 on Apple Silicon

`-Ddynalloy.solver=minisat` makes DynAlloy solve with MiniSat 2.2 (Kodkod's
`MiniSat220` class, through JNI), from the native libraries in `lib/alloy4.jar`
(Alloy 4.2). The macOS one, `x86-mac/libminisat220.jnilib`, used to be x86_64
only, so it needed an x86_64 JVM, under Rosetta on Apple Silicon. It is now a
universal library with two slices:

- x86_64: the library alloy4.jar has shipped since 2012, byte for byte.
- arm64: built by `scripts/build-minisat220.sh` from MiniSat 2.2.0
  (github.com/niklasso/minisat, tag `releases/2.2.0`, commit `eb01ad68`) and
  Kodkod's JNI glue for it, as published with Alloy 5.1.0
  (github.com/AlloyTools/org.alloytools.alloy, commit `05328e25`,
  `org.alloytools.kodkod.nativesat/lib/minisat-2.2.0`). The script has the
  URLs, checksums and compiler flags; the only source change is a one-line move
  of a default argument that current compilers reject.

Nothing else in alloy4.jar changed. The Linux libraries are still x86 and
amd64 only, and there is no MiniSat 2.2 for Windows.

MiniSat and Kodkod are under the MIT license: `LICENSES/MiniSat-2.2.txt`
(MiniSat 2.2, as in its sources), `LICENSES/MiniSat.txt` and
`LICENSES/Kodkod.txt`.

To rebuild it, on macOS with the Xcode command line tools and `JAVA_HOME` set
to a JDK 8 (for the JNI headers):

    scripts/build-minisat220.sh                  # updates lib/alloy4.jar
    scripts/build-minisat220.sh path/to/alloy4.jar

With the same toolchain (Apple clang 17.0.0) it gives the committed bytes; it
says so, or prints the new checksum. Then rebuild dynalloy4.jar (`ant jar`),
which bundles everything in `lib/`, and what is built on it (jDynAlloy, TACO).
