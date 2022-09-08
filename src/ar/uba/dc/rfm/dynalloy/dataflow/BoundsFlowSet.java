package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public class BoundsFlowSet extends FlowSet<BoundedVariable> {
	private Map<String, Integer> highestIncarnationUpdated;
	//We use this instead of the innerSet of FlowSet to have constant instead of linear complexity
	private Map<ExprVariable, Set<Bound>> boundedVariables;
	
	public BoundsFlowSet() {
		super();
		highestIncarnationUpdated = new LinkedHashMap<String, Integer>();
		boundedVariables = new LinkedHashMap<ExprVariable, Set<Bound>>();
	}
	
	public Map<String, Integer> getHighestIncarnationUpdatedTable() {
		return highestIncarnationUpdated;
	}
	
	public void incarnationUpdated(String variable, int incarnation) {
		if (highestIncarnationUpdated.containsKey(variable)) {
			if (highestIncarnationUpdated.get(variable) >= incarnation) {
				return;
			}
		}
		
		highestIncarnationUpdated.put(variable, incarnation);
	}
	
	public boolean isVariableBounded(ExprVariable var) {
		return boundedVariables.containsKey(var);
	}
	
	public BoundedVariable getBounds(ExprVariable var) {
		if (!isVariableBounded(var))
			throw new UnsupportedOperationException(String.format("Bounds not found for variable '%s'", var));
		
		return new BoundedVariable(var, boundedVariables.get(var));
	}
	
	@Override
	public void copy(FlowSet<BoundedVariable> dest) {
		super.copy(dest);
		((BoundsFlowSet)dest).boundedVariables.clear();
		((BoundsFlowSet)dest).boundedVariables.putAll(this.boundedVariables);
		((BoundsFlowSet)dest).highestIncarnationUpdated.clear();
		((BoundsFlowSet)dest).highestIncarnationUpdated.putAll(this.highestIncarnationUpdated);
	}

	@Override
	public void add(BoundedVariable bv) {
		boundedVariables.put(bv.getVariable(), bv.getBounds());
	}

	@Override
	public void remove(BoundedVariable bv) {
		boundedVariables.remove(bv.getVariable());
	}

	@Override
	public Iterator<BoundedVariable> iterator() {
		return new Iterator<BoundedVariable>() {
		      private Iterator<ExprVariable> it = boundedVariables.keySet().iterator(); 
		      
		      public boolean hasNext() {  
		        return it.hasNext();       
		      }
		      
		      public BoundedVariable next() {
		        ExprVariable next = it.next();
		        return new BoundedVariable(next, boundedVariables.get(next));
		      }
		      
		      public void remove() {
		        it.remove();
		      }
		    };
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((boundedVariables == null) ? 0 : boundedVariables.hashCode());
		result = prime
				* result
				+ ((highestIncarnationUpdated == null) ? 0
						: highestIncarnationUpdated.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundsFlowSet other = (BoundsFlowSet) obj;
		if (boundedVariables == null) {
			if (other.boundedVariables != null)
				return false;
		} else if (!boundedVariables.equals(other.boundedVariables))
			return false;
		if (highestIncarnationUpdated == null) {
			if (other.highestIncarnationUpdated != null)
				return false;
		} else if (!highestIncarnationUpdated
				.equals(other.highestIncarnationUpdated))
			return false;
		return true;
	}

	
}
