package ar.uba.dc.rfm.dynalloy.visualization;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

/**
 * Class representing the mapping between a DynAlloy AST and an Alloy AST.
 * This mapping will be used to visualize counterexamples found
 * by the Alloy SAT solver.
 * @author Pablo
 *
 */
public class DynAlloyAlloyMapping {

	public DynAlloyAlloyMapping() {
		super();
	}

	// Maps Dynalloy programs to Alloy formulas
	private IdentityHashMap<DynalloyProgram, AlloyFormula> dToAMapping = new IdentityHashMap<DynalloyProgram, AlloyFormula>();
	// Reverse map, from alloy formulas to dynalloy programs
	private IdentityHashMap<AlloyFormula, DynalloyProgram> aToDMapping = new IdentityHashMap<AlloyFormula, DynalloyProgram>();
	// Simplifications map. Maps dynalloy's original code to simplified dynalloy code (i.e. closure removals)
	private IdentityHashMap<DynalloyProgram, DynalloyProgram> dToSimdMapping = new IdentityHashMap<DynalloyProgram, DynalloyProgram>();
	private IdentityHashMap<DynalloyProgram, DynalloyProgram> simdToDMapping = new IdentityHashMap<DynalloyProgram, DynalloyProgram>();

	private IdentityHashMap<AssertionDeclaration, AlloyAssertion> assertions = new IdentityHashMap<AssertionDeclaration, AlloyAssertion>();
	private Map<String, AlloyFormula> runs = new HashMap<String, AlloyFormula>();

	/**
	 * Adds an AlloyAssertion
	 * @param dynalloyAssertion Assertion name (or id)
	 * @param assertion AlloyAssertion object
	 */
	public void addAssertion(AssertionDeclaration dynalloyAssertion,
			AlloyAssertion alloyAssertion) {
		this.assertions.put(dynalloyAssertion, alloyAssertion);
	}

	/**
	 * Retuns an assertion present on this mapping object.
	 * @param name Assertion name (or id)
	 * @return AlloyAssertion stored with the given name
	 */
	public AlloyAssertion getAssertion(String name) {
		for (AssertionDeclaration assertionDeclaration : assertions.keySet()) {
			if (assertionDeclaration.getAssertionId().equals(name))
				return assertions.get(assertionDeclaration);
		}
		return null;
	}

	/**
	 * Adds a run statement to the mapping
	 * @param name
	 * @param formula
	 */
	public void addRun(String name, AlloyFormula formula) {
		this.runs.put(name, formula);
	}

	/**
	 * Returns an AlloyFormula that has a run statement on the code.
	 * @param name Program name (or id)
	 * @return AlloyFormula that represents the program to run
	 */
	public AlloyFormula getRun(String name) {
		if (this.runs.containsKey(name)) {
			return this.runs.get(name);
		} else {
			return null;
		}
	}

	/**
	 * Adds a DynAlloy to Alloy mapping.
	 * @param dynAlloy DynAlloyProgram to map
	 * @param alloy AlloyFormula to map
	 */
	public void addMapping(DynalloyProgram dynAlloy, AlloyFormula alloy) {
		this.dToAMapping.put(dynAlloy, alloy);
		this.aToDMapping.put(alloy, dynAlloy);
	}

	/**
	 * Maps a dynallloy simplification.
	 * @param originalCode Original dynalloy code (i.e. using closures *)
	 * @param simplifiedCode Simplified dynalloy code (i.e. with loop unrolls)
	 */
	public void addSimplification(DynalloyProgram originalCode,
			DynalloyProgram simplifiedCode) {
		this.dToSimdMapping.put(originalCode, simplifiedCode);
		this.simdToDMapping.put(simplifiedCode, originalCode);
	}

	/**
	 * Returns a DynalloyProgram mapped to the AlloyFormula received in the parameter
	 * @param f AlloyFormula whose mapping we are looking for.
	 * @return DynalloyProgram that maps to the AlloyFormula f
	 */
	public DynalloyProgram getDynAlloy(AlloyFormula f) {
		if (this.aToDMapping.containsKey(f)) {
			DynalloyProgram dynalloyProgram = this.aToDMapping.get(f);
			if (this.simdToDMapping.containsKey(dynalloyProgram)) {
				return this.simdToDMapping.get(dynalloyProgram);
			} else {
				return dynalloyProgram;
			}
		} else {
			return null;
		}
	}

	public AssertionDeclaration getDynAlloy(AlloyAssertion alloyAssertion) {
		for (Entry<AssertionDeclaration, AlloyAssertion> entry : assertions
				.entrySet()) {

			if (entry.getValue() == alloyAssertion)
				return entry.getKey();
		}
		return null;
	}

	/**
	 * Returns an AlloyFormula that maps to the given DynalloyProgram p.
	 * @param p DynallloyProgram we are looking the mapping of.
	 * @return AlloyFormula that maps the DynalloyProgram p
	 */
	public AlloyFormula getAlloy(DynalloyProgram p) {
		if (this.dToAMapping.containsKey(p)) {
			return this.dToAMapping.get(p);
		} else {
			return null;
		}
	}

	/**
	 * Replaces an AlloyFormula with another one while keeping its DynalloyProgram mapping.
	 * @param oldValue Old AlloyFormula (will be no longer mapped)
	 * @param newValue New AlloyFormula, will replace oldValue as the mapped value.
	 */
	public void replaceValue(AlloyFormula oldValue, AlloyFormula newValue) {
		DynalloyProgram dynaAlloy = aToDMapping.get(oldValue);
		if (dynaAlloy != null) {
			this.dToAMapping.put(dynaAlloy, newValue);
			this.aToDMapping.remove(oldValue);
			this.aToDMapping.put(newValue, dynaAlloy);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DynAlloyAlloyMapping:\n");
		for (Map.Entry<DynalloyProgram, AlloyFormula> e : dToAMapping
				.entrySet()) {
			sb.append(e.getKey());
			sb.append(" -> ");
			sb.append(e.getValue());
			sb.append("\n");
		}
		sb.append("\nAssertions:\n");
		for (Map.Entry<AssertionDeclaration, AlloyAssertion> e : assertions
				.entrySet()) {
			sb.append(e.getKey().getAssertionId());
			sb.append(" -> ");
			sb.append(e.getValue());
			sb.append("\n");
		}
		sb.append("\nSimplifications:\n");
		for (Map.Entry<DynalloyProgram, DynalloyProgram> e : simdToDMapping
				.entrySet()) {
			sb.append(e.getKey());
			sb.append(" -> ");
			sb.append(e.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}
