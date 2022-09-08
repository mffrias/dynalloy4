package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.*;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public class BoundedVariable {
	
	private final ExprVariable variable;
	private final Set<Bound> bounds;
	
	public static AlloyVariable createAlloyVar(String varId) {
		int underscoreLastIndex = varId.lastIndexOf('_');
		if (underscoreLastIndex == -1)
			return new AlloyVariable(varId);
		else {
			String id = varId.substring(0, underscoreLastIndex);
			String suffix = (varId.substring(varId.lastIndexOf('_') + 1, varId.length()));
			try {
				int index = Integer.parseInt(suffix);
				return new AlloyVariable(id, index);
			}
			catch(NumberFormatException ex) {
				return new AlloyVariable(varId);
			}			
		}
	}
	
	public BoundedVariable(String varId) {
		this(new ExprVariable(createAlloyVar(varId)));
	}
	
	public BoundedVariable(ExprVariable var) {
		this(var, new LinkedHashSet<Bound>());
	}
	
	public BoundedVariable(ExprVariable var, Set<Bound> initialBounds) {
		variable = var;
		bounds = initialBounds;
	}
	
	public ExprVariable getVariable() {
		return variable;
	}
	
	public Set<Bound> getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Bound bound : bounds) {
			builder.append(String.format("%s,\n", bound));
		}
		
		//Remove the trailing ",\n"
		if (!bounds.isEmpty())
			builder.delete(builder.length() - 2, builder.length());
		
		return String.format("%s IN {\n%s\n}", variable, builder.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundedVariable other = (BoundedVariable) obj;
		if (bounds == null) {
			if (other.bounds != null)
				return false;
		} else if (!bounds.equals(other.bounds))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

}
