# Architecture

DynAlloy turns a DynAlloy model into an Alloy model and asks the Alloy Analyzer
to check it. This page follows a model through the code. Paths are relative to
`src/ar/uba/dc/rfm/`.

    .dals ──parse──▶ DynAlloy AST ──plugins──▶ unroll loops ──translate──▶ Alloy AST ──print──▶ .als ──Alloy Analyzer──▶ Kodkod ──▶ SAT

## Where it sits

TACO (github.com/mffrias/TACO) translates Java + JML into JDynAlloy modules;
jDynAlloy (github.com/mffrias/jDynAlloy) translates those into DynAlloy, built
with this repository's AST classes (`alloy.ast`, `dynalloy.ast`), and prints
them into one `.dals` file. TACO then calls this repository twice:

- `dynalloy.DynAlloyCompiler.compile(...)` (from TACO's
  `DynalloyToAlloyManager`), to translate `output/output.dals` into
  `output/output.als`;
- `dynalloy.analyzer.AlloyAnalyzer.analyzeCommand(...)` (from TACO's
  `AlloyStage`), to run the first command of `output.als`.

TACO also registers its own plugins with the compiler (scope inference, the
`check` command with TACO's scopes, symmetry breaking, upper bounds, Java
arithmetic), and passes in four maps that DynAlloy cannot compute itself: the
predicates and typings that come from arithmetic in object invariants (per
module) and in contracts (per program).

## Packages

| Package | Contents |
|---|---|
| `alloy` | Alloy variables (`AlloyVariable`, indexed as `x_0`, `x_1`, …), typings, index ranges |
| `alloy.ast`, `alloy.ast.expressions`, `alloy.ast.formulas` | the Alloy AST: modules, signatures, facts, predicates, assertions, expressions, formulas |
| `alloy.parser` | ANTLR grammar for Alloy expressions and formulas (`expression.g`) |
| `alloy.util` | printers (`AlloyPrinter`, `FormulaPrinter`), visitors, mutators, substitution |
| `dynalloy` | entry points: `DynAlloyCompiler`, `DynAlloyTranslator`, `DynAlloyOptions`, `DynAlloyAnalyzer`, `DynAlloyConsole`, `Main` |
| `dynalloy.ast`, `dynalloy.ast.programs` | the DynAlloy AST: modules, actions, programs, assertions; program constructs |
| `dynalloy.parser`, `dynalloy.parser.splitter` | the DynAlloy grammar (`dynalloy.g`) and the splitter that cuts a file into sections |
| `da2a.prepare` | `ClosureRemover`: loop unrolling |
| `da2a.qremover` | quantifier removal (`QuantifierRemover`, `GlobalSigAttacher`) |
| `dynalloy.xlator` | the translator (`DynalloyXlatorVisitor`, `ProgramTranslator`, `SpecContext`, `CallingConvention`, indexers and joiners) |
| `dynalloy.analyzer` | `AlloyAnalyzer` (solver choice, Alloy → Kodkod → SAT) and `AlloyJNILibraryPath` (native libraries) |
| `dynalloy.plugin` | the plugin interfaces |
| `dynalloy.trace` | turns a counterexample into a DynAlloy trace |
| `dynalloy.visualization` | the Swing visualizer/debugger `DynAlloyVizGUI` and its mapping between DynAlloy and Alloy |
| `dynalloy.dataflow` | control-flow graph and bounds/points-to analyses (with their own `main`s); nothing outside the package uses them |
| `da2a.simplify` | spec simplifications; nothing outside the package uses them |

## Parsing

`dynalloy.parser.DynalloyParser` first splits the file into sections
(`parser.splitter.DocumentSplitter`), by the keyword each one starts with:
`module`, `open`, `sig`/`abstract`/`one`, `pred`, `action`/`act`,
`program`/`prog`, `assertCorrectness` and `import`. The splitter gives an
`import` section no kind, so parsing a file with one fails with a
`NullPointerException` (read from the code). Actions, programs and assertions are parsed with the
ANTLR grammar. Signatures, `module` and `open` are parsed with it too (for the
signatures' fields, the module's name and the opened modules) and passed
through to the Alloy output as text; everything else, including Alloy
predicates, facts, functions and `check`/`run` commands, is only passed
through. The parser keeps one action per name, one program per name and number
of parameters, and one assertion per name: the last one in the file.

The grammars are ANTLR 2.7.6 (`lib/antlr.jar`). `dynalloy/parser/dynalloy.g`
extends the expression grammar `alloy/parser/expression.g`; ANTLR merges them
into `expandeddynalloy.g`. `ant build` (target `generateParser`) regenerates the
Java parsers into `src/`, where they are committed; edit the `.g` files, not
the generated `.java` files.

A small example, `test/regr/swap/swap.dals`:

    action updateVar[l:univ, r:univ] {
      pre { TruePred[] }
      post { equ[l',r] }
    }

    program swap[a: univ,b: univ]
      var [ temp: univ ] {
    updateVar[temp,a];
    updateVar[a,b];
    updateVar[b,temp]
    }

    assertCorrectness checkSwap[a: univ, b: univ] {
      pre = {checkSwapPre[a,b]}
      program = { call swap[a,b] }
      post = { checkSwapPost[a,b,a',b'] }
    }

    check checkSwap

Primed variables (`a'`) are values after the program; `\pre[x]` is a value
before it.

Program constructs (`dynalloy.g`, rule `atomicPrograms`):

| Syntax | AST | Meaning |
|---|---|---|
| `p ; q` | `Composition` | sequence |
| `p + q` | `Choice` | nondeterministic choice |
| `p*`, `repeat { p }` | `Closure` | iteration |
| `[f]?`, `assume f` (optionally `lblpos L` / `lblneg L`) | `TestPredicate` | test (blocks unless `f` holds) |
| `a[x, y]`, `m/a[x, y]` | `InvokeAction` | action call |
| `call P[x, y]` | `InvokeProgram` | program call |
| `x := e` | `Assigment` | assignment |
| `while f do { p }` | `WhileProgram` | loop |
| `if f { p } else { q }` | becomes `([f]? ; p) + ([!f]? ; q)` | conditional |
| `skip` | `Skip` | no-op |

## Translation

`DynAlloyCompiler.compile(input, output, options, 4 maps, translatingForStryker, inputToFix)`:

1. reads and parses the `.dals` file;
2. applies the registered DynAlloy-AST plugins;
3. translates it with `DynAlloyTranslator.translateDynAlloyAST`;
4. applies the Alloy-AST plugins;
5. prints the Alloy AST (`AlloyPrinter`), after a comment with the options;
6. applies the string plugins and writes the `.als` file.

`DynAlloyTranslator` first **unrolls loops** (`da2a.prepare.ClosureRemover`),
with `DynAlloyOptions.loopUnroll` = k (TACO's `dynalloy.toAlloy.loopUnroll`):

- `p*` becomes `skip` if k = 0, and otherwise k copies of `(p + skip)` in
  sequence, or of `p` with `strictUnrolling`;
- `while c do { B }` becomes k nested levels of
  `([c]? ; B ; next) + ([!c]? ; skip)`, followed by `[!c]?` unless
  `removeExitWhileGuard` is set. So executions that need more than k
  iterations are cut off: the analysis is bounded.

Then `xlator.DynalloyXlatorVisitor` translates the loop-free module, with a
`SpecContext` that knows every program by `module::id:arity` and every action
by `module::id`:

- **Variables are indexed.** In an action, `x` is `x_0` and `x'` is `x_1`
  (`FirstSubindexer`). A sequence shifts the indices of its second part by the
  last index of the first (`CompositionJoiner`); a choice is an `or` of its
  branches, padded with equalities so that every branch ends on the same index
  (`ChoiceJoiner`). An assignment `x := e` becomes `x_1 = e_0`.
- **An action** becomes an Alloy `pred` over its parameters at every index,
  `pre and post`.
- **A program** becomes a `pred` over its parameters and local variables at
  every index. Calling it instantiates its calling convention, and its local
  variables get a fresh prefix (`l0_`, `l1_`, …) at each call.
- **A test** becomes its predicate, negated for the false branch; `skip`
  becomes `TruePred[]`, which the model has to declare.
- **The assertion** becomes
  `assert id { all <variables> | (pre and invariants and program) => post }`.
  Only one `assertCorrectness` of the module is translated (the code asserts
  that there is exactly one, which Java checks only with `-ea`): the first in a
  `HashSet`'s iteration order, which need not be the first in the file. In the
  post, primed variables get their last index in the whole antecedent (pre,
  invariant and contract predicates, program), and index 0 if they do not occur
  in it. The invariant predicates come from TACO's maps and are in the
  antecedent twice, at index 0 and at each variable's last index; the antecedent
  also has the arithmetic predicates of the called program's contract, from
  TACO's maps too. `options.assertionId` must be `check_<program>`: the part
  after `check_` is the key of the program's arithmetic typing in TACO's map.
- With `removeQuantifiers`, and if the assertion's id is `options.assertionId`,
  the variables of the assertion's quantifier become fields of a signature `QF`
  (and references to them `QF.x`), the formula is broken into parts
  (`FormulaBreaker`), all of them but the last become facts, and the last one is
  the assertion. A `one sig QF` is always declared.

No `check` command is generated: the commands are the ones in the `.dals` text
(TACO appends its own with `DynAlloyAppendCommandPlugin`).

### What TACO passes in

Three inputs change the assertion. TACO sets them; nothing else in this
repository does.

| Input | Effect |
|---|---|
| `translatingForStryker` (argument of `compile`) | The post is negated: `(pre and program) => not post`. A counterexample is then an execution that satisfies the post, which is what Stryker's feedback checks look for. |
| `DynAlloyCompiler.isCheckAndAfterRunSpec` (static) | For computing a method's specified output: the program's formula is left out (it is only used to find the variables' indices), the post is indexed with `FirstSubindexer` only, and the assertion is `pre => not post`. A counterexample is an input/output pair that satisfies the contract. If `translatingForStryker` is also set, the post is negated twice. |
| `inputToFix` (argument of `compile`) | Only checked for null. When set, the index range of the module's variables in the second copy of the invariant predicates (at the last indices) is widened from `[b, b]` to `[b, b + 1]`. The post's primed occurrences of those variables then get `b + 1` too, since they take the last index in the antecedent. |

TACO's stryker branch passes `inputToFix` only when not translating for
Stryker. What the second and third inputs are meant to achieve in detail is in
[OPEN-QUESTIONS.md](OPEN-QUESTIONS.md).

### DynAlloyOptions

| Field | Default | Note |
|---|---|---|
| `loopUnroll` | 0 | 3 in `DEFAULT_DYNALLOY_OPTIONS`, which the console uses; TACO always sets it |
| `strictUnrolling` | false | |
| `removeQuantifiers` | false | |
| `remove_exit_while_guard` | false | |
| `assertionId` | null | |
| `module_under_analysis_name` | | used for the invariants |
| `programSpecs` | | read by TACO's scope inference plugin |
| `run_alloy_analyzer` | false | used by `DynAlloyAnalyzer` |
| `output_filename` | null | the `.als` that `DynAlloyAnalyzer` writes and analyses; if null, the `.dals` path with `.als` |
| `build_dynalloy_trace` | false | only printed in the header of the `.als` |
| `alloyOptions` | `MiniSatJNI` | ignored, see [SOLVERS.md](SOLVERS.md) |

## Analysis

`AlloyAnalyzer.analyzeCommand(command, options)`:

1. builds its options with `build_A4Options()`, which chooses the SAT solver
   ([SOLVERS.md](SOLVERS.md)); the `options` argument is not used;
2. makes the native solvers loadable (`AlloyJNILibraryPath`,
   [NATIVE-LIBRARIES.md](NATIVE-LIBRARIES.md));
3. sets two switches of this `alloy4.jar`,
   `TranslateAlloyToKodkod.boundsHackEnabled = false` and
   `removeAlloySyntaxChecksHackEnabled = true`;
4. parses and type-checks the `.als` (`CompUtil.parseEverything_fromFile`);
5. picks the command (the first one if `command` is null, as TACO does);
6. translates it to Kodkod and solves it
   (`TranslateAlloyToKodkod.execute_command`);
7. returns an `AlloyAnalysisResult`: satisfiable or not, the Alloy module, the
   command and the `A4Solution`. For a `check`, satisfiable means a
   counterexample. `nextSolution` asks the solver for the next one.

`DynAlloyAnalyzer` combines compiling and analysing for the console and the
visualizer, and builds DynAlloy traces from solutions (`dynalloy.trace`).

## Entry points

- `ar.uba.dc.rfm.dynalloy.Main`: the console (`DynAlloyConsole`, options
  `--input`, `--output`, `--unroll`, `--assertion`, `--strictUnroll`,
  `--removeQuantifiers`, `--runAlloyAnalyzer`, `--removeExitWhileGuard`), or
  the visualizer with the argument `viz`. The jar has no `Main-Class`, so it is
  started with `java -cp dist/dynalloy4.jar ar.uba.dc.rfm.dynalloy.Main`. It
  does not work on this branch (see [KNOWN-LIMITATIONS.md](KNOWN-LIMITATIONS.md)).
- `dynalloy.dataflow.MainBounds` and `MainPointsTo`: the dataflow analyses.
- The library API above, which is how TACO uses DynAlloy.
