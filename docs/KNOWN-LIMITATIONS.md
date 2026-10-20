# Known limitations

What does not work, or works in a way that surprises, on the stryker branch.
Each item says whether it was seen in a run or read from the code. Checked on
2026-10-17 with Zulu 8u504 (arm64) on macOS, unless stated otherwise.

## DynAlloy works only through TACO

**The command line fails** (seen, the same on master a2ff826):

    java -cp dist/dynalloy4.jar ar.uba.dc.rfm.dynalloy.Main --input test/regr/swap/swap.dals --output /tmp/swap.als

ends in a `NullPointerException` at `DynalloyXlatorVisitor.java:504`, with or
without `--assertion`, and writes nothing. The translator expects what TACO
passes in: the four maps of invariant and contract predicates and typings (the
console passes empty ones, so `predsToAddThatComeFromInvariant` stays null), an
`assertionId` of the form `check_<program>`, and, without
`--removeQuantifiers`, a typing for that program. `java -jar dist/dynalloy4.jar`
says `no main manifest attribute`.

The visualizer (`Main viz`) uses the same translation with empty maps
(`DynalloyVisualizerController`), so it presumably fails the same way (read
from the code, not tried).

Some help text is wrong (seen): it lists a `gui` mode that `Main` does not
handle, and `--applySimplifications`, which does not exist, and calls
`--output` mandatory, which `ConsoleArguments` treats as optional.
`--buildDynAlloyTrace` is listed but rejected as an unknown argument (read from
the code).

## Tests

`test/` is not part of `build.xml`. Compiled and run by hand (see
[AGENTS.md](../AGENTS.md)), **21 of its 193 tests fail**, the same 21 on master
(seen):

- 13 with a `NullPointerException` in `DynalloyXlatorVisitor` (lines 285, 470
  and 504), the same cause as the command line: `DynAlloyAnalyzerTest`,
  `LocalVariableTest`, `DynalloyToAlloyTranslatorTest`, `SwapTranslatorTest`,
  `regr.new_grammar.*` and `regr.viz.*`;
- 6 in `DocumentSplitterTest`, which expect `alloy` sections where the splitter
  now returns `pred` sections;
- 1 `IndexOutOfBoundsException` in `New_GrammarTest.old_grammar_test`.

`lib/junit.jar` is JUnit 4.11, which needs hamcrest-core 1.3; it is not in
`lib/`. The tests use paths relative to the repository root and leave a
`temp.dals` there.

## Building

- **JDK 8 only** (seen): javac 21 and 25 stop with `Source option 7 is no
  longer supported` (`build.xml` has `source`/`target` 1.7). The built jar runs
  on JDK 8, 21 and 25 (seen through TACO).
- `ant jar` changes `build.num`, which is in git; `ant clean` deletes the
  generated parser sources, which are in git too, and the next `ant build`
  writes them again identically (seen).
- `dist/dynalloy4.jar` includes JUnit, because the jar target bundles every jar
  in `lib/` (seen: 225 `org/junit` entries).
- `dist/dynalloy4_src.jar` has no grammars: its pattern `src/**/.g` lacks a `*`
  (seen: no `.g` file in it).

## Solver and analyzer

- The solver is chosen only by `-Ddynalloy.solver` ([SOLVERS.md](SOLVERS.md)).
  `AlloyAnalyzer.analyzeCommand` ignores its `A4Options` argument, so
  `DynAlloyOptions.alloyOptions` (default `MiniSatJNI`) and the visualizer's
  solver menu have no effect (read from the code).
- The analyzer's reporter cannot be changed: the `A4Reporter` given to the
  `AlloyAnalyzer` constructor, `setReporter` and
  `DynAlloyAnalyzer.setVerbosity` are ignored, and the progress report always
  goes to `System.out` (read from the code).
- **No MiniSat 2.2 on Windows, FreeBSD or other architectures**: `auto` falls
  back to SAT4J, `minisat` fails. On a 64-bit Windows JVM the platform folder is
  `amd64-windows`, which the jar does not have, so no native solver at all is
  found there (read from the code, not tried on Windows).
- On JDK 8 the first analysis replaces `ClassLoader.usr_paths` instead of
  adding to it (see [NATIVE-LIBRARIES.md](NATIVE-LIBRARIES.md); read from the
  code).

## Translation

- **One analysis at a time per JVM.** `AlloyAnalyzer` keeps its result and its
  reporter in static fields (`answer`, `log4Jreporter`), and
  `DynAlloyCompiler.isCheckAndAfterRunSpec` is a static flag that is copied into
  `DynAlloyTranslator` and `DynalloyXlatorVisitor` at each compilation (read
  from the code).
- **Only one assertion of a module is translated**: the first in a `HashSet`'s
  iteration order, which need not be the first in the file. The check that there
  is exactly one is a Java `assert`, so it only fails with `-ea` (read from the
  code).
- **Bounded**: loops and iterations are unrolled `loopUnroll` times (3 by
  default on the command line; TACO always sets it), so executions that need
  more iterations are not considered; without `strictUnrolling` each unrolled
  iteration may also be skipped.
- `skip` is translated as `TruePred[]`, which the model has to declare (read
  from the code).
