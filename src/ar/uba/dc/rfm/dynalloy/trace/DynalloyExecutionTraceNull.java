package ar.uba.dc.rfm.dynalloy.trace;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public class DynalloyExecutionTraceNull extends DynalloyExecutionTrace {

	@Override
	public boolean equals(Object obj) {
		if ((obj != null)
				&& (obj.getClass().equals(DynalloyExecutionTraceNull.class))) {
			return true;
		} else
			return false;
	}

	public static DynalloyExecutionTrace nullInstance() {
		DynalloyExecutionTraceNull root = new DynalloyExecutionTraceNull();
		DynalloyExecutionTraceNull child = new DynalloyExecutionTraceNull();
		root.children.add(child);

		return root;
	}

	private DynalloyExecutionTraceNull() {
	}

	private List<DynalloyExecutionTrace> children = new ArrayList<DynalloyExecutionTrace>();

	@Override
	public void addChild(DynalloyExecutionTrace trace) {
		// Intentionally blank
	}

	@Override
	public void addParameterMap(AlloyExpression callerExpression, AlloyExpression calleeExpression) {
		// Intentionally blank
	}

	@Override
	public List<DynalloyExecutionTrace> getChildren() {
		return this.children;
	}

	@Override
	public AlloyExpression getParameterMap(AlloyExpression calleeExpression) {
		// Intentionally blank
		return null;
	}

	@Override
	public void toStringAppend(StringBuilder sb, int level) {
		// Intentionally blank
	}

	@Override
	public String toString() {
		return "No counterexample found";
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isValue() {
		return true;
	}
}
