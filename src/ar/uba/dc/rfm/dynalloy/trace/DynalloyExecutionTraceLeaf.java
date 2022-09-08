package ar.uba.dc.rfm.dynalloy.trace;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.visualization.utils.StringUtils;

public class DynalloyExecutionTraceLeaf extends DynalloyExecutionTrace {

	private static List<DynalloyExecutionTrace> EMPTY_LIST = new ArrayList<DynalloyExecutionTrace>();

	public DynalloyExecutionTraceLeaf(DynalloyProgram program) {
		super(program);
	}

	@Override
	public List<DynalloyExecutionTrace> getChildren() {
		return EMPTY_LIST;
	}

	@Override
	public void addChild(DynalloyExecutionTrace trace) {
		// Intentionally blank
	}

	@Override
	public void toStringAppend(StringBuilder sb, int level) {
		String tabs = StringUtils.indent(level);

		sb.append(tabs).append(getProgram()).append("\n");
	}

	@Override
	public void addParameterMap(AlloyExpression callerExpression,
			AlloyExpression caleeExpression) {
		// Intentionally empty
	}

	@Override
	public AlloyExpression getParameterMap(AlloyExpression calleeExpression) {
		return calleeExpression;
	}

}
