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

import java.util.Collections;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

public interface AlloyPredicates {

	public static final String TRUE_PRED_ID = "TruePred";

	public static final String FALSE_PRED_ID = "FalsePred";

	public static final PredicateFormula TRUE_CONSTANT = new PredicateFormula(null,
			TRUE_PRED_ID, Collections.<AlloyExpression> emptyList());

	public static final PredicateFormula FALSE_CONSTANT = new PredicateFormula(null,
			FALSE_PRED_ID, Collections.<AlloyExpression> emptyList());

	public static final String TRUE_PRED_SPEC = "pred TruePred[] {}";

	public static final String FALSE_PRED_SPEC = "pred FalsePred[] { not TruePred[] }";

}
