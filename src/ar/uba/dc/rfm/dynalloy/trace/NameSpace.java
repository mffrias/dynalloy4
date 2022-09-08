package ar.uba.dc.rfm.dynalloy.trace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;

public class NameSpace {

	private final List<String> names = new LinkedList<String>();

	private final Map<String, Map<Integer, String>> incarnations = new HashMap<String, Map<Integer, String>>();

	public NameSpace(AssertionDeclaration assertionDeclaration) {
		AlloyTyping assertionTyping = assertionDeclaration.getTyping();
		for (AlloyVariable v : assertionTyping) {
			names.add(v.toString());
			incarnations.put(v.toString(), new HashMap<Integer, String>());
		}
	}

	public NameSpace(ProgramDeclaration programDeclaration) {
		AlloyTyping programTyping = programDeclaration.getParameterTyping();
		for (AlloyVariable v : programTyping) {
			names.add(v.toString());
			incarnations.put(v.toString(), new HashMap<Integer, String>());
		}
	}

	public List<String> varNames() {
		return names;
	}

	public boolean containsName(String name) {
		return names.contains(name);
	}

	public void addIncarnation(String name, Integer instant,
			String incarnationStr) {

		incarnations.get(name).put(instant, incarnationStr);

	}

	public String getIncarnation(String name, int instant) {
		return incarnations.get(name).get(instant);
	}

}
