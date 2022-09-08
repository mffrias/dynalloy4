package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.LinkedHashSet;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class AlloyBoundedVariableFormatter extends BoundedVariableFormatter {

	private DefaultBoundsCalculator defaultBounds;
	
	public AlloyBoundedVariableFormatter(SpecContext context, ProgramDeclaration program, DefaultBoundsCalculator defaultBounds) {
		super(context, program);
		this.defaultBounds = defaultBounds;
	}
	
	@Override
	public Set<BoundedVariable> format(Set<BoundedVariable> bounds) {
		//First, we need to remove any bounds that give no relevant information
		Set<BoundedVariable> relevantBounds = defaultBounds.removeIrrelevantBounds(bounds);
		
		//Then, we need to prefix anything that is not a parameter
		return prefixNonParameters(relevantBounds);
	}
	
	@Override
	public void output(Set<BoundedVariable> bounds) {
		StringBuilder builder = new StringBuilder();
		builder.append("//Dataflow analysis bounds:\n\n");
		
		for (BoundedVariable var : bounds) {
			builder.append(toAlloyFormat(var));
			builder.append("\n\n");
		}
		
		System.out.println(builder.toString());
	}
	
	private String toAlloyFormat(BoundedVariable bv) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("fact {\n");
		builder.append(String.format("(QF.%s) in ", bv.getVariable()));
		boolean isFirst = true;
		for (Bound bound : bv.getBounds()) {
			if (isFirst) {
				builder.append(bound.toString());
				isFirst = false;
			}
			else
				builder.append(String.format("\n+%s", bound));
		}
		builder.append("\n}");

		return builder.toString();
	}
}
