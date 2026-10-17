# Native SAT solvers

`lib/alloy4.jar` contains Kodkod's JNI bindings and native builds of several SAT
solvers, in one folder per platform. DynAlloy uses only one of them, MiniSat
2.2 (Kodkod's `kodkod.engine.satlab.MiniSat220`), and only when it is available
(see [SOLVERS.md](SOLVERS.md)); otherwise it solves with SAT4J, which is plain
Java. This page covers which libraries the jar has, how DynAlloy extracts and
loads them, and how the ARM builds of MiniSat 2.2 were made.

## What the jar contains

    unzip -l lib/alloy4.jar | grep minisat220

| Folder (platform) | MiniSat 2.2 (`MiniSat220`) | Other natives |
|---|---|---|
| `x86-mac` (every Mac) | `libminisat220.jnilib`, universal: x86_64 + arm64 | MiniSat (old), MiniSat prover, ZChaff, Lingeling, MiniSat 2.2 simp, `berkmin` |
| `amd64-linux` | `libminisat220.so` (x86_64) | MiniSat (old), MiniSat prover, ZChaff, Lingeling, MiniSat 2.2 simp, `berkmin` |
| `x86-linux` | `libminisat220.so` (i386) | the same, plus `x1` variants |
| `aarch64-linux` | `libminisat220.so` (arm64), added on the stryker branch | none |
| `x86-windows` | none | MiniSat (old), MiniSat prover, ZChaff, `berkmin.exe` |
| `x86-freebsd` | none | MiniSat (old), MiniSat prover, ZChaff, `berkmin` |

So MiniSat 2.2 is available on macOS (Intel and Apple Silicon) and on x86,
x86_64 and arm64 Linux. Windows, FreeBSD and other architectures solve with
SAT4J.

`alloy4.jar` is not a stock Alloy release: its `Version` class says 4.1.10
(2009), but it also has classes that came later, such as `MiniSat220` and
`Lingeling`, and fields that only RFM's build has
(`TranslateAlloyToKodkod.boundsHackEnabled` and
`removeAlloySyntaxChecksHackEnabled`, which `AlloyAnalyzer` sets). Which Alloy
sources it was built from is not recorded here (see
[OPEN-QUESTIONS.md](OPEN-QUESTIONS.md)).

## How DynAlloy finds the library

All of this is in `src/ar/uba/dc/rfm/dynalloy/analyzer/AlloyJNILibraryPath.java`,
called by `AlloyAnalyzer` before each analysis.

1. **Platform folder** (`platformFolder()`): `<os.arch>-<os>`, with `i386`…`i686`
   mapped to `x86`, and `x86-mac` on any Mac (the Mac libraries are universal).
   So x86_64 Linux is `amd64-linux`, arm64 Linux is `aarch64-linux`, and a
   64-bit JVM on Windows is `amd64-windows`, which the jar does not have.
   MiniSat 2.2's file name (`miniSat220FileName()`) is `libminisat220.jnilib` on
   macOS, `minisat220.dll` on Windows and `libminisat220.so` elsewhere.
2. **Extraction** (`copyFromJAR()`): the platform's libraries and solver
   executables are copied out of the jar into
   `<java.io.tmpdir>/alloy4tmp40-<user.name>/binary` (`alloy4tmp40-<user.name>`
   is made `chmod 700`), using
   Alloy's `Util.copy`, which never overwrites an existing file. Before that,
   once per JVM, `replaceOutdated` replaces every file there whose bytes differ
   from the jar's, by writing a new copy and renaming it over the old one, so
   another JVM that has the old library loaded keeps working. Without it a
   library extracted by an older `alloy4.jar` would be used forever, for example
   the x86_64-only MiniSat 2.2 that an arm64 JVM cannot load.
3. **Loading** (`setupJNILibraryPath()`): Kodkod loads MiniSat 2.2 with
   `System.loadLibrary("minisat220")`, which looks in the JVM's library path.
   DynAlloy sets `java.library.path` to the extraction folder and, through
   reflection, replaces `ClassLoader.usr_paths` (the JVM's cached copy) with it.
   That works on JDK 8. Newer JVMs (17 and 21, for example) hide `usr_paths`
   from reflection; then DynAlloy loads the library itself with
   `System.load(<binary>/libminisat220.jnilib|.so)`, once per JVM. Kodkod's own
   `loadLibrary` then fails quietly, and its native methods bind to the library
   already loaded by the same class loader.

`usr_paths` is replaced, not extended, so on JDK 8, after the first analysis,
`System.loadLibrary` looks only in the extraction folder; other JNI libraries
that the program expects on `java.library.path` are no longer found (read from
the code, not tested).

The first analysis also decides whether MiniSat 2.2 can be used, by creating and
freeing one solver (`AlloyAnalyzer.miniSat220Problem()`).

## MiniSat 2.2 for ARM

`lib/alloy4.jar` used to have MiniSat 2.2 only for x86 and x86_64.
`scripts/build-minisat220.sh` adds it for ARM:

- `x86-mac/libminisat220.jnilib` is now a universal library. Its x86_64 slice
  is the library `alloy4.jar` has shipped since 2012, byte for byte (sha256
  `1e415d68…`); the arm64 slice (sha256 `455d4838…`) is built with Apple clang.
- `aarch64-linux/libminisat220.so` is new, for arm64 Linux (sha256
  `691fc8cf…`). It is built in a linux/arm64 Docker container (native on Apple
  Silicon): Debian 10 (buster) pinned by image digest, with g++ 8.3.0 from a
  fixed date of snapshot.debian.org (2024-06-01). It needs only glibc (symbol
  versions up to 2.17, the first for aarch64) and links the C++ runtime
  statically, so it loads on any arm64 Linux with glibc.

Both are compiled from MiniSat 2.2.0 (github.com/niklasso/minisat, tag
`releases/2.2.0`, commit `eb01ad68`) and Kodkod's JNI glue for it, as published
with Alloy 5.1.0 (github.com/AlloyTools/org.alloytools.alloy, commit
`05328e25`, `org.alloytools.kodkod.nativesat/lib/minisat-2.2.0`), with
`-std=c++98 -O3 -ffp-contract=off`. Alloy 5 names the Java class `MiniSat`;
this jar's is `MiniSat220`, so the JNI symbols are renamed. No fused
multiply-add: the floating point arithmetic (variable and clause activities)
rounds as in the x86_64 build, so the solvers make the same decisions. The
script has the URLs, checksums, images and compiler flags; the only source
change is a one-line move of a default argument that current compilers reject.
Nothing else in `alloy4.jar` changed. There is no MiniSat 2.2 for Windows.

To look at them:

    unzip -o -q lib/alloy4.jar x86-mac/libminisat220.jnilib aarch64-linux/libminisat220.so -d /tmp/alloy4-natives
    lipo -info /tmp/alloy4-natives/x86-mac/libminisat220.jnilib
    file /tmp/alloy4-natives/aarch64-linux/libminisat220.so
    shasum lib/alloy4.jar

`lipo` lists `x86_64 arm64`, `file` says `ELF 64-bit LSB shared object, ARM
aarch64`, and the committed jar's sha1 is
`0f348b4ac430bc79bf8d7f9a2fee684f113ebf45`.

MiniSat and Kodkod are under the MIT license: `LICENSES/MiniSat-2.2.txt`
(MiniSat 2.2, as in its sources), `LICENSES/MiniSat.txt` and
`LICENSES/Kodkod.txt`.

### Rebuilding them

On macOS with the Xcode command line tools (clang++, lipo, otool), Docker, curl,
perl, patch, unzip and zip, and `JAVA_HOME` set to a JDK 8 (for the JNI headers
of the macOS library):

    scripts/build-minisat220.sh                  # both, into lib/alloy4.jar
    scripts/build-minisat220.sh --mac            # only the macOS library
    scripts/build-minisat220.sh --linux-aarch64  # only the Linux one (Docker only)
    scripts/build-minisat220.sh path/to/alloy4.jar

The jar is changed in place, only in those entries, with fixed timestamps. The
Linux library comes out byte for byte the same; the macOS one does with the
same toolchain (checked with Apple clang 17.0.0, clang-1700.0.13.5, ld-1167.5,
macOS SDK 15.5). The script says whether each library is identical to the
committed one, or prints the new checksum. Starting from the original
`alloy4.jar` (master's) or from the committed one, it gives the committed jar;
`--mac` alone, then `--linux-aarch64`, gives it too. Then rebuild
`dist/dynalloy4.jar` (`ant jar`), which bundles everything in `lib/`, and what
is built on it (jDynAlloy, TACO).
