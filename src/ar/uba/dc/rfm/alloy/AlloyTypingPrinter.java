package ar.uba.dc.rfm.alloy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AlloyTypingPrinter {

	public String print(AlloyTyping t) {
		return print(t, toOrderedList(t.varSet()));
	}

	public String print(AlloyTyping t, List<AlloyVariable> order) {
		StringBuffer buff = new StringBuffer();
		for (AlloyVariable var : order) {
			if (buff.length() > 0)
				buff.append(",");
	
			if (t.get(var) == null)
				throw new IllegalArgumentException();
	
			buff.append(var.toString() + ":" + t.get(var));
		}
		return buff.toString();
	}	
	private List<AlloyVariable> toOrderedList(Set<AlloyVariable> vs) {
		List<AlloyVariable> r = new LinkedList<AlloyVariable>(vs);
		Collections.sort(r, new AlloyVariableComparator());
		return r;
	}

}
