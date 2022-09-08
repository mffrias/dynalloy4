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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.xlator.AlloyPredicates;
import ar.uba.dc.rfm.dynalloy.xlator.DynalloyXlatorVisitor;
import ar.uba.dc.rfm.dynalloy.xlator.ProgramTranslator;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class DynalloyToAlloyVisitorTest {

	private SpecContext context;

	@Before
	public void setUp() throws Exception {
		context = buildContext();
	}

	private PredicateFormula buildPred(VariableId v, int idx1, int idx2) {
		return buildP(new AlloyVariable(v, idx1), new AlloyVariable(v, idx2));
	}

	private EqualsFormula buildEqu(AlloyVariable v1, AlloyVariable v2) {
		return new EqualsFormula(new ExprVariable(v1), new ExprVariable(v2));
	}

	private PredicateFormula buildP(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(v1));
		ps.add(new ExprVariable(v2));
		return new PredicateFormula(null, "P", ps);
	}

	private PredicateFormula buildNotP(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(v1));
		ps.add(new ExprVariable(v2));
		return new PredicateFormula(null, "NotP", ps);
	}

	private SpecContext buildContext() {
		AlloyVariable x = new AlloyVariable("x");
		AlloyVariable x_prime = new AlloyVariable("x", true);
		AlloyVariable y = new AlloyVariable("y");

		PredicateFormula pre = buildP(x, y);
		PredicateFormula post = buildP(x, x_prime);

		List<VariableId> actionFormalParams = new LinkedList<VariableId>();
		actionFormalParams.add(new VariableId("x"));
		actionFormalParams.add(new VariableId("y"));

		AlloyTyping alloyTyping = new AlloyTyping();
		alloyTyping.put(x,"T1");
		alloyTyping.put(y,"T2");
		ActionDeclaration body = new ActionDeclaration("A", actionFormalParams,
				pre, post, alloyTyping);
		Map<String, ActionDeclaration> map = new HashMap<String, ActionDeclaration>();
		map.put(body.getActionId(), body);
		DynalloyModule dynalloyAST = new DynalloyModule("moduleId", Collections
				.<OpenDeclaration> emptySet(), "...",
				new HashSet<ActionDeclaration>(map.values()), Collections
						.<ProgramDeclaration> emptySet(), Collections
						.<AssertionDeclaration> emptySet(), null, null);
		SpecContext result = new SpecContext( dynalloyAST );
		result.switchToModule("moduleId");
		result.setFields(alloyTyping);
		body.accept(new DynalloyXlatorVisitor(result, null, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false));
		return result;
	}

	private VariableId aId = new VariableId("a");
	private VariableId bId = new VariableId("b");

	private AlloyVariable a = new AlloyVariable(aId);
	private AlloyVariable a_0 = new AlloyVariable(aId, 0);
	private AlloyVariable a_1 = new AlloyVariable(aId, 1);
	private AlloyVariable a_2 = new AlloyVariable(aId, 2);
	private AlloyVariable b = new AlloyVariable(bId);
	private AlloyVariable b_0 = new AlloyVariable(bId, 0);
	private AlloyVariable b_1 = new AlloyVariable(bId, 1);

	@Test
	public void invokeAction() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		DynalloyProgram p = new InvokeAction(null, "A", actualPs);
		// expected
		AlloyFormula expected = PredicateFormula.buildPredicate("A",a_0, a_1, b_0 );
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();
		// check result
		assertEquals(expected, actual);
	}

	private List<AlloyExpression> actualParams(VariableId x, VariableId y) {
		List<AlloyExpression> actualPs = new LinkedList<AlloyExpression>();
		actualPs.add(new ExprVariable(new AlloyVariable(x)));
		actualPs.add(new ExprVariable(new AlloyVariable(y)));
		return actualPs;
	}

	@Test
	public void testPredicate() {
		// input
		TestPredicate p = new TestPredicate(buildP(a, b));
		// expected
		PredicateFormula expected = buildP(a_0, b_0);
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void skip() {
		// input
		Skip p = new Skip();
		// expected
		PredicateFormula expected = new PredicateFormula(null,
				AlloyPredicates.TRUE_PRED_ID, Collections
						.<AlloyExpression> emptyList());
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();
		// check result
		assertEquals(expected, actual);
	}

	@Test
	public void compositionActAct() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		DynalloyProgram a1 = new InvokeAction(null, "A", actualPs);
		DynalloyProgram a2 = new InvokeAction(null, "A", actualPs);
		Composition p = new Composition(a1, a2);
		// expected
		
		AlloyFormula left = PredicateFormula.buildPredicate("A",a_0, a_1,b_0);
		AlloyFormula right = PredicateFormula.buildPredicate("A",a_1, a_2,b_0);
		AndFormula expected = new AndFormula(left,right);
		
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();
		// check result
		assertEquals(expected, actual);

	}

	@Test
	public void choicePredAct() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		TestPredicate pred = new TestPredicate(buildP(a, b));
		DynalloyProgram act = new InvokeAction(null, "A", actualPs);
		Choice p = new Choice(pred, act);
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormulaWithLocals formula_with_locals = (AlloyFormulaWithLocals) p.accept(t);
		AlloyFormula actual = formula_with_locals.getFormula();

	}

	@Test
	public void choiceActPred() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		InvokeAction act = new InvokeAction(null, "A", actualPs);
		TestPredicate pred = new TestPredicate(buildP(a, b));
		Choice p = new Choice(act, pred);
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();

	}

	@Test
	public void choiceActActSameIdxRange() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		InvokeAction a1 = new InvokeAction(null, "A", actualPs);
		InvokeAction a2 = new InvokeAction(null, "A", actualPs);
		Choice p = new Choice(a1, a2);
		// expected
		OrFormula expected = new OrFormula(new AndFormula(buildP(a_0, b_0),
				buildP(a_0, a_1)), new AndFormula(buildP(a_0, b_0), buildP(a_0,
				a_1)));
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();

	}

	@Test
	public void choiceActActDiffIdxRange() {
		// input
		List<AlloyExpression> a1Ps = actualParams(aId, bId);
		List<AlloyExpression> a2Ps = actualParams(bId, aId);
		InvokeAction a1 = new InvokeAction(null, "A", a1Ps);
		InvokeAction a2 = new InvokeAction(null, "A", a2Ps);
		Choice p = new Choice(a1, a2);
		// expected
		AndFormula expLeft = new AndFormula(new AndFormula(buildP(a_0, b_0),
				buildP(a_0, a_1)), buildEqu(b_0, b_1));
		AndFormula expRight = new AndFormula(new AndFormula(buildP(b_0, a_0),
				buildP(b_0, b_1)), buildEqu(a_0, a_1));
		OrFormula expected = new OrFormula(expLeft, expRight);
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();

	}

	@Test
	public void compositionActPred() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		InvokeAction act = new InvokeAction(null, "A", actualPs);
		TestPredicate pred = new TestPredicate(buildP(a, b));
		Composition p = new Composition(act, pred);
		// expected
		AndFormula expected = new AndFormula(new AndFormula(buildP(a_0, b_0),
				buildP(a_0, a_1)), buildP(a_1, b_0));
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();

	}

	@Test
	public void compositionPredAct() {
		// input
		List<AlloyExpression> actualPs = actualParams(aId, bId);
		TestPredicate pred = new TestPredicate(buildP(a, b));
		DynalloyProgram act = new InvokeAction(null, "A", actualPs);
		Composition p = new Composition(pred, act);
		// expected
		AndFormula expected = new AndFormula(buildP(a_0, b_0), new AndFormula(
				buildP(a_0, b_0), buildP(a_0, a_1)));
		// perform test
		ProgramTranslator t = new ProgramTranslator(context);
		AlloyFormula actual = ((AlloyFormulaWithLocals) p.accept(t)).getFormula();

	}
}
