# AGENTS.md

Notes for coding agents, and people, changing this repository. Start with
[README.md](README.md); the details are in [docs/](docs/).

## What this is

DynAlloy: it translates DynAlloy models (`.dals`) into Alloy (`.als`) and runs
the Alloy Analyzer on them. It is the third of three repositories that TACO
uses:

    Java + JML ──TACO──▶ JDynAlloy ──jDynAlloy──▶ DynAlloy ──dynalloy4──▶ Alloy ──▶ SAT

- [mffrias/TACO](https://github.com/mffrias/TACO) drives everything and is
  where changes are tested end to end;
- [mffrias/jDynAlloy](https://github.com/mffrias/jDynAlloy) builds DynAlloy
  models with this repository's AST classes and bundles `dist/dynalloy4.jar`;
- this repository is used as a library: the command line does not work on its
  own ([docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md)).

## Build

JDK 8 and Ant (javac 21 and 25 reject `source`/`target` 1.7):

    ant jar
    git checkout build.num

`ant jar` writes `dist/dynalloy4.jar` and increments `build.num`, which is in
git; put it back unless you mean to release. `bin/` and `dist/` are ignored.

## Tests

`test/` is not in `build.xml`. JUnit 4.11 (`lib/junit.jar`) needs
hamcrest-core 1.3, which is not in `lib/`; TACO's `lib/` has one. From this
directory, with TACO cloned next to it (as `setupTACO.sh` does):

    ant build
    mkdir -p /tmp/dynalloy4-tests
    javac -nowarn -d /tmp/dynalloy4-tests -cp bin:lib/alloy4.jar:lib/antlr.jar:lib/junit.jar $(find test -name "*.java")
    java -cp /tmp/dynalloy4-tests:bin:lib/alloy4.jar:lib/antlr.jar:lib/junit.jar:../TACO/lib/org.hamcrest.core_1.3.0.v201303031735.jar org.junit.runner.JUnitCore $(cd /tmp/dynalloy4-tests && find . -name "*Test.class" ! -name '*$*' | sed 's|^\./||; s|\.class$||; s|/|.|g')

Expect `Tests run: 193,  Failures: 21`: the same 21 fail on master, see
[docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md). A change should not add
failures. The tests leave `temp.dals` in the repository root; delete it.

The real check is TACO: copy `dist/dynalloy4.jar` into `../jDynAlloy/lib` and
`../TACO/lib`, rebuild jDynAlloy (`ant jar`, on its `stryker` branch) and copy
its `dist/jdynalloy.jar` into `../TACO/lib`, then run TACO's smoke test
(`scripts/smoke-test.sh` on TACO's stryker branch; see TACO's
`docs/RUNNING.md`), which checks four repair benchmarks against their known
results.

## Rules of thumb

- **Generated parsers**: edit `src/ar/uba/dc/rfm/alloy/parser/expression.g`
  and `src/ar/uba/dc/rfm/dynalloy/parser/dynalloy.g`, not the generated
  `*Lexer.java`/`*Parser.java`; `ant build` regenerates them (ANTLR 2.7.6) and
  they are committed. `ant clean` deletes them; `ant build` writes them back.
- **`lib/alloy4.jar`** is binary and shared by everything built on it. Change
  its native libraries only with `scripts/build-minisat220.sh`, which is
  reproducible ([docs/NATIVE-LIBRARIES.md](docs/NATIVE-LIBRARIES.md)), and
  check the jar's sha1 before and after.
- **Solver**: chosen by `-Ddynalloy.solver` in
  `AlloyAnalyzer.build_A4Options()` ([docs/SOLVERS.md](docs/SOLVERS.md)); the
  `A4Options` passed around elsewhere are ignored.
- **Static state**: `AlloyAnalyzer`'s results and the
  `isCheckAndAfterRunSpec` flags are static, so one analysis at a time per JVM.
- **TACO-specific inputs** (`translatingForStryker`, `isCheckAndAfterRunSpec`,
  `inputToFix`, the four maps) change the generated assertion; see
  [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) before touching
  `DynalloyXlatorVisitor.visit(AssertionDeclaration)`.
- **Branches**: `master` is the maintainer's; the Stryker port's changes are on
  `stryker`, which TACO's `stryker` branch builds against.
- Java 7 syntax only (no lambdas): the build compiles with `source` 1.7.
  Follow the indentation of the file you edit (some use tabs, some spaces).
- Open points about intent are in
  [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md); ask rather than guess.
