package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.LinkedHashSet;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public abstract class BoundedVariableFormatter {
	
	protected SpecContext context;
	protected ProgramDeclaration program;
	
	public BoundedVariableFormatter(SpecContext context, ProgramDeclaration program) {
		this.context = context;
		this.program = program;
	}
	
	public abstract Set<BoundedVariable> format(Set<BoundedVariable> bounds);
	
	public abstract void output(Set<BoundedVariable> bounds);
	
	protected Set<BoundedVariable> prefixNonParameters(Set<BoundedVariable> bounds){
		//We need to prefix anything that is not a parameter
		if (context.getLocalVarIndex() != 0) {
			Set<BoundedVariable> finalBounds = new LinkedHashSet<BoundedVariable>();
			String prefix = String.format("l%d_", context.getLocalVarIndex() - 1);
			for (BoundedVariable bv : bounds) {
				VariableId varId = bv.getVariable().getVariable().getVariableId();
				if (program.getParameters().contains(varId)) {
					finalBounds.add(bv);
				}
				else {
					String prefixedVarId = prefix + varId.getString();
					int prefixedVarIndex = bv.getVariable().getVariable().getIndex();
					ExprVariable prefixedVar = new ExprVariable(new AlloyVariable(prefixedVarId, prefixedVarIndex));
					finalBounds.add(new BoundedVariable(prefixedVar, bv.getBounds()));
				}
			}
			return finalBounds;
		}
		else
			return bounds;
	}
}
