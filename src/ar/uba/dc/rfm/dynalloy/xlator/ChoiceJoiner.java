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

import static ar.uba.dc.rfm.dynalloy.xlator.CollectionUtils.diff;
import static ar.uba.dc.rfm.dynalloy.xlator.CollectionUtils.intersect;
import ar.uba.dc.rfm.alloy.IdxRange;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.util.FormulaBuffer;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

class ChoiceJoiner {

	private IdxRangeCollectHelper varHelper;
	private DynAlloyAlloyMapping mapping;

	public ChoiceJoiner() {
		varHelper = new IdxRangeCollectHelper();
	}

	public ChoiceJoiner(DynAlloyAlloyMapping mapping) {
		this();
		setMapping(mapping);
	}
	
	public AlloyFormula join(AlloyFormula l, AlloyFormula r) {
		IdxRangeMap lrs = varHelper.collect(l);
		IdxRangeMap rrs = varHelper.collect(r);
		AlloyFormula syncLeft = synchronizeIdxRanges(l, lrs, rrs);
		AlloyFormula syncRight = synchronizeIdxRanges(r, rrs, lrs);
		return new OrFormula(syncLeft, syncRight);
	}

	private AlloyFormula synchronizeIdxRanges(AlloyFormula f, IdxRangeMap frs,
			IdxRangeMap grs) {
		FormulaBuffer buff = new FormulaBuffer(f);
		buff.setMapping(mapping);
		addDiffEqualities(buff, frs, grs);
		addIntersectEqualities(buff, frs, grs);
		replaceLastIdx(buff, frs, grs);
		AlloyFormula formula = buff.toFormula();
		return formula;
	}

	private void addIntersectEqualities(FormulaBuffer buff, IdxRangeMap frs,
			IdxRangeMap grs) {
		for (VariableId varId : intersect(frs.keySet(), grs.keySet())) {
			IdxRange fv = frs.getIdxRange(varId);
			IdxRange gv = grs.getIdxRange(varId);
			if (fv.isInitSingleton() && !gv.isInitSingleton()) {
				buff.cojoinEquals(varId, fv.getEnd(), gv.getEnd());
			}
		}
	}

	private void addDiffEqualities(FormulaBuffer buff, IdxRangeMap frs,
			IdxRangeMap grs) {
		for (VariableId varId : diff(grs.keySet(), frs.keySet())) {
			IdxRange idxRange = grs.getIdxRange(varId);
			if (!idxRange.isInitSingleton()) {
				if (idxRange.isSingleton() || idxRange.getBegin()==1)
					buff.cojoinEquals(varId, IdxRange.SUBINDEX_0, idxRange
							.getEnd());
				else
					buff.cojoinEquals(varId, idxRange.getBegin(), idxRange
							.getEnd());
			}
		}
	}

	private void replaceLastIdx(FormulaBuffer buff, IdxRangeMap frs,
			IdxRangeMap grs) {
		for (VariableId varId : intersect(frs.keySet(), grs.keySet())) {
			IdxRange r1 = frs.getIdxRange(varId);
			int end2 = grs.getIdxRange(varId).getEnd();

			if ((!r1.isInitSingleton()) && (r1.getEnd() < end2)) {
				buff.replaceIdx(varId, r1.getEnd(), end2);
			}
		}
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}
}
