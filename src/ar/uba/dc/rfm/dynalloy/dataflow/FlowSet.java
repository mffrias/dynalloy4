package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.*;

public class FlowSet<T> implements Iterable<T> {
	private Set<T> innerSet;
	
	public FlowSet() {
		innerSet = new LinkedHashSet<T>();
	}
	
	public void copy(FlowSet<T> dest) {
		dest.innerSet.clear();
		dest.innerSet.addAll(innerSet);
	}
	
	public void add(T o) {
		innerSet.add(o);
	}
	
	public void remove(T o) {
		innerSet.remove(o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((innerSet == null) ? 0 : innerSet.hashCode());
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
		FlowSet other = (FlowSet) obj;
		if (innerSet == null) {
			if (other.innerSet != null)
				return false;
		} else if (!innerSet.equals(other.innerSet))
			return false;
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return innerSet.iterator();
	}
}
