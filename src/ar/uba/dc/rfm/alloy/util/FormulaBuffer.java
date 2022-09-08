/*
 * Dynalloy Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.alloy.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class FormulaBuffer {

	private final class MapSubstitutor extends VarSubstitutor {

		private Map<AlloyVariable, AlloyVariable> map;

		public MapSubstitutor(Map<AlloyVariable, AlloyVariable> m) {
			map = m;
		}

		@Override
		protected AlloyExpression getExpr(AlloyVariable v) {
			if (map.get(v) != null)
				return new ExprVariable(map.get(v));
			else
				return null;
		}

	}

	private final AlloyFormula f;

	private List<EqualsFormula> eqs;
	
	private DynAlloyAlloyMapping mapping;

	public FormulaBuffer(AlloyFormula _f) {
		eqs = new LinkedList<EqualsFormula>();
		replaceMap = new HashMap<AlloyVariable, AlloyVariable>();
		f = _f;
	}
	
//	public void removeIdx(VariableId varId){
//		int currIdx = Integer.decode((varId.getString().substring(varId.getString().lastIndexOf("_")+1, varId.getString().length())));
//		String varIdWithoutIndex = varId.getString().substring(0,varId.getString().lastIndexOf("_"));
//		replaceMap.put(new AlloyVariable(varIdWithoutIndex, currIdx), new AlloyVariable(varIdWithoutIndex,currIdx));
//	}

	public void cojoinEquals(VariableId varId, int i1, int i2) {
		eqs.add(buildEqualsFormula(varId, i1, i2));
	}

	private Map<AlloyVariable, AlloyVariable> replaceMap;

	public void replaceIdx(VariableId varId, int i1, int i2) {
		replaceMap.put(new AlloyVariable(varId, i1), new AlloyVariable(varId,
				i2));
	}

	public AlloyFormula toFormula() {
		return addEqualities(replace(f));
	}

	private AlloyFormula replace(AlloyFormula f) {
		FormulaMutator mutator = new FormulaMutator(new MapSubstitutor(replaceMap));
		mutator.setMapping(mapping);
		AlloyFormula result = (AlloyFormula) f.accept(mutator);
		return result;
	}

	private AlloyFormula addEqualities(AlloyFormula f) {
		AlloyFormula r = null;
		for (EqualsFormula pred : eqs) {
			if (r == null)
				r = pred;
			else
				r = new AndFormula(r, pred);
		}
		if (r != null)
			return new AndFormula(f, r);
		else
			return f;

	}

	private EqualsFormula buildEqualsFormula(VariableId vId, int i1, int i2) {
		return new EqualsFormula(new ExprVariable(new AlloyVariable(vId, i1)),
				new ExprVariable(new AlloyVariable(vId, i2)));
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}
}
