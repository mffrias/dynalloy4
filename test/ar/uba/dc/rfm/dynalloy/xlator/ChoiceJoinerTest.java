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
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.xlator.AlloyPredicates;
import ar.uba.dc.rfm.dynalloy.xlator.ChoiceJoiner;

public class ChoiceJoinerTest {

	private PredicateFormula buildPred(VariableId v, int idx1, int idx2) {
		List<AlloyExpression> actualParams = new LinkedList<AlloyExpression>();
		actualParams.add(new ExprVariable(new AlloyVariable(v, idx1)));
		actualParams.add(new ExprVariable(new AlloyVariable(v, idx2)));
		return new PredicateFormula(null, "P1", actualParams);
	}

	private VariableId a = new VariableId("a");

	private VariableId b = new VariableId("b");

	@Test
	public void choiceIntersect() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = buildPred(a, 0, 3);
		// expected
		AlloyFormula expected = new OrFormula(buildPred(a, 0, 3), buildPred(a, 0, 3));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceOffsetIntersect() {
		// input
		AlloyFormula l = buildPred(a, 2, 4);
		AlloyFormula r = buildPred(a, 3, 5);
		// expected
		AlloyFormula expected = new OrFormula(buildPred(a, 2, 5), buildPred(a, 3, 5));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceOffsetSingleton() {
		// input
		AlloyFormula l = buildPred(a, 2, 4);
		AlloyFormula r = buildPred(a, 5, 5);
		// expected
		AlloyFormula expected = new OrFormula(buildPred(a, 2, 5), buildPred(a, 5, 5));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceIntersectSingleton() {
		// input
		AlloyFormula l = buildPred(a, 0, 0);
		AlloyFormula r = buildPred(a, 0, 2);
		// expected
		AlloyFormula expected = new OrFormula(new AndFormula(buildPred(a, 0, 0), buildEq(a,0,2)), buildPred(a, 0, 2));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceBothSingletons() {
		// input
		AlloyFormula l = buildPred(a, 0, 0);
		AlloyFormula r = buildPred(a, 0, 0);
		// expected
		AlloyFormula expected = new OrFormula(buildPred(a, 0, 0), buildPred(a, 0, 0));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	private EqualsFormula buildEq(VariableId v, int idx1, int idx2) {
		return new EqualsFormula(new ExprVariable(new AlloyVariable(v, idx1)),new ExprVariable(new AlloyVariable(v, idx2)));
	}

	@Test
	public void choiceDiff() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = buildPred(b, 0, 3);
		// expected
		AlloyFormula expected = new OrFormula(new AndFormula(buildPred(a, 0, 2),
				buildEq(b, 0, 3)), new AndFormula(buildPred(b, 0, 3), buildEq(a,
				0, 2)));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceDiffInitSingleton() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = new AndFormula(buildPred(a, 0, 2),buildPred(b, 0, 0));
		// expected
		AlloyFormula expected = new OrFormula(l,r);
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceDiffSingleton() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = new AndFormula(buildPred(a, 0, 2),buildPred(b, 1, 1));
		// expected
		AlloyFormula expected = new OrFormula(new AndFormula(l,buildEq(b,0,1)),r);
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void choiceDiffOffsetNotSingleton() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = new AndFormula(buildPred(a, 0, 2),buildPred(b, 1, 2));
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
	}

	@Test
	public void choiceDiffOffsetSingleton() {
		// input
		AlloyFormula l = buildPred(a, 0, 2);
		AlloyFormula r = new AndFormula(buildPred(a, 0, 2),buildPred(b, 3, 3));
		// expected
		AlloyFormula expected = new OrFormula(new AndFormula(l,buildEq(b,0,3)),r);
		// perform test
		ChoiceJoiner joiner = new ChoiceJoiner();
		AlloyFormula actual = joiner.join(l, r);
		// check result
		assertEquals(expected, actual);
	}
}
