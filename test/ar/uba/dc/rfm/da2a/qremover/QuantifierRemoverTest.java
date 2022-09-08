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
package ar.uba.dc.rfm.da2a.qremover;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.AlloyBuffer;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover;
import ar.uba.dc.rfm.da2a.qremover.QuantifierRemover.QuantifierRemoverResult;

public class QuantifierRemoverTest {

	private AlloyVariable a_0 = new AlloyVariable("a", 0);

	private AlloyVariable a_1 = new AlloyVariable("a", 1);

	private AlloyVariable b_0 = new AlloyVariable("b", 0);

	@Test
	public void removeQuantifiedInvokeActions() {
		// input
		ImpliesFormula i = new ImpliesFormula(new AndFormula(buildP(a_0, b_0),
				new AndFormula(buildP(a_0, b_0), buildP(a_0, a_1))), buildP(
				a_1, b_0));
		AlloyBuffer buff = new AlloyBuffer();
		buff.setAssertion(i);
		buff.addQuantifiedVar(a_0, "T");
		buff.addQuantifiedVar(a_1, "T");
		buff.addQuantifiedVar(b_0, "T");
		buff.setAssertionId("assertionId");
		AlloyModule a4spec = buff.toAlloyModule();
		AlloyAssertion assertion = a4spec.getAssertion("assertionId");
		// expected
		List<AlloyFact> expectedFacts = new LinkedList<AlloyFact>();
		expectedFacts.add(new AlloyFact(buildQP(a_0, b_0)));
		expectedFacts.add(new AlloyFact(buildQP(a_0, b_0)));
		expectedFacts.add(new AlloyFact(buildQP(a_0, a_1)));

		AlloyFormula expectedAssertion = buildQP(a_1, b_0);

		// perform test
		QuantifierRemoverResult actual = new QuantifierRemover()
				.removeQuantifiers(assertion);
		// check results
		assertEquals(expectedFacts, actual.qfFacts);
		assertEquals(expectedAssertion, actual.qfAssertion.getFormula());

	}

	private PredicateFormula buildP(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(v1));
		ps.add(new ExprVariable(v2));
		return new PredicateFormula(null, "P", ps);
	}

	private PredicateFormula buildQP(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprJoin(new ExprConstant(null, "QF"), new ExprVariable(v1)));
		ps.add(new ExprJoin(new ExprConstant(null, "QF"), new ExprVariable(v2)));
		return new PredicateFormula(null, "P", ps);
	}

}
