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

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.util.VarCollector;

/**
 * Computes the index range of each variable of an indexed formula: the lowest and the highest
 * index at which it occurs. The ranges drive the translation: which indexed copies become
 * arguments of a predicate ({@link CallingConvention}), how far {@link CompositionJoiner}
 * shifts the second part of a sequence, which equalities {@link ChoiceJoiner} adds, which
 * variables {@link DynalloyXlatorVisitor} quantifies, and which index the post's primed
 * variables get. No state.
 */
class IdxRangeCollectHelper {
	/**
	 * Returns the index range of each variable of {@code f}. A range spans every index between
	 * its ends, whether or not {@code f} uses it.
	 *
	 * <p>Left out: variables marked immutable ({@code \pre[x]}, see {@link FirstSubindexer}),
	 * and variables whose id starts with {@code SK_jml_pred_java_primitive_}, the auxiliary
	 * constants of TACO's Java arithmetic ({@code JMLAuxiliaryConstantsFactory} in TACO). So
	 * these are never shifted, completed with equalities or quantified by index; the only
	 * declarations the assertion gets for such variables are TACO's typings, which
	 * {@link DynalloyXlatorVisitor#visit(ar.uba.dc.rfm.dynalloy.ast.DynalloyModule)} adds as
	 * they are.
	 *
	 * <p>Assumes that every other variable of {@code f} is indexed: a plain or primed one makes
	 * {@code IdxRange} throw an {@code IllegalArgumentException}.
	 *
	 * @param f an indexed formula; not changed
	 * @return a new map from variable id to range
	 */
	public IdxRangeMap collect(AlloyFormula f) {
		VarCollector visitor = new VarCollector();
		f.accept(new FormulaVisitor(visitor));
		IdxRangeMap ranges = new IdxRangeMap();
		for (AlloyVariable v : visitor.getVariables()) {
			VariableId varId = v.getVariableId();
			int idx = v.getIndex();
			if (ranges.contains(varId) && v.isMutable()) {
				int begin = ranges.getIdxRange(varId).getBegin();
				int end = ranges.getIdxRange(varId).getEnd();
				if (idx < begin) {
					ranges.addIdxRange(varId, idx, end);
				} else if (end < idx) {
					ranges.addIdxRange(varId, begin, idx);
				}
			} else {
				if (v.isMutable() && !v.getVariableId().getString().startsWith("SK_jml_pred_java_primitive_")){
					ranges.addIdxRange(varId, idx, idx);
				}
			}
		} 
		return ranges;
	}
}
