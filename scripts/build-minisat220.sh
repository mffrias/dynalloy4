#!/usr/bin/env bash
# Builds the MiniSat 2.2 libraries for ARM that alloy4.jar did not have, from the
# sources below, and puts them into alloy4.jar:
#
#   --mac            x86-mac/libminisat220.jnilib becomes a universal (x86_64 +
#                    arm64) library, so that MiniSat 2.2 also loads in an arm64 JVM
#                    on Apple Silicon. Its x86_64 slice is the one alloy4.jar
#                    already has, byte for byte; only the arm64 slice is compiled.
#                    Needs macOS with the Xcode command line tools (clang++, lipo,
#                    otool) and JAVA_HOME pointing at a JDK 8 (for its JNI headers).
#   --linux-aarch64  adds aarch64-linux/libminisat220.so, for arm64 Linux. It is
#                    compiled in a linux/arm64 Docker container (native on Apple
#                    Silicon), with a pinned Debian image and g++ from a fixed
#                    snapshot of the Debian archive, so the same bytes come out.
#                    Needs Docker.
#
# Without options, both. See README.md, "MiniSat 2.2 for ARM".
#
# Usage: scripts/build-minisat220.sh [--mac] [--linux-aarch64] [ALLOY4_JAR]
#        (default jar: this repository's lib/alloy4.jar)
#
# The jar is updated in place, and only those entries change; running the
# script again on the result gives the same jar. Also needs curl, perl, patch,
# unzip and zip.
set -euo pipefail

# MiniSat 2.2.0 (MIT): github.com/niklasso/minisat, tag releases/2.2.0.
MINISAT_COMMIT=eb01ad68b75bb3b34ff8657c37ad6a31faae0fc3
MINISAT_URL="https://codeload.github.com/niklasso/minisat/tar.gz/$MINISAT_COMMIT"
MINISAT_SHA256=e146050119f2062d438f7b5450db8d16cb5645394de56efbfcfa43f557559eed

# Kodkod's JNI glue for MiniSat 2.2 (MIT), as published with Alloy 5.1.0
# (github.com/AlloyTools/org.alloytools.alloy, tag v5.1.0), and its ParseUtils.h,
# which compiles without zlib when Kodkod_JNI is defined. Alloy 5 names the
# class kodkod.engine.satlab.MiniSat; alloy4.jar's is MiniSat220, so the JNI
# symbols are renamed.
ALLOY_COMMIT=05328e2549eb6c1aa47da5deec5d19ecf65223e4
ALLOY_URL="https://raw.githubusercontent.com/AlloyTools/org.alloytools.alloy/$ALLOY_COMMIT/org.alloytools.kodkod.nativesat/lib/minisat-2.2.0"
ALLOY_FILES=(
    "utils/ParseUtils.h 7ef7dc817c6d1ae14a437d1382ca0955ae5f714018fab7404365eb12e8788bf8"
    "jni/kodkod_engine_satlab_MiniSat.cpp 198f78d3ecdb13c60e5de0a394cdc8e2a3001c217e597e646ebeb5bc4b289ce2"
    "jni/kodkod_engine_satlab_MiniSat.h 4e13b14434ef2d85ef97f465b5e4a49afa2504328557701d16faef5116eb9670"
)

# The x86_64 library alloy4.jar has shipped since 2012 (66360 bytes).
X86_64_SHA256=1e415d689c6541804a5192f4f61dcced338e337cc3e82f65b6bd28bcdd0befe1
# The arm64 slice committed in lib/alloy4.jar, built with Apple clang 17.0.0
# (clang-1700.0.13.5), ld-1167.5, macOS SDK 15.5. Another toolchain may give
# other bytes; the script then says so but goes on.
ARM64_SHA256=455d48388cdbbb87310b0231a20901c232bc2718e2c8ceccce8b0927b939bc48

# -std=c++98: MiniSat 2.2.0 predates C++11 (its PRIi64 literals don't compile as
# C++11). -ffp-contract=off: no fused multiply-add, so the floating point
# arithmetic (variable and clause activities) rounds as in the x86_64 build and
# both make the same decisions.
CXXFLAGS=(-std=c++98 -arch arm64 -mmacosx-version-min=11.0 -O3 -ffp-contract=off -fPIC
    -D__STDC_LIMIT_MACROS -D__STDC_FORMAT_MACROS -DKodkod_JNI)

# The arm64 Linux library: Debian 10 (buster) for linux/arm64, pinned by digest, with g++ from
# the Debian archive as of DEBIAN_SNAPSHOT (g++ 8.3.0), and the JNI headers of Azul Zulu 8u504
# for aarch64 Linux. Built there, the library needs only glibc symbols of version 2.17, glibc's
# first for aarch64, so it loads on any arm64 Linux with glibc (built on Debian 12 it needed
# 2.36). -static-libstdc++ -static-libgcc: it does not depend on the C++ runtime of the system
# that loads it either.
LINUX_IMAGE=debian:buster-slim@sha256:bb3dc79fddbca7e8903248ab916bb775c96ec61014b3d02b4f06043b604726dc
DEBIAN_SNAPSHOT=20240601T000000Z
DEBIAN_RELEASE=buster
LINUX_JDK=zulu8.96.0.205-ca-jdk8.0.504-linux_aarch64
LINUX_JDK_SHA256=c79f5fd740702a1336a9e6da2262be03e1a6e4255a94d198b66c49896be6b478
LINUX_CXXFLAGS=(-std=c++98 -O3 -ffp-contract=off -fPIC
    -D__STDC_LIMIT_MACROS -D__STDC_FORMAT_MACROS -DKodkod_JNI)
LINUX_LDFLAGS=(-shared -static-libstdc++ -static-libgcc -Wl,-soname,libminisat220.so)
# The aarch64 Linux library committed in lib/alloy4.jar.
AARCH64_LINUX_SHA256=691fc8cfafa8d3890e907716dd7ccdeaff85ea2a04eaefd13fa306b4603dd40c

# Timestamp of the jar entries, fixed so that rebuilding gives the same jar.
ENTRY_TIME=202610160000

die() {
    echo "error: $*" >&2
    exit 1
}

sha256() {
    shasum -a 256 "$1" | cut -d' ' -f1
}

check_sha256() {
    local file="$1" expected="$2"
    [ "$(sha256 "$file")" = "$expected" ] || die "$file: sha256 $(sha256 "$file"), expected $expected"
}

build_mac=false
build_linux=false
jar=""
for arg in "$@"; do
    case "$arg" in
        --mac) build_mac=true ;;
        --linux-aarch64) build_linux=true ;;
        -h|--help) sed -n '2,/^set -euo/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'; exit 0 ;;
        -*) die "unknown option $arg" ;;
        *) [ -z "$jar" ] || die "only one jar"; jar="$arg" ;;
    esac
done
if ! $build_mac && ! $build_linux; then
    build_mac=true
    build_linux=true
fi
jar="${jar:-$(dirname "$0")/../lib/alloy4.jar}"
[ -f "$jar" ] || die "$jar not found"
jar="$(cd "$(dirname "$jar")" && pwd)/$(basename "$jar")"
entry=x86-mac/libminisat220.jnilib
linux_entry=aarch64-linux/libminisat220.so
if $build_mac; then
    [ "$(uname -s)" = Darwin ] || die "--mac runs on macOS (it needs clang++ and lipo for Mach-O)"
    [ -n "${JAVA_HOME:-}" ] && [ -f "$JAVA_HOME/include/jni.h" ] || die "set JAVA_HOME to a JDK 8 (for include/jni.h)"
    for tool in clang++ lipo otool; do
        command -v "$tool" > /dev/null || die "$tool is required"
    done
fi
if $build_linux; then
    command -v docker > /dev/null || die "--linux-aarch64 needs Docker"
fi
for tool in curl perl patch unzip zip; do
    command -v "$tool" > /dev/null || die "$tool is required"
done

work="$(mktemp -d "${TMPDIR:-/tmp}/minisat220.XXXXXX")"
trap 'rm -rf "$work"' EXIT
src="$work/src"
mkdir -p "$src/jni" "$work/stage/x86-mac" "$work/stage/aarch64-linux"

echo "Fetching MiniSat 2.2.0 ($MINISAT_COMMIT)"
curl -fsSL -o "$work/minisat.tar.gz" "$MINISAT_URL"
check_sha256 "$work/minisat.tar.gz" "$MINISAT_SHA256"
tar -xzf "$work/minisat.tar.gz" -C "$work"
cp -R "$work/minisat-$MINISAT_COMMIT/core" "$work/minisat-$MINISAT_COMMIT/mtl" \
    "$work/minisat-$MINISAT_COMMIT/utils" "$src/"

echo "Fetching Kodkod's JNI glue (Alloy 5.1.0, $ALLOY_COMMIT)"
for f in "${ALLOY_FILES[@]}"; do
    path="${f% *}"
    curl -fsSL -o "$work/alloy-$(basename "$path")" "$ALLOY_URL/$path"
    check_sha256 "$work/alloy-$(basename "$path")" "${f#* }"
done
cp "$work/alloy-ParseUtils.h" "$src/utils/ParseUtils.h"
for ext in cpp h; do
    perl -pe 's/kodkod_engine_satlab_MiniSat(?=\b|_|\.)/kodkod_engine_satlab_MiniSat220/g' \
        "$work/alloy-kodkod_engine_satlab_MiniSat.$ext" > "$src/jni/kodkod_engine_satlab_MiniSat220.$ext"
done

# Current compilers reject a default argument on a friend declaration that is not
# a definition ("friend declaration specifying a default argument must be a
# definition"); move it to the definition. Same meaning.
(cd "$src" && patch -s -p1) <<'EOF'
--- a/core/SolverTypes.h
+++ b/core/SolverTypes.h
@@ -47,7 +47,7 @@
     int     x;

     // Use this as a constructor:
-    friend Lit mkLit(Var var, bool sign = false);
+    friend Lit mkLit(Var var, bool sign);

     bool operator == (Lit p) const { return x == p.x; }
     bool operator != (Lit p) const { return x != p.x; }
@@ -55,7 +55,7 @@
 };


-inline  Lit  mkLit     (Var var, bool sign) { Lit p; p.x = var + var + (int)sign; return p; }
+inline  Lit  mkLit     (Var var, bool sign = false) { Lit p; p.x = var + var + (int)sign; return p; }
 inline  Lit  operator ~(Lit p)              { Lit q; q.x = p.x ^ 1; return q; }
 inline  Lit  operator ^(Lit p, bool b)      { Lit q; q.x = p.x ^ (unsigned int)b; return q; }
 inline  bool sign      (Lit p)              { return p.x & 1; }
EOF

if $build_mac; then
    echo "Compiling the arm64 slice"
    (cd "$src" && clang++ "${CXXFLAGS[@]}" -I. -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" \
        -shared -Wl,-install_name,libminisat220.jnilib -o "$work/arm64.jnilib" \
        utils/Options.cc utils/System.cc core/Solver.cc jni/kodkod_engine_satlab_MiniSat220.cpp)
    if otool -tv -arch arm64 "$work/arm64.jnilib" | grep -qwE 'fn?m(add|sub)|fml[as]'; then
        die "the arm64 slice contains fused multiply-add instructions"
    fi
    if [ "$(sha256 "$work/arm64.jnilib")" = "$ARM64_SHA256" ]; then
        echo "arm64 slice: identical to the committed one"
    else
        echo "arm64 slice: sha256 $(sha256 "$work/arm64.jnilib"), the committed one is $ARM64_SHA256 (other toolchain?)"
    fi

    echo "Taking the x86_64 slice from $jar"
    unzip -p "$jar" "$entry" > "$work/current.jnilib"
    case "$(lipo -archs "$work/current.jnilib")" in
        x86_64) cp "$work/current.jnilib" "$work/x86_64.jnilib" ;;
        "x86_64 arm64"|"arm64 x86_64") lipo -thin x86_64 "$work/current.jnilib" -output "$work/x86_64.jnilib" ;;
        *) die "$entry in $jar has architectures $(lipo -archs "$work/current.jnilib")" ;;
    esac
    check_sha256 "$work/x86_64.jnilib" "$X86_64_SHA256"

    lipo -create "$work/x86_64.jnilib" "$work/arm64.jnilib" -output "$work/stage/$entry"
    lipo -thin x86_64 "$work/stage/$entry" -output "$work/x86_64.check"
    cmp -s "$work/x86_64.check" "$work/x86_64.jnilib" || die "the x86_64 slice changed in lipo -create"
    touch -t "$ENTRY_TIME" "$work/stage/$entry"
    (cd "$work/stage" && zip -q -X "$jar" "$entry")
    unzip -tq "$jar" > /dev/null

    echo "Updated $entry in $jar ($(lipo -archs "$work/stage/$entry"))"
    echo "  library sha256: $(sha256 "$work/stage/$entry")"
    echo "  arm64 sha256:   $(sha256 "$work/arm64.jnilib")"
fi

if $build_linux; then
    echo "Fetching the JNI headers for aarch64 Linux ($LINUX_JDK)"
    curl -fsSL -o "$work/linux-jdk.tar.gz" "https://cdn.azul.com/zulu/bin/$LINUX_JDK.tar.gz"
    check_sha256 "$work/linux-jdk.tar.gz" "$LINUX_JDK_SHA256"
    tar -xzf "$work/linux-jdk.tar.gz" -C "$work" "$LINUX_JDK/include"
    mkdir -p "$work/linux"

    echo "Compiling the aarch64 Linux library (Docker, linux/arm64)"
    docker run --rm --platform linux/arm64 \
        -v "$src:/src:ro" -v "$work/$LINUX_JDK/include:/jdk/include:ro" -v "$work/linux:/out" \
        -e DEBIAN_SNAPSHOT="$DEBIAN_SNAPSHOT" -e DEBIAN_RELEASE="$DEBIAN_RELEASE" \
        "$LINUX_IMAGE" bash -euo pipefail -c '
            rm -f /etc/apt/sources.list.d/*
            echo "deb http://snapshot.debian.org/archive/debian/$DEBIAN_SNAPSHOT $DEBIAN_RELEASE main" > /etc/apt/sources.list
            echo "deb http://snapshot.debian.org/archive/debian-security/$DEBIAN_SNAPSHOT $DEBIAN_RELEASE/updates main" >> /etc/apt/sources.list
            apt-get -o Acquire::Check-Valid-Until=false -qq update
            DEBIAN_FRONTEND=noninteractive apt-get -qq install -y --no-install-recommends g++ > /dev/null
            g++ --version | head -1 > /out/gxx-version.txt
            cd /src
            g++ '"${LINUX_CXXFLAGS[*]}"' -I. -I/jdk/include -I/jdk/include/linux '"${LINUX_LDFLAGS[*]}"' \
                -o /out/libminisat220.so \
                utils/Options.cc utils/System.cc core/Solver.cc jni/kodkod_engine_satlab_MiniSat220.cpp
            { objdump -d /out/libminisat220.so | grep -cwE "fn?m(add|sub)|fml[as]" || true; } > /out/fma.txt
            objdump -p /out/libminisat220.so | sed -n "s/^ *NEEDED *//p" | tr "\n" " " > /out/needed.txt
            objdump -T /out/libminisat220.so | grep -o "GLIBC_[0-9.]*" | sort -t. -k2,2n -u | tail -1 > /out/glibc.txt'
    [ "$(cat "$work/linux/fma.txt")" = 0 ] || die "the aarch64 Linux library contains fused multiply-add instructions"
    echo "  compiler: $(cat "$work/linux/gxx-version.txt"); needs: $(cat "$work/linux/needed.txt")(up to $(cat "$work/linux/glibc.txt"))"
    if [ "$(sha256 "$work/linux/libminisat220.so")" = "$AARCH64_LINUX_SHA256" ]; then
        echo "aarch64 Linux library: identical to the committed one"
    else
        echo "aarch64 Linux library: sha256 $(sha256 "$work/linux/libminisat220.so"), the committed one is $AARCH64_LINUX_SHA256"
    fi
    cp "$work/linux/libminisat220.so" "$work/stage/$linux_entry"
    touch -t "$ENTRY_TIME" "$work/stage/$linux_entry"
    (cd "$work/stage" && zip -q -X "$jar" "$linux_entry")
    unzip -tq "$jar" > /dev/null
    echo "Added $linux_entry to $jar"
    echo "  library sha256: $(sha256 "$work/stage/$linux_entry")"
fi
echo "  jar sha1:       $(shasum "$jar" | cut -d' ' -f1)"
