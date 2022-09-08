package ar.uba.dc.rfm.dynalloy.dataflow;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.*;

import java.util.*;

public class CfgNode {
	private final Set<CfgNode> successors;
	private final Set<CfgNode> predecessors;
	private final DynalloyProgram program;
	private final ProgramDeclaration parentProgramDeclaration;
	private final HashMap<ExprVariable, AlloyExpression> localToGlobalVarMapping;
	
	public CfgNode(DynalloyProgram program, ProgramDeclaration parentProgramDeclaration, HashMap<ExprVariable, AlloyExpression> localToGlobalVarMapping) {
		successors = new LinkedHashSet<CfgNode>();
		predecessors = new LinkedHashSet<CfgNode>();
		this.program = program;
		this.parentProgramDeclaration = parentProgramDeclaration;
		this.localToGlobalVarMapping = localToGlobalVarMapping;
	}
	
	public Set<CfgNode> getSuccessors() {
		return successors;
	}
	
	public Set<CfgNode> getPredecessors() {
		return predecessors;
	}
	
	public DynalloyProgram getProgram() {
		return program;
	}
	
	public ProgramDeclaration getParentProgramDeclaration() {
		return parentProgramDeclaration;
	}
	
	/**
	 * Since to perform data-flow analysis quantifier elimination is performed,
	 * all the variables are global. This HashMap gives a mapping between the local
	 * variables of the program this CfgNode statement belongs to, and their global counterpart
	 * (it's an expression because formal parameters can be substituted by an expression,
	 * if they're not used as an lvalue)
	 */
	public HashMap<ExprVariable, AlloyExpression> getLocalToGlobalVarMapping() {
		return localToGlobalVarMapping;
	}

	@Override
	public String toString() {
		return String.format("{%d}-> %s ->{%d}", predecessors.size(), program.toString(), successors.size());
	}
	
	
}
