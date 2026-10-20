# Open questions

Points where the code does not say what was intended. They are collected here
for the maintainer (Marcelo Frias) instead of being guessed in the other pages.
Line numbers are those of the stryker branch, 2026-10-17.

1. **Specified output (`isCheckAndAfterRunSpec`).** In
   `DynalloyXlatorVisitor.visit(AssertionDeclaration)` the program's formula is
   used only to collect the variables' indices, and the assertion is
   `pre => not post`. In `addIdxsToPostToGenerateOutput` the remapping of the
   post's indices to the program's last index is commented out, so primed
   variables stay at index 1. Is that the intended model (any output that
   satisfies the contract, for the fixed input), and is the commented-out code
   meant to come back?
2. **`inputToFix` in DynAlloy.** Its only use is to widen the index range of the
   module's variables in the second copy of the invariant predicates (at the last
   indices) from `[b, b]` to `[b, b + 1]` (lines 393-402, added in b58e74b). What is the widening for, and
   for which translations? TACO's stryker branch now passes the input to DynAlloy
   only when it is not translating for Stryker (you said the block was not meant
   for Stryker's feedback checks).
3. **Both flags at once.** With `translatingForStryker` and
   `isCheckAndAfterRunSpec` both set, the post is negated twice. Should that
   combination be rejected, or is it impossible by construction?
4. **`alloy4.jar`.** Its `Version` class says 4.1.10 (2009), but it has later
   classes (`MiniSat220`, `Lingeling`) and RFM's switches
   (`TranslateAlloyToKodkod.boundsHackEnabled`,
   `removeAlloySyntaxChecksHackEnabled`). Which Alloy sources is it built from,
   and are its changes recorded anywhere?
5. **Standalone use.** The command line, the visualizer and 13 of the tests fail
   with a `NullPointerException`, because the translator now needs TACO's maps
   and an assertion named `check_<program>`. Should DynAlloy work on its own
   again (for example with empty maps and any assertion name), or is it a
   library for TACO only? Should the 21 failing tests be fixed or removed?
6. **The appended functions.** For each program without a parameter
   `throw: java_lang_Throwable + null`, the translator appends
   `fun <program>[<params>] : <return type> { <first parameter> }`, with the
   parameter types read from a new, empty typing (lines 150-169), so they
   would print as `null`. TACO's programs all have `throw`, so it does not
   happen there. What is it for?
7. **Building with newer JDKs.** The build needs JDK 8 (`source`/`target` 1.7).
   Would you take `release="8"` on the `javac` task, as TACO's stryker branch
   does? Ant ignores it on JDK 8, so that build stays byte for byte the same,
   and JDK 21 and 25 would build it for Java 8.
8. **`dynalloy.dataflow` and `da2a.simplify`** are not used by anything outside
   their packages. Are they still needed?

## From the Javadoc pass

9. **`AlloyAnalyzer.a4reporter` and `solvingTime`** (`analyzer/AlloyAnalyzer.java:43`, `47`):
   these public static fields are never assigned (always null and -1) and nothing reads them;
   only `answer` and `log4Jreporter` are written. Are they meant to be filled (for example, so
   that TACO can read the solving time), or can they go?
10. **`AssertionNotFound`** (`parser/AssertionNotFound.java`): `compile`, `translateDynAlloyAST`,
    `parse` and others declare it and TACO catches it, but nothing throws it, and nothing has
    since the first commit. Should a module without an assertion throw it? Today it ends in a
    `NullPointerException` in `DynalloyXlatorVisitor.visit(DynalloyModule)`.
11. **`import` sections** (`parser/splitter/DocumentSplitter.java:108-113`, `124-126`,
    `beginsDynalloyCheck`): `import` starts a section, but `getKind` returns null for it, so
    `DynalloyParser.java:102` throws a `NullPointerException`. What are `import` sections for:
    should the keyword get a kind, or be removed?
