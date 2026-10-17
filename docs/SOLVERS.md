# Choosing the SAT solver

`AlloyAnalyzer.build_A4Options()` (`src/ar/uba/dc/rfm/dynalloy/analyzer/AlloyAnalyzer.java`)
chooses the SAT solver from the JVM system property `dynalloy.solver`
(`AlloyAnalyzer.SOLVER_PROPERTY`). It is a property of the JVM that runs
DynAlloy, which in practice is TACO's: give the `java` command that runs TACO
the option `-Ddynalloy.solver=sat4j`, for example.

| Value | Solver |
|---|---|
| `auto` (default, also when the property is not set) | MiniSat 2.2 (Kodkod's `MiniSat220`, through JNI) if `lib/alloy4.jar` has its library for this platform and it loads; SAT4J otherwise, with one line on stderr saying why, e.g. (on arm64 Linux, with an alloy4.jar from before `aarch64-linux/libminisat220.so` was added) `DynAlloy: solving with SAT4J, because alloy4.jar has no MiniSat 2.2 for this platform (aarch64-linux/libminisat220.so).` |
| `minisat` | MiniSat 2.2. If it cannot be used, the analysis fails with an `IllegalStateException` that says why. |
| `sat4j` | SAT4J, in Java, on every platform. |
| anything else | `IllegalArgumentException`. |

The value is not case sensitive. "Can be used" is decided once per JVM: the
library must be in `alloy4.jar` under `<platform folder>/<file name>` (see
[NATIVE-LIBRARIES.md](NATIVE-LIBRARIES.md)), and creating and freeing one
MiniSat 2.2 solver must work. `auto` makes its choice at the first analysis and
keeps it.

`minisat` means MiniSat 2.2 (`A4Options.SatSolver.MiniSat220JNI`), not the
older MiniSat that Alloy calls `MiniSatJNI`.

## Which one to use

When both finish they give the same verdicts, but not always the same
counterexample, and
MiniSat 2.2 is much faster on the larger problems. On the stryker branch of
TACO, measured on 84 of its drivers (the JUnit test classes under `unittest/`
that run TACO) on Apple Silicon with JDK 8 (2026-10-17): 46
of them reach the solver; two that time out with SAT4J finish with MiniSat
(410 s and 208 s), and some analyses go from 706 s to 16 s or from 875 s to
45 s. Stryker's search depends on the counterexamples, so with SAT4J its
results differ; TACO's smoke test (`scripts/smoke-test.sh`) always uses MiniSat
2.2.

Use `sat4j` where there is no MiniSat 2.2 (Windows, FreeBSD, other
architectures) or to compare; `minisat` where silently falling back to SAT4J
would be a mistake, e.g. in benchmarks.

## Where it is set

- `AlloyAnalyzer.analyzeCommand` always uses `build_A4Options()`. Its
  `A4Options` parameter, `DynAlloyOptions.alloyOptions` and the solver menu of
  the visualizer (`DynAlloyVizGUI`) are ignored (see
  [KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md)).
- TACO's scripts on its stryker branch pass `-Ddynalloy.solver=minisat`
  (`scripts/run-benchmark.sh`, `STRYKER_SOLVER=minisat|sat4j`).

## History

dynalloy4's master hardcodes SAT4J in `build_A4Options()`. The stryker branch
added the property (08c2fd8, then SAT4J by default), and made `auto` the
default (40c1beb) once MiniSat 2.2 loaded on Apple Silicon, arm64 Linux and
JVMs that hide `ClassLoader.usr_paths` (see
[NATIVE-LIBRARIES.md](NATIVE-LIBRARIES.md)).
