package ar.uba.dc.rfm.dynalloy.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.visualization.utils.StringUtils;

public class DynalloyExecutionTraceNode extends DynalloyExecutionTrace {

	private List<DynalloyExecutionTrace> children = new ArrayList<DynalloyExecutionTrace>();
	private Map<AlloyExpression, AlloyExpression> parametersMap = new HashMap<AlloyExpression, AlloyExpression>();
	private List<AlloyExpression> variables = new ArrayList<AlloyExpression>();

	public DynalloyExecutionTraceNode() {
		super();
	}

	public DynalloyExecutionTraceNode(DynalloyProgram program) {
		super(program);
	}

	public static DynalloyExecutionTraceNode createRootNode() {
		DynalloyExecutionTraceNode node = new DynalloyExecutionTraceNode();
		node.setValue(true);
		node.setVisible(true);

		return node;
	}

	@Override
	public void addChild(DynalloyExecutionTrace trace) {
		children.add(trace);
		trace.setParent(this);
	}

	@Override
	public List<DynalloyExecutionTrace> getChildren() {
		return children;
	}

	@Override
	public void toStringAppend(StringBuilder sb, int level) {
		String tabs = StringUtils.indent(level);
		sb.append(tabs).append(getProgram()).append("\n");
		for (DynalloyExecutionTrace trace : children) {
			trace.toStringAppend(sb, level + 1);
		}
	}

	@Override
	public void addParameterMap(AlloyExpression callerExpression,
			AlloyExpression calleeExpression) {
		parametersMap.put(calleeExpression, callerExpression);
	}

	@Override
	public AlloyExpression getParameterMap(AlloyExpression calleeExpression) {

		AlloyExpression callerExpression;
		if (parametersMap.containsKey(calleeExpression)) {
			callerExpression = parametersMap.get(calleeExpression);
		} else {
			callerExpression = calleeExpression;
		}

		return callerExpression;
	}

	@Override
	public void addVariable(AlloyExpression variable) {
		this.variables.add(variable);
	}

	@Override
	public List<AlloyExpression> getVariables() {
		return this.variables;
	}
}
