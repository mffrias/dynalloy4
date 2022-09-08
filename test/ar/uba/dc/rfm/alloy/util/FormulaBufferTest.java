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

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaBuffer;
import ar.uba.dc.rfm.dynalloy.xlator.AlloyPredicates;

public class FormulaBufferTest {

	private static final String PREDICATE_ID = "predicateId";

	private VariableId a = new VariableId("a");

	private VariableId b = new VariableId("b");

	private VariableId c = new VariableId("c");

	private EqualsFormula buildEq(VariableId v, int idx1, int idx2) {
		return new EqualsFormula(new ExprVariable(new AlloyVariable(v, idx1)),
				new ExprVariable(new AlloyVariable(v, idx2)));
	}

	private PredicateFormula buildF1(VariableId v, int idx1, int idx2) {
		List<AlloyExpression> actualParams = new LinkedList<AlloyExpression>();
		actualParams.add(new ExprVariable(new AlloyVariable(v, idx1)));
		actualParams.add(new ExprVariable(new AlloyVariable(v, idx2)));
		return new PredicateFormula(null,"P1", actualParams);
	}

	@Test
	public void addEquToPredicate() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = new AndFormula(buildF1(a, 0, 2), buildEq(b, 0,
				3));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.cojoinEquals(b, 0, 3);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void replaceIdxToPredicate() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = buildF1(a, 0, 4);
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.replaceIdx(a, 2, 4);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void replaceIdxAndAddEqu() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = new AndFormula(buildF1(a, 0, 4), buildEq(b, 0,
				2));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.replaceIdx(a, 2, 4);
		buffer.cojoinEquals(b, 0, 2);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void conflictReplaceVsAdd() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = new AndFormula(buildF1(a, 0, 4), buildEq(a, 0,
				2));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.replaceIdx(a, 2, 4);
		buffer.cojoinEquals(a, 0, 2);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void precedenceReplaceAmongAdd() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = new AndFormula(buildF1(a, 0, 4), buildEq(a, 0,
				2));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.cojoinEquals(a, 0, 2);
		buffer.replaceIdx(a, 2, 4);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void addEqusToOnePred() {
		// input
		AlloyFormula f = buildF1(a, 0, 2);
		// expected
		AlloyFormula expected = new AndFormula(buildF1(a, 0, 2),
				new AndFormula(buildEq(b, 0, 3), buildEq(c, 0, 2)));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.cojoinEquals(b, 0, 3);
		buffer.cojoinEquals(c, 0, 2);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void addEquToAndFormula() {
		// input
		AlloyFormula f = new AndFormula(buildF1(a, 0, 2), buildF1(b, 0, 1));
		// expected
		AlloyFormula expected = new AndFormula(f, buildEq(c, 0, 2));
		// perform test
		FormulaBuffer buffer = new FormulaBuffer(f);
		buffer.cojoinEquals(c, 0, 2);
		AlloyFormula actual = buffer.toFormula();
		// check result
		assertEquals(expected, actual);
	}

}
