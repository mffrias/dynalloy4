package ar.uba.dc.rfm.dynalloy.trace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;
import ar.uba.dc.rfm.alloy.util.PrefixedExpressionPrinter;
import ar.uba.dc.rfm.dynalloy.analyzer.AlloyAnalysisResult;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class DynAlloySolution {

	private final DynalloyExecutionTrace executionTrace;

	private final AlloyFormula precondition;

	private final AlloyFormula postcondition;

	private AlloyEvaluator evaluator;

	private final DynAlloyAlloyMapping mapping;

	private static List<TraceStep> buildTrace(
			DynalloyExecutionTrace executionTrace,
			DynAlloySolution dynalloySolution) {

		List<TraceStep> trace = new LinkedList<TraceStep>();
		for (DynalloyExecutionTrace child : executionTrace.getChildren()) {
			TraceStep traceElement = new TraceStep(dynalloySolution, child);
			trace.add(traceElement);

			if (child.getProgram() instanceof InvokeProgram) {

				List<TraceStep> childTrace = buildTrace(child, dynalloySolution);
				traceElement.setTrace(childTrace);
			}
		}
		return trace;
	}

	public static DynAlloySolution buildAssertionCounterExample(
			AlloyFormula precondition, DynalloyExecutionTrace trace,
			AlloyFormula postcondition, AlloyEvaluator evaluator,
			DynAlloyAlloyMapping mapping) {
		return new DynAlloySolution(precondition, trace, postcondition,
				evaluator, mapping);
	}

	public static DynAlloySolution buildProgramInstance(
			DynalloyExecutionTrace trace, AlloyEvaluator evaluator,
			DynAlloyAlloyMapping mapping) {
		return new DynAlloySolution(null, trace, null, evaluator, mapping);
	}

	public static DynAlloySolution buildUNSAT(AlloyAnalysisResult result) {
		DynAlloySolution dynAlloySolution = new DynAlloySolution(null, DynalloyExecutionTraceNull
				.nullInstance(), null, null, null);
		dynAlloySolution.setAnalysisResult(result);
		return dynAlloySolution;
	}

	public boolean isUNSAT() {
		return this.executionTrace.equals(DynalloyExecutionTraceNull
				.nullInstance());
	}

	public boolean isAssertionCounterexample() {
		return !isUNSAT() && this.postcondition != null;
	}

	public boolean isRunInstance() {
		return !isUNSAT() && !isAssertionCounterexample();
	}

	public boolean isSAT() {
		return !this.isUNSAT();
	}

	private DynAlloySolution(AlloyFormula precondition,
			DynalloyExecutionTrace trace, AlloyFormula postcondition,
			AlloyEvaluator evaluator, DynAlloyAlloyMapping mapping) {
		super();
		this.precondition = precondition;
		this.executionTrace = trace;
		this.postcondition = postcondition;
		this.evaluator = evaluator;
		this.mapping = mapping;
	}

	public DynalloyExecutionTrace getExecutionTrace() {
		return this.executionTrace;
	}

	public AlloyFormula getPrecondition() {
		return this.precondition;
	}

	public AlloyFormula getPostcondition() {
		return this.postcondition;
	}

	public Object evaluate(AlloyExpression e,
			DynalloyExecutionTrace pointOfEvaluation) {

		Object evaluation;

		IncarnationMutator incarnator = new IncarnationMutator(
				pointOfEvaluation);

		AlloyExpression incarnationExpr = (AlloyExpression) e
				.accept(incarnator);

		if (incarnator.undef())
			evaluation = "<<Undef>>";
		else {

			ExpressionPrinter printer = new ExpressionPrinter();
			String str = (String) incarnationExpr.accept(printer);

			if (str != null)
				evaluation = evaluator.evaluate(str);
			else
				evaluation = "<<Uninitialize>>";
		}
		return evaluation;

	}

	public String getAlloyTranslation(AlloyExpression e,
			DynalloyExecutionTrace pointOfEvaluation) {

		IncarnationMutator incarnator = new IncarnationMutator(
				pointOfEvaluation);

		AlloyExpression incarnationExpr = (AlloyExpression) e
				.accept(incarnator);

		String str = null;
		if (!incarnator.undef()) {
			ExpressionPrinter printer = new ExpressionPrinter();
			str = (String) incarnationExpr.accept(printer);
			return str;
		} else
			return null;

	}

	private String xmlFilename;

	public void setXmlFilename(String xmlFilename) {
		this.xmlFilename = xmlFilename;
	}

	public String getXmlFilename() {
		return this.xmlFilename;
	}

	public AlloyEvaluator getEvaluator() {
		return this.evaluator;

	}

	private String alsFilename;

	public String getAlsFilename() {
		return alsFilename;
	}

	public void setAlsFilename(String alsFilename) {
		this.alsFilename = alsFilename;
	}

	private List<TraceStep> trace = null;

	private AlloyAnalysisResult analysisResult;

	public List<TraceStep> getTrace() {

		if (trace == null) {
			this.trace = buildTrace(this.getExecutionTrace(), this);
		}
		return trace;
	}

	public AlloyAnalysisResult getAlloyAnalysisResult() {
		return this.analysisResult;
	}

	public void setAnalysisResult(AlloyAnalysisResult analysisResult) {
		this.analysisResult = analysisResult;
	}

}
