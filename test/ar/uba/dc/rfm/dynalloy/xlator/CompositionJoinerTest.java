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
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.xlator.CompositionJoiner;
import ar.uba.dc.rfm.dynalloy.xlator.IdxRangeCollectHelper;
import ar.uba.dc.rfm.dynalloy.xlator.ProgramTranslator;

public class CompositionJoinerTest {

	private AlloyVariable a = new AlloyVariable("a");

	private AlloyVariable a_0 = new AlloyVariable("a", 0);

	private AlloyVariable b = new AlloyVariable("b");

	private AlloyVariable b_0 = new AlloyVariable("b", 0);

	private AlloyVariable c = new AlloyVariable("c");

	private AlloyVariable c_0 = new AlloyVariable("c", 0);

	/**
	 * 
	 * @param v1
	 * @param v2
	 * @return P(v1,v2)
	 */
	private PredicateFormula buildP1(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(v1));
		ps.add(new ExprVariable(v2));
		PredicateFormula p = new PredicateFormula(null,"P", ps);
		return p;
	}

	@Test
	public void compositionTestPTestP() {
		// input data
		Composition f = new Composition(new TestPredicate(buildP1(a, b)),
				new TestPredicate(buildP1(b, c)));
		// expected data
		AndFormula expectedF = new AndFormula(buildP1(a_0, b_0), buildP1(b_0,
				c_0));

		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(b_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(c_0.getVariableId(), 0, 0);

		// perform test
		ProgramTranslator translator = new ProgramTranslator(null);
		AlloyFormulaWithLocals rf = (AlloyFormulaWithLocals) f.accept(translator);

		// check
		assertEquals(expectedF, rf.getFormula());
		assertEquals(expectedRanges, collectVarIdxRanges(rf.getFormula()));
	}

	private AlloyFormula buildInlinedAction(VariableId x, VariableId y, int idx1,
			int idx2) {
		AlloyVariable x_0 = new AlloyVariable(x, idx1);
		AlloyVariable x_1 = new AlloyVariable(x, idx1 + 1);
		AlloyVariable y_0 = new AlloyVariable(y, idx2);

		PredicateFormula pre = buildP1(x_0, y_0);
		PredicateFormula post = buildP1(x_0, x_1);

		AndFormula f = new AndFormula(pre, post);
		return f;
	}

	@Test
	public void compositionAndPAndP() {
		// input data
		AlloyFormula left = buildInlinedAction(a.getVariableId(), b.getVariableId(),
				0, 0);
		AlloyFormula right = buildInlinedAction(b.getVariableId(),
				a.getVariableId(), 0, 0);
		// expected data
		AndFormula expectedF = new AndFormula(left, buildInlinedAction(b
				.getVariableId(), a.getVariableId(), 0, 1));
		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a_0.getVariableId(), 0, 1);
		expectedRanges.addIdxRange(b_0.getVariableId(), 0, 1);

		// perform test
		CompositionJoiner joiner = new CompositionJoiner();
		AlloyFormula rf = joiner.join(left, right);

		// check
		assertEquals(expectedF, rf);
		assertEquals(expectedRanges, collectVarIdxRanges(rf));

		/*
		 * VarIndexRanges r = new VarIndexRanges(); r.addRange(x, 0, 1);
		 * r.addRange(y, 0, 0);
		 */

	}

	private VariableId x_id = new VariableId("x");

	private AlloyVariable x_0 = new AlloyVariable(x_id, 0);

	private AlloyVariable x_1 = new AlloyVariable(x_id, 1);

	private AlloyVariable x_2 = new AlloyVariable(x_id, 2);

	private AlloyVariable x_3 = new AlloyVariable(x_id, 3);

	private AlloyVariable x_4 = new AlloyVariable(x_id, 4);

	private AlloyVariable x_5 = new AlloyVariable(x_id, 5);

	private VariableId y_id = new VariableId("y");

	private AlloyVariable y_0 = new AlloyVariable(y_id, 0);

	private AlloyVariable y_1 = new AlloyVariable(y_id, 1);

	private VariableId z_id = new VariableId("z");

	private AlloyVariable z_0 = new AlloyVariable(z_id, 0);

	private AlloyVariable z_1 = new AlloyVariable(z_id, 1);

	@Test
	public void compositionFandF() {
		// input
		AlloyFormula lf = buildNestedAndFormula(new AlloyFormula[] { buildP1(x_0, x_1),
				buildP1(x_1, x_2), buildP1(x_2, x_3), buildP1(y_0, y_1) });
		AlloyFormula rf = buildNestedAndFormula(new AlloyFormula[] { buildP1(z_0, z_1),
				buildP1(x_0, x_1), buildP1(x_1, x_2) });

		IdxRangeMap lr = new IdxRangeMap();
		lr.addIdxRange(x_id, 0, 3);
		lr.addIdxRange(y_id, 0, 1);

		IdxRangeMap rr = new IdxRangeMap();
		rr.addIdxRange(x_id, 0, 2);
		rr.addIdxRange(z_id, 0, 1);

		// expected result
		AlloyFormula elf = buildNestedAndFormula(new AlloyFormula[] { buildP1(x_0, x_1),
				buildP1(x_1, x_2), buildP1(x_2, x_3), buildP1(y_0, y_1) });
		AlloyFormula erf = buildNestedAndFormula(new AlloyFormula[] { buildP1(z_0, z_1),
				buildP1(x_3, x_4), buildP1(x_4, x_5) });

		AndFormula expectedFormula = new AndFormula(elf, erf);
		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(x_id, 0, 5);
		expectedRanges.addIdxRange(y_id, 0, 1);
		expectedRanges.addIdxRange(z_id, 0, 1);

		// perform test
		CompositionJoiner joiner = new CompositionJoiner();
		AlloyFormula actual = joiner.join(lf, rf);

		// check
		assertEquals(expectedFormula, actual);
		assertEquals(expectedRanges, collectVarIdxRanges(actual));
	}

	@Test
	public void compositionIncompleteRanges() {
		// input
		AlloyFormula lf = buildNestedAndFormula(new AlloyFormula[] { buildP1(x_0, x_1),
				buildP1(x_1, x_3), buildP1(y_0, y_1) });
		AlloyFormula rf = buildNestedAndFormula(new AlloyFormula[] { buildP1(z_0, z_1),
				buildP1(x_0, x_1), buildP1(x_1, x_2) });

		IdxRangeMap lr = new IdxRangeMap();
		lr.addIdxRange(x_id, 0, 3);
		lr.addIdxRange(y_id, 0, 1);

		IdxRangeMap rr = new IdxRangeMap();
		rr.addIdxRange(x_id, 0, 2);
		rr.addIdxRange(z_id, 0, 1);

		// expected result
		AlloyFormula elf = buildNestedAndFormula(new AlloyFormula[] { buildP1(x_0, x_1),
				buildP1(x_1, x_3), buildP1(y_0, y_1) });
		AlloyFormula erf = buildNestedAndFormula(new AlloyFormula[] { buildP1(z_0, z_1),
				buildP1(x_3, x_4), buildP1(x_4, x_5) });

		AndFormula expectedFormula = new AndFormula(elf, erf);
		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(x_id, 0, 5);
		expectedRanges.addIdxRange(y_id, 0, 1);
		expectedRanges.addIdxRange(z_id, 0, 1);

		// perform test
		CompositionJoiner joiner = new CompositionJoiner();
		AlloyFormula actual = joiner.join(lf, rf);

		// check
		assertEquals(expectedFormula, actual);
		assertEquals(expectedRanges, collectVarIdxRanges(actual));

	}

	private AndFormula buildNestedAndFormula(AlloyFormula[] children) {
		assert (children.length > 1);
		AndFormula result = null;
		AlloyFormula fstChild = null;
		for (AlloyFormula child : children) {
			if (fstChild == null) {
				fstChild = child;
			} else if (result == null)
				result = new AndFormula(fstChild, child);
			else
				result = new AndFormula(result, child);
		}
		return result;
	}

	private IdxRangeMap collectVarIdxRanges(AlloyFormula f) {
		IdxRangeCollectHelper v = new IdxRangeCollectHelper();
		return v.collect(f);
	}
}
