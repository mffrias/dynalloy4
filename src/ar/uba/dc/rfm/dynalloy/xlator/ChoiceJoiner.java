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

/**
 * Joins the translations of the two branches of a choice {@code p + q} into
 * {@code p' or q'}, where each branch is completed so that both end with each variable at the
 * same index. {@code ProgramTranslator.visit(Choice)} creates one for each step (a choice of n
 * branches is joined left to right), with the context's {@code DynAlloyAlloyMapping}. See
 * docs/ARCHITECTURE.md ("Translation").
 *
 * <p>With f one branch, g the other and the index ranges of their variables
 * ({@link IdxRangeCollectHelper}), f gets:
 * <ul>
 * <li>for a variable only in g, if its range there is not {@code [0, 0]}: {@code x_b = x_e},
 *     with e the end of the range in g and b its beginning, or 0 if the range is a single index
 *     or begins at 1 (f leaves the variable as it was);</li>
 * <li>for a variable in both whose range is {@code [0, 0]} in f but not in g:
 *     {@code x_0 = x_e}, e the end in g;</li>
 * <li>for a variable in both whose range is not {@code [0, 0]} in f and ends there before it
 *     ends in g: its last index in f renamed to its last index in g.</li>
 * </ul>
 * The equalities are added to f with {@code and} ({@code FormulaBuffer}). Builds new formulas;
 * with a mapping, the mutators also replace the old formulas by the new ones in it. No static
 * state.
 */
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
	
	/**
	 * Joins two branches (see the class comment).
	 *
	 * @param l the translation of the left branch, indexed
	 * @param r the translation of the right branch, indexed
	 * @return a new {@code OrFormula} of the two completed branches
	 */
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
