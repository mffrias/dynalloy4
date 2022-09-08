package ar.uba.dc.rfm.dynalloy.visualization.gui;

import java.util.EventObject;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.trace.DynalloyExecutionTrace;

public class TraceSelectionEvent extends EventObject {

	private static final long serialVersionUID = -4028784846138350755L;

	private DynalloyExecutionTrace traceNode;
	
	private AlloyFormula formula;
	
	public TraceSelectionEvent(Object source) {
		super(source);
	}

	public void setTraceNode(DynalloyExecutionTrace traceNode) {
		this.traceNode = traceNode;
	}

	public DynalloyExecutionTrace getTraceNode() {
		return traceNode;
	}
	
	public void setAlloyFormula(AlloyFormula formula) {
		this.traceNode = null;
		this.formula = formula;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

}
