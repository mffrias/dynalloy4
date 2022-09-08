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
package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.HashMap;
import java.util.Map;

import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.util.FormulaCloner;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

class CompositionJoiner {

	private IdxRangeCollectHelper varHelper;
	private DynAlloyAlloyMapping mapping;
	
	public CompositionJoiner() {
		varHelper = new IdxRangeCollectHelper();
	}
	
	public CompositionJoiner(DynAlloyAlloyMapping mapping) {
		this();
		setMapping(mapping);
	}

	/**
	 * F1(x[i1,j1]) ; F2(x[i2,j2]) => F1(x[i1,j1]) AND F2(x[j1+i2,j1+j2])  
	 * @author jgaleotti
	 */
	public AlloyFormula join(AlloyFormula l, AlloyFormula r) {
		AlloyFormula reindexRight = reindexRightFormula(r, varHelper.collect(l));
		AlloyFormula joinedFormula = new AndFormula(l, reindexRight);
		return joinedFormula;

	}

	private AlloyFormula reindexRightFormula(AlloyFormula r, IdxRangeMap lrs) {
		Map<VariableId, Integer> offsetMap = buildOffsetMap(lrs, varHelper
				.collect(r));
		FormulaMutator fm = new FormulaMutator(new OffsetSubindexer(offsetMap));
		fm.setMapping(getMapping());
		return (AlloyFormula) r.accept(fm);
	}

	private Map<VariableId, Integer> buildOffsetMap(IdxRangeMap rLeft,
			IdxRangeMap rRight) {
		Map<VariableId, Integer> map = new HashMap<VariableId, Integer>();
		for (VariableId v : rRight.keySet()) {
			if (rLeft.contains(v) && rLeft.getIdxRange(v).getEnd() > 0) {
				int offset = rLeft.getIdxRange(v).getEnd();
				map.put(v, offset);
			}
		}
		return map;
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}
}
