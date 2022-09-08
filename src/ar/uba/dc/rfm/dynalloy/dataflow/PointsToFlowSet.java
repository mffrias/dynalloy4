package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Not the nicest representation, but for points-to, our flowset has only one element,
 * which is the points-to graph. 
 */
public class PointsToFlowSet extends FlowSet<PointsToGraph> {
	
	private Map<String, Integer> highestVarIncarnationUpdated;
	private Map<String, Integer> highestFieldIncarnationUpdated;
	
	public PointsToFlowSet() {
		super();
		highestVarIncarnationUpdated = new LinkedHashMap<String, Integer>();
		highestFieldIncarnationUpdated = new LinkedHashMap<String, Integer>();
		add(new PointsToGraph());
	}
	
	public Map<String, Integer> getHighestVarIncarnationUpdatedTable() {
		return highestVarIncarnationUpdated;
	}
	
	public Map<String, Integer> getHighestFieldIncarnationUpdatedTable() {
		return highestFieldIncarnationUpdated;
	}
	
	public void varIncarnationUpdated(String variable, int incarnation) {
		if (highestVarIncarnationUpdated.containsKey(variable)) {
			if (highestVarIncarnationUpdated.get(variable) >= incarnation) {
				return;
			}
		}
		
		highestVarIncarnationUpdated.put(variable, incarnation);
	}
	
	public void fieldIncarnationUpdated(String field, int incarnation) {
		if (highestFieldIncarnationUpdated.containsKey(field)) {
			if (highestFieldIncarnationUpdated.get(field) >= incarnation) {
				return;
			}
		}
		
		highestFieldIncarnationUpdated.put(field, incarnation);
	}

	@Override
	public void copy(FlowSet<PointsToGraph> dest) {
		super.copy(dest);
		
		((PointsToFlowSet)dest).highestVarIncarnationUpdated.clear();
		((PointsToFlowSet)dest).highestFieldIncarnationUpdated.clear();
		((PointsToFlowSet)dest).highestVarIncarnationUpdated.putAll(this.highestVarIncarnationUpdated);
		((PointsToFlowSet)dest).highestFieldIncarnationUpdated.putAll(this.highestFieldIncarnationUpdated);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((highestFieldIncarnationUpdated == null) ? 0
						: highestFieldIncarnationUpdated.hashCode());
		result = prime
				* result
				+ ((highestVarIncarnationUpdated == null) ? 0
						: highestVarIncarnationUpdated.hashCode());
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
		PointsToFlowSet other = (PointsToFlowSet) obj;
		if (highestFieldIncarnationUpdated == null) {
			if (other.highestFieldIncarnationUpdated != null)
				return false;
		} else if (!highestFieldIncarnationUpdated
				.equals(other.highestFieldIncarnationUpdated))
			return false;
		if (highestVarIncarnationUpdated == null) {
			if (other.highestVarIncarnationUpdated != null)
				return false;
		} else if (!highestVarIncarnationUpdated
				.equals(other.highestVarIncarnationUpdated))
			return false;
		return true;
	}
	
}
