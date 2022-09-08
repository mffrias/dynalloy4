package ar.uba.dc.rfm.dynalloy.trace;

import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

public class TraceStep {

	private List<TraceStep> trace;

	private DynAlloySolution dynalloySolution;

	private DynalloyExecutionTrace executionTrace;

	public TraceStep(DynAlloySolution dynalloySolution,
			DynalloyExecutionTrace executionTrace) {
		this.dynalloySolution = dynalloySolution;
		this.executionTrace = executionTrace;
	}

	public List<TraceStep> getTrace() {
		return this.trace;
	}

	public void setTrace(List<TraceStep> trace) {
		this.trace = trace;
	}

	public DynalloyProgram getProgram() {
		return this.executionTrace.getProgram();
	}

	public Object evaluate(AlloyExpression expr) {

		Object evalResult = this.dynalloySolution.evaluate(expr,
				this.executionTrace);
		return evalResult;
	}

}
