#!/usr/bin/env bash
# Makes x86-mac/libminisat220.jnilib in alloy4.jar a universal (x86_64 + arm64)
# library, so that MiniSat 2.2 (-Ddynalloy.solver=minisat) also loads in an
# arm64 JVM on Apple Silicon. The x86_64 slice is the one alloy4.jar already
# has, byte for byte; only the arm64 slice is compiled, from the sources below.
# See README.md, "MiniSat 2.2 on Apple Silicon".
#
# Usage: scripts/build-minisat220.sh [ALLOY4_JAR]   (default: this repository's lib/alloy4.jar)
#
# The jar is updated in place; running the script again on the result rebuilds
# the arm64 slice and keeps the same x86_64 slice. Needs macOS with the Xcode
# command line tools (clang++, lipo, otool), JAVA_HOME pointing at a JDK 8 (only
# its JNI headers are used), curl, perl, patch, unzip and zip.
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

# Timestamp of the jar entry, fixed so that rebuilding gives the same jar.
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

jar="${1:-$(dirname "$0")/../lib/alloy4.jar}"
[ -f "$jar" ] || die "$jar not found"
jar="$(cd "$(dirname "$jar")" && pwd)/$(basename "$jar")"
entry=x86-mac/libminisat220.jnilib
[ "$(uname -s)" = Darwin ] || die "this script runs on macOS (it needs clang++ and lipo for Mach-O)"
[ -n "${JAVA_HOME:-}" ] && [ -f "$JAVA_HOME/include/jni.h" ] || die "set JAVA_HOME to a JDK 8 (for include/jni.h)"
for tool in clang++ lipo otool curl perl patch unzip zip; do
    command -v "$tool" > /dev/null || die "$tool is required"
done

work="$(mktemp -d "${TMPDIR:-/tmp}/minisat220.XXXXXX")"
trap 'rm -rf "$work"' EXIT
src="$work/src"
mkdir -p "$src/jni" "$work/stage/x86-mac"

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
echo "  jar sha1:       $(shasum "$jar" | cut -d' ' -f1)"
