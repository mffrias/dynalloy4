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

public class DynAlloyTranslator {

	private SpecContext specContext;

	private boolean translatingForStryker = false;
	
	public DynAlloyTranslator(boolean forStryker){
		this.translatingForStryker = forStryker;
	}

	
	public SpecContext getSpecContext() {
		return specContext;
	}

	public void setSpecContext(SpecContext specContext) {
		this.specContext = specContext;
	}

	private DynalloyModule unrollDynAlloyAST(DynalloyModule spec, int loopUnroll, boolean addSkip, DynAlloyAlloyMapping mapping, boolean removeExitWhileGuard) {
		DynalloyMutator closureRemover = new DynalloyMutator(new ClosureRemover(loopUnroll, addSkip, removeExitWhileGuard), mapping);
		DynalloyModule noClosureSpec = (DynalloyModule) spec.accept(closureRemover);
		return noClosureSpec;
	}

	private AlloyModule translateUnrolledDynAlloyAST(DynalloyModule dynalloyAST, DynAlloyOptions options,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule) throws RecognitionException, TokenStreamException,
			IOException, AssertionNotFound {

		DynalloyXlatorVisitor visitor = new DynalloyXlatorVisitor(this.specContext, options, 
				varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
				predsComingFromArithmeticConstraintsInContractsByProgram,
				varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule, translatingForStryker);
		AlloyModule alloyAST = (AlloyModule) dynalloyAST.accept(visitor);
		return alloyAST;
	}

	public AlloyModule translateDynAlloyAST(DynalloyModule dynalloyModuleAST, DynAlloyOptions options,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule) throws RecognitionException, TokenStreamException,
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
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule);
		return alloyAST;
	}

}
