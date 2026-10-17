dynalloy4
=========

DynAlloy extends Alloy with *actions* (pre/post pairs over relations) and
*programs* built from them (sequence, choice, iteration, `while`, `if`,
assignments, calls), plus partial-correctness assertions about programs. This
repository is the DynAlloy compiler and analyzer that TACO uses: it reads a
DynAlloy model (`.dals`), unrolls its loops a bounded number of times,
translates it into an Alloy model (`.als`), and runs the Alloy Analyzer on it,
which solves it with a SAT solver (MiniSat 2.2 or SAT4J).

How it fits with TACO and jDynAlloy
-----------------------------------

TACO checks Java methods annotated with JML by translating them, in steps that
live in three repositories, into a SAT problem:

    Java + JML ──TACO──▶ JDynAlloy ──jDynAlloy──▶ DynAlloy ──dynalloy4──▶ Alloy ──Alloy Analyzer──▶ SAT

| Repository | What it does |
|---|---|
| [mffrias/TACO](https://github.com/mffrias/TACO) | Parses Java + JML, translates it into JDynAlloy, runs the whole pipeline and turns counterexamples into JUnit tests. Its `stryker` branch adds Stryker, which repairs faulty methods. |
| [mffrias/jDynAlloy](https://github.com/mffrias/jDynAlloy) | Translates JDynAlloy (an object-oriented layer over DynAlloy) into DynAlloy, using this repository's AST classes. |
| [mffrias/dynalloy4](https://github.com/mffrias/dynalloy4) (this one) | Translates DynAlloy into Alloy and runs the Alloy Analyzer (`lib/alloy4.jar`: Alloy 4 with Kodkod, SAT4J and native SAT solvers). |

TACO writes the DynAlloy model that jDynAlloy produces to `output/output.dals`,
calls `DynAlloyCompiler.compile` to get `output/output.als`, and then
`AlloyAnalyzer.analyzeCommand` to solve it. A satisfiable result is a
counterexample to the method's contract. TACO also passes in what the
DynAlloy model itself cannot express: the predicates and typings that come from
arithmetic in object invariants and contracts, and its flags for Stryker and for
computing a method's specified output (see
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)). The standalone command line does
not work on this branch (see
[docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md)); use DynAlloy through
TACO.

Building
--------

Requirements: a JDK 8 and Ant (checked with Zulu 8u504 and Ant 1.9.16). javac
21 and 25 reject the build's `source`/`target` 1.7.

    git clone https://github.com/mffrias/dynalloy4.git
    cd dynalloy4
    ant jar

`ant jar` generates the ANTLR 2.7.6 parsers from the grammars, compiles `src/`
into `bin/` and writes `dist/dynalloy4.jar`, which also contains every jar in
`lib/` (`alloy4.jar`, `antlr.jar`, `junit.jar`) and `dist/dynalloy4_src.jar`.
Other targets: `ant build` (parsers and classes only, the default) and
`ant clean` (deletes `bin/` and the generated parser sources, which are in git;
the next `ant build` writes them again, byte for byte). `ant jar` increments
the build number in `build.num`, which is in git:

    git checkout build.num

TACO's `setupTACO.sh` does all of this for you: it clones the three
repositories side by side, builds this one, and copies `dist/dynalloy4.jar` into
`jDynAlloy/lib` and `TACO/lib`. After changing this repository, rebuild the jar
and then jDynAlloy and TACO, which bundle or use it.

Branches
--------

- `master`: Marcelo Frias's line of development.
- `stryker`: what TACO's `stryker` branch needs (Stryker port): the SAT solver
  is chosen with `-Ddynalloy.solver` (MiniSat 2.2 when available), MiniSat 2.2
  for Apple Silicon and arm64 Linux, and loading fixes for newer JVMs. TACO's
  `setupTACO.sh` builds this branch when TACO is on its `stryker` branch.
  TACO's [docs/FIXES-FOR-MASTER.md](https://github.com/mffrias/TACO/blob/docs/docs/FIXES-FOR-MASTER.md)
  describes each of these changes (the bug, its cause in master, the commit,
  how it was checked), with those in TACO and jDynAlloy.
- `docs`: this documentation, on top of `stryker`.

Choosing the SAT solver
-----------------------

The JVM property `dynalloy.solver` of the program that uses DynAlloy (TACO)
selects the solver: `auto` (the default) uses MiniSat 2.2 if `lib/alloy4.jar`
has it for this platform and it loads, and SAT4J otherwise, saying so on
stderr; `minisat` requires MiniSat 2.2; `sat4j` always uses SAT4J. See
[docs/SOLVERS.md](docs/SOLVERS.md).

MiniSat 2.2 for ARM
-------------------

`lib/alloy4.jar` used to have MiniSat 2.2 only for x86 and x86_64. On this
branch `x86-mac/libminisat220.jnilib` is a universal library (its x86_64 slice
is the original one, byte for byte, plus an arm64 slice), and
`aarch64-linux/libminisat220.so` is new. `scripts/build-minisat220.sh` builds
both from pinned sources and reproduces the committed jar. Everything about the
native libraries (platforms, sources, flags, how to rebuild them, how they are
extracted and loaded) is in
[docs/NATIVE-LIBRARIES.md](docs/NATIVE-LIBRARIES.md).

Documentation
-------------

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): packages, parsing, the
  DynAlloy → Alloy translation, the analyzer, and what TACO passes in.
- [docs/SOLVERS.md](docs/SOLVERS.md): `-Ddynalloy.solver`.
- [docs/NATIVE-LIBRARIES.md](docs/NATIVE-LIBRARIES.md): the SAT solvers in
  `alloy4.jar`, MiniSat 2.2 for ARM, extraction and loading.
- [docs/KNOWN-LIMITATIONS.md](docs/KNOWN-LIMITATIONS.md): what does not work.
- [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md): questions for the
  maintainer about intent.
- [AGENTS.md](AGENTS.md): notes for coding agents (and people) changing this
  repository.

References / how to cite
------------------------

DynAlloy:

- M. F. Frias, C. G. López Pombo, G. A. Baum, N. M. Aguirre, T. Maibaum.
  “Taking Alloy to the Movies.” LNCS, 2003, pp. 678–697.
  DOI [10.1007/978-3-540-45236-2_37](https://doi.org/10.1007/978-3-540-45236-2_37).
- M. F. Frias, J. P. Galeotti, C. G. López Pombo, N. M. Aguirre. “DynAlloy:
  Upgrading Alloy with Actions.” ICSE 2005, pp. 442–451.
  DOI [10.1145/1062455.1062535](https://doi.org/10.1145/1062455.1062535).

TACO, its main user:

- J. P. Galeotti, N. Rosner, C. G. López Pombo, M. F. Frias. “TACO: Efficient
  SAT-Based Bounded Verification Using Symmetry Breaking and Tight Bounds.”
  IEEE TSE 39(9):1283–1307, 2013.
  DOI [10.1109/TSE.2013.15](https://doi.org/10.1109/TSE.2013.15).

TACO's README has the full list, with the work built on TACO.

Licenses
--------

DynAlloy's sources say GNU GPL version 2 or later in their headers (for example
`src/ar/uba/dc/rfm/dynalloy/DynAlloyCompiler.java`). The licenses of the
bundled third-party code are in `LICENSES/`: Alloy, JavaCup, Kodkod, MiniSat
(and `MiniSat-2.2.txt`, as in its sources), SAT4J and ZChaff.
