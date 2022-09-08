package ar.uba.dc.rfm.dynalloy.trace;

import java.io.File;
import java.io.IOException;
import java.util.ListIterator;

import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.DynAlloyCompiler;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.analyzer.AlloyAnalysisResult;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerException;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;

public final class DynAlloySolutionBuilder {

	private SpecContext context;
	private Module world = null;
	private DynAlloyOptions options;
	private String xmlFilename;
	private String alsFilename;
	private Command command;

	public DynAlloySolutionBuilder(SpecContext context, Module alloyWorld, Command alloyCommand) {
		this.context = context;
		this.world = alloyWorld;
		this.command = alloyCommand;
	}

	public void setWorld(Module module) {
		world = module;
	}

	// END ADDITION

	public DynAlloySolution buildSolution(AlloyAnalysisResult analysisResult, boolean options_skolemize) throws Err, IOException {

		if (analysisResult.isSAT()) {
			DynAlloySolution satSolution = buildSatisfiableSolution(analysisResult, options_skolemize);
			return satSolution;
		} else {
			return DynAlloySolution.buildUNSAT(analysisResult);
		}

	}

	private DynAlloySolution buildSatisfiableSolution(AlloyAnalysisResult analysisResult, boolean options_skolemize) throws Err, IOException {

		DynAlloyAlloyMapping mapping = context.getMapping();
		A4Solution ans = analysisResult.getAlloy_solution();

		if (false) {
			ans.writeXML(xmlFilename);
			File solutionXml = new File(xmlFilename);

			ans = A4SolutionReader.read(world.getAllReachableSigs(), new XMLNode(solutionXml));

		}

		// TODO: Ver como hacer esto para el caso en que sea un run y no un
		// check
		AlloyFormula programFormula;
		NameSpace rootNameSpace;
		String variablePrefix;
		if (command.check) {
			AlloyAssertion assertion = mapping.getAssertion(command.label);
			programFormula = getTranslationOfProgram(assertion.getFormula());

			AssertionDeclaration assertionDeclaration = mapping.getDynAlloy(assertion);
			rootNameSpace = new NameSpace(assertionDeclaration);

			if (options_skolemize) {
				variablePrefix = String.format("QF.");
			} else {
				String assertionId = command.label;
				variablePrefix = String.format("$%s_", assertionId);
			}
		} else {
			String programId = command.label;

			AlloyFormula formula = mapping.getRun(programId);

			ProgramDeclaration programDecl = context.getProgram(null, programId);

			rootNameSpace = new NameSpace(programDecl);
			programFormula = mapping.getRun(command.label);

			variablePrefix = String.format("$%s_", programId);
		}

		AlloyEvaluator alloyEvaluator = new AlloyEvaluator(world, ans);

		alloyEvaluator.setVariablePrefix(variablePrefix);

		EvaluationVisitor visVisitor = new EvaluationVisitor(context, alloyEvaluator, true);

		visVisitor.setRootNameSpace(rootNameSpace);

		PredicateFormula precondition;
		PredicateFormula postcondition;
		if (command.check) {

			AlloyAssertion alloyAssertion = mapping.getAssertion(command.label);

			AssertionDeclaration assertionDeclaration = mapping.getDynAlloy(alloyAssertion);

			precondition = assertionDeclaration.getPre();

			postcondition = assertionDeclaration.getPost();

			AlloyFormula alloyPrecondition = getTranslationOfPrecondtion(alloyAssertion.getFormula());

			AlloyFormula alloyPostcondition = getTranslationOfPostcondition(alloyAssertion.getFormula());

			TestPredicate assumePrecondition = new TestPredicate((PredicateFormula) precondition, precondition.getPosition());

			mapping.addMapping(assumePrecondition, alloyPrecondition);

			TestPredicate assumeNotPostcondition = new TestPredicate((PredicateFormula) postcondition, false, postcondition.getPosition());

			mapping.addMapping(assumeNotPostcondition, alloyPostcondition);

			visVisitor.visitAlloyAssertion(alloyPrecondition, programFormula, alloyPostcondition);

		} else {
			precondition = null;
			postcondition = null;
			programFormula.accept(visVisitor);
		}

		DynalloyExecutionTrace rootExecutionTrace = visVisitor.getRootExecutionTrace();

		DynalloyExecutionTrace cleanUpTrace = cleanUpTrace(rootExecutionTrace);

		DynAlloySolution dynalloySolution;
		if (command.check) {

			attachInstants(cleanUpTrace);

			dynalloySolution = DynAlloySolution
					.buildAssertionCounterExample(precondition, cleanUpTrace, postcondition, alloyEvaluator, visVisitor.getMapping());

		} else {
			attachInstants(cleanUpTrace);

			dynalloySolution = DynAlloySolution.buildProgramInstance(cleanUpTrace, alloyEvaluator, visVisitor.getMapping());
		}

		dynalloySolution.setXmlFilename(xmlFilename);
		dynalloySolution.setAlsFilename(alsFilename);

		dynalloySolution.setAnalysisResult(analysisResult);

		return dynalloySolution;

	}

	private static void attachInstants(DynalloyExecutionTrace rootStep) {
		if (rootStep.getChildren().isEmpty())
			return;

		NameSpace nameSpace = rootStep.getChildren().get(0).getNameSpace();

		for (String varName : nameSpace.varNames()) {

			int minimum_index = 0;

			for (DynalloyExecutionTrace step : rootStep.getChildren()) {

				if (step.usesName(varName)) {
					int firstUseIndex = step.firstUseOf(varName);

					if (firstUseIndex < minimum_index) {
						minimum_index = firstUseIndex;
					}
				}
			}

			// I have the minimum index

			int current_index = minimum_index;

			for (DynalloyExecutionTrace step : rootStep.getChildren()) {

				step.setEntryInstant(varName, current_index);

				if (step.usesName(varName)) {
					int lastUseIndex = step.lastUseOf(varName);

					if (current_index < lastUseIndex) {
						current_index = lastUseIndex;
					}
				}
			}
		}

		for (DynalloyExecutionTrace step : rootStep.getChildren()) {
			if (step.getProgram() instanceof InvokeProgram) {
				attachInstants(step);

			}
		}
	}

	/**
	 * Returns an AlloyFormula that represents the program to run by the assert,
	 * given the main AlloyFormula. Formulas are composed as
	 * <code>(pre and program) implies post</code> This method decomposes the
	 * formula and returns program.
	 * 
	 * @param f
	 *            Original formula, with precondition, program and post
	 *            condition.
	 * @return AlloyFormula representing the program to run
	 * @throws DynalloyVisualizerExeption
	 *             if the formula doesn't match the
	 *             <code>(pre and program) implies post</code> pattern.
	 */
	private AlloyFormula getTranslationOfProgram(AlloyFormula f) {
		try {
			ImpliesFormula implies = (ImpliesFormula) f;
			AndFormula and = (AndFormula) implies.getLeft();
			return and.getRight();
		} catch (ClassCastException e) {
			throw new DynalloyVisualizerException("Invalid program formula. Expected (pre and prog) implies post.", e);
		}
	}

	private AlloyFormula getTranslationOfPrecondtion(AlloyFormula f) {
		try {
			ImpliesFormula implies = (ImpliesFormula) f;
			AndFormula and = (AndFormula) implies.getLeft();
			return and.getLeft();
		} catch (ClassCastException e) {
			throw new DynalloyVisualizerException("Invalid program formula. Expected (pre and prog) implies post.", e);
		}
	}

	private AlloyFormula getTranslationOfPostcondition(AlloyFormula f) {
		try {
			ImpliesFormula implies = (ImpliesFormula) f;
			return implies.getRight();
		} catch (ClassCastException e) {
			throw new DynalloyVisualizerException("Invalid program formula. Expected (pre and prog) implies post.", e);
		}
	}

	private DynalloyExecutionTrace cleanUpTrace(DynalloyExecutionTrace trace) {
		ListIterator<DynalloyExecutionTrace> iterator = trace.getChildren().listIterator();
		while (iterator.hasNext()) {
			DynalloyExecutionTrace next = iterator.next();
			if (!next.isValue()) {
				iterator.remove();
			} else {
				cleanUpTrace(next);
				if (!next.isVisible()) {
					iterator.remove();
					for (DynalloyExecutionTrace t : next.getChildren()) {
						iterator.add(t);
					}
				}
			}
		}
		return trace;
	}

}
