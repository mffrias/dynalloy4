package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;

abstract class VariableIndexer {

	/**
	 * 
	 * @param f
	 *            a P(x,..x) formula
	 * @return Let m: variable.(plain+primed) |-> variable.(0+1), returns m(f)
	 */
	public static PredicateFormula addIdxsToPred(PredicateFormula f) {
		return (PredicateFormula) f.accept(new FormulaMutator(
				new FirstSubindexer()));
	}

	public static PredicateFormula substituteAndIndex(PredicateFormula p,
			List<VariableId> formals, List<AlloyExpression> actuals) {
		assert (formals.size() == actuals.size());
	
		PredicateFormula subs_p = substituteFormalParams(p, formals, actuals);
		PredicateFormula indexed_p = addIdxsToPred(subs_p);
		return indexed_p;
	}

	/**
	 * @param f
	 *            a P(x0,...xn) formula
	 * @param formal
	 *            a list of formal parameters (variables)
	 * @param actual
	 *            a list of actual parameters (expressions)
	 * 
	 * 
	 * @return Let m: formal->actual (mapping from formal to actual parameters),
	 *         returns m(f)
	 */
	public static PredicateFormula substituteFormalParams(PredicateFormula f,
			List<VariableId> formal, List<AlloyExpression> actual) {
	
		if (formal.size() != actual.size())
			throw new IllegalArgumentException();
	
		Map<VariableId, AlloyExpression> map = new HashMap<VariableId, AlloyExpression>();
		for (int i = 0; i < formal.size(); i++) {
			map.put(formal.get(i), actual.get(i));
		}
	
		FormalVariableSubstitutor substitutor = new FormalVariableSubstitutor(
				map, new HashSet<AlloyVariable>());
		PredicateFormula result = (PredicateFormula) f
				.accept(new FormulaMutator(substitutor));
		return result;
	}

}
