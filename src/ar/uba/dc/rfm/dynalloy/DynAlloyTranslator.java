package ar.uba.dc.rfm.dynalloy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.da2a.prepare.ClosureRemover;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover.QuantifierRemoverResult;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;
import ar.uba.dc.rfm.dynalloy.util.DynalloyMutator;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;
import ar.uba.dc.rfm.dynalloy.xlator.DynalloyXlatorVisitor;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

/**
 * Translates a parsed DynAlloy module into an Alloy module: the step of
 * {@link DynAlloyCompiler#compile} between the DynAlloy-AST plugins and the Alloy-AST plugins.
 * {@link #translateDynAlloyAST} unrolls loops and iterations
 * ({@code da2a.prepare.ClosureRemover}), builds a new {@code SpecContext} for the unrolled module
 * and translates that module with a {@code DynalloyXlatorVisitor}. Besides the compiler,
 * {@code dynalloy.dataflow.MainCommon} and some tests use it directly. See docs/ARCHITECTURE.md
 * ("Translation").
 *
 * <p>Static state: {@link #isCheckAndAfterRunSpec}, which {@code DynAlloyCompiler.compile} sets at
 * every compilation and which keeps its value between translations in one JVM; a caller that uses
 * this class directly gets the value of the last compilation. The {@code SpecContext} belongs to
 * the instance; {@code translateDynAlloyAST} replaces the one given to {@code setSpecContext}.
 */
public class DynAlloyTranslator {

	private SpecContext specContext;

	private boolean translatingForStryker = false;
	
	/**
	 * Creates a translator.
	 *
	 * @param forStryker true to negate the post of the assertion, for Stryker's feedback checks
	 *        ({@code (pre and program) => not post}); passed to {@code DynalloyXlatorVisitor}
	 */
	public DynAlloyTranslator(boolean forStryker){
		this.translatingForStryker = forStryker;
	}

	
	/**
	 * Returns the {@code SpecContext} of the last {@link #translateDynAlloyAST}: the unrolled
	 * module, the fields of its signatures and the mapping between DynAlloy programs and their
	 * Alloy formulas. {@code DynAlloyCompiler.compile} keeps it, and
	 * {@code dynalloy.dataflow.MainCommon} reads it.
	 *
	 * @return the context; before any translation, the one given to {@code setSpecContext}, or
	 *         null
	 */
	public SpecContext getSpecContext() {
		return specContext;
	}

	public void setSpecContext(SpecContext specContext) {
		this.specContext = specContext;
	}

	/**
	 * Static: the specified-output flag, which {@link #translateDynAlloyAST} copies into the flag
	 * of the same name of {@code DynalloyXlatorVisitor}. {@code DynAlloyCompiler.compile} sets it
	 * from {@link DynAlloyCompiler#isCheckAndAfterRunSpec}; see there.
	 */
	public static boolean isCheckAndAfterRunSpec = false;

	private DynalloyModule unrollDynAlloyAST(DynalloyModule spec, int loopUnroll, boolean addSkip, DynAlloyAlloyMapping mapping, boolean removeExitWhileGuard) {
		DynalloyMutator closureRemover = new DynalloyMutator(new ClosureRemover(loopUnroll, addSkip, removeExitWhileGuard), mapping);
		DynalloyModule noClosureSpec = (DynalloyModule) spec.accept(closureRemover);
		return noClosureSpec;
	}

	private AlloyModule translateUnrolledDynAlloyAST(DynalloyModule dynalloyAST, DynAlloyOptions options,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule, Object inputToFix) throws RecognitionException, TokenStreamException,
			IOException, AssertionNotFound {

		DynalloyXlatorVisitor.isCheckAndAfterRunSpec = isCheckAndAfterRunSpec;

		DynalloyXlatorVisitor visitor = new DynalloyXlatorVisitor(this.specContext, options, 
				varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
				predsComingFromArithmeticConstraintsInContractsByProgram,
				varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule, translatingForStryker, inputToFix);


		AlloyModule alloyAST = (AlloyModule) dynalloyAST.accept(visitor);
		return alloyAST;
	}

	/**
	 * Translates a DynAlloy module into an Alloy module. Unrolls its loops and iterations
	 * {@code options.getLoopUnroll()} times (each unrolled iteration may be skipped unless
	 * {@code options.getStrictUnrolling()}; with {@code options.getRemoveExitWhileGuard()}, an
	 * unrolled {@code while} does not end with the test of its negated condition), recording in a
	 * new {@code DynAlloyAlloyMapping} what each construct became; builds a new
	 * {@code SpecContext} from the unrolled module, with the fields of {@code dynalloyModuleAST}
	 * and that mapping; and translates the unrolled module with a {@code DynalloyXlatorVisitor},
	 * which adds to the mapping. See docs/ARCHITECTURE.md ("Translation").
	 *
	 * <p>Side effects: copies the static {@link #isCheckAndAfterRunSpec} into
	 * {@code DynalloyXlatorVisitor.isCheckAndAfterRunSpec}; replaces this translator's
	 * {@code SpecContext}. The checked exceptions it declares are not thrown.
	 *
	 * <p>Assumes the maps and options that TACO passes in (see {@link DynAlloyCompiler#compile}).
	 * The maps come in another order than in {@code compile}: those of the contracts first.
	 *
	 * @param dynalloyModuleAST the parsed module, after the DynAlloy-AST plugins
	 * @param options the options of the translation
	 * @param varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram per program id,
	 *        the variables that come from arithmetic constraints in its contract, with their types
	 * @param predsComingFromArithmeticConstraintsInContractsByProgram per program id, the
	 *        predicates on those variables
	 * @param varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule per module
	 *        id, the variables that come from arithmetic constraints in its object invariants,
	 *        with their types
	 * @param predsComingFromArithmeticConstraintsInObjectInvariantsByModule per module id, the
	 *        predicates on those variables
	 * @param inputToFix only tested for null (see {@link DynAlloyCompiler#compile})
	 * @return the Alloy module
	 */
	public AlloyModule translateDynAlloyAST(DynalloyModule dynalloyModuleAST, DynAlloyOptions options,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule, Object inputToFix) throws RecognitionException, TokenStreamException,
			IOException, AssertionNotFound {
		DynAlloyAlloyMapping mapping = new DynAlloyAlloyMapping();
		DynalloyModule unrolledAST = null;
		boolean addSkip = !options.getStrictUnrolling();
		unrolledAST = unrollDynAlloyAST(dynalloyModuleAST, options.getLoopUnroll(), addSkip, mapping, options.getRemoveExitWhileGuard());

		specContext = new SpecContext(unrolledAST);
		specContext.setFields(dynalloyModuleAST.getDynalloyFields());
		specContext.setMapping(mapping);

		AlloyModule alloyAST = translateUnrolledDynAlloyAST(unrolledAST, options, 
				varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
				predsComingFromArithmeticConstraintsInContractsByProgram,
				varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule, inputToFix);
		return alloyAST;
	}

}
