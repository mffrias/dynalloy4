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

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.xlator.AlloyPredicates;
import ar.uba.dc.rfm.dynalloy.xlator.DynalloyXlatorVisitor;
import ar.uba.dc.rfm.dynalloy.xlator.IdxRangeCollectHelper;
import ar.uba.dc.rfm.dynalloy.xlator.ProgramTranslator;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class SimpleProgramTranslation {

	@Test
	public void translateSkip() {
		ProgramTranslator visitor = new ProgramTranslator(buildEmptyContext());

		DynalloyProgram skip = new Skip();
		AlloyFormulaWithLocals f = (AlloyFormulaWithLocals) skip.accept(visitor);

		assertEquals(new PredicateFormula(null, AlloyPredicates.TRUE_PRED_ID, Collections.<AlloyExpression> emptyList()), f.getFormula());
		assertEquals(new IdxRangeMap(), collectVarIdxRanges(f.getFormula()));
	}

	private IdxRangeMap collectVarIdxRanges(AlloyFormula f) {
		IdxRangeCollectHelper v = new IdxRangeCollectHelper();
		return v.collect(f);
	}

	private SpecContext buildEmptyContext() {
		final DynalloyModule dynalloyAST = new DynalloyModule("moduleId", Collections.<OpenDeclaration> emptySet(), "...", Collections
				.<ActionDeclaration> emptySet(), Collections.<ProgramDeclaration> emptySet(), Collections.<AssertionDeclaration> emptySet(),
				null, null);
		SpecContext context = new SpecContext(dynalloyAST);
		context.switchToModule("moduleId");
		return context;
	}

	private AlloyVariable a = new AlloyVariable("a");

	private AlloyVariable b = new AlloyVariable("b");

	private AlloyVariable c = new AlloyVariable("c");

	private AlloyVariable a_0 = new AlloyVariable("a", 0);

	private AlloyVariable a_1 = new AlloyVariable("a", 1);

	private AlloyVariable b_0 = new AlloyVariable("b", 0);

	private AlloyVariable c_0 = new AlloyVariable("c", 0);

	private AlloyVariable c_1 = new AlloyVariable("c", 1);

	@Test
	public void translateTestP1() {
		ProgramTranslator visitor = new ProgramTranslator(buildEmptyContext());

		DynalloyProgram test = (DynalloyProgram) new TestPredicate(buildP1(a, b));
		AlloyFormulaWithLocals f = (AlloyFormulaWithLocals) test.accept(visitor);

		PredicateFormula expectedFormula = buildP1(a_0, b_0);
		assertEquals(expectedFormula, f.getFormula());

		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(b_0.getVariableId(), 0, 0);
		assertEquals(expectedRanges, collectVarIdxRanges(f.getFormula()));
	}

	@Test
	public void translateTestP2() {
		ProgramTranslator visitor = new ProgramTranslator(buildEmptyContext());

		DynalloyProgram test = (DynalloyProgram) new TestPredicate(buildP2(a, b, c));
		AlloyFormula f = ((AlloyFormulaWithLocals) test.accept(visitor)).getFormula();

		PredicateFormula expectedFormula = buildP2(a_0, b_0, c_0);
		assertEquals(expectedFormula, f);

		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(b_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(c_0.getVariableId(), 0, 0);
		assertEquals(expectedRanges, collectVarIdxRanges(f));
	}

	@Test
	public void translateTestP3() {
		ProgramTranslator visitor = new ProgramTranslator(buildEmptyContext());

		DynalloyProgram test = (DynalloyProgram) new TestPredicate(buildP3(a, b, c));
		AlloyFormula f = ((AlloyFormulaWithLocals) test.accept(visitor)).getFormula();

		PredicateFormula expectedFormula = buildP3(a_0, b_0, c_0);
		assertEquals(expectedFormula, f);

		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(b_0.getVariableId(), 0, 0);
		expectedRanges.addIdxRange(c_0.getVariableId(), 0, 0);
		assertEquals(expectedRanges, collectVarIdxRanges(f));
	}

	private PredicateFormula buildP1(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(v1));
		ps.add(new ExprVariable(v2));
		PredicateFormula p1 = new PredicateFormula(null, "P1", ps);
		return p1;
	}

	private PredicateFormula buildP2(AlloyVariable v1, AlloyVariable v2, AlloyVariable v3) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprUnion(new ExprVariable(v1), new ExprVariable(v2)));
		ps.add(new ExprVariable(v3));
		PredicateFormula p1 = new PredicateFormula(null, "P2", ps);
		return p1;
	}

	private PredicateFormula buildP3(AlloyVariable v1, AlloyVariable v2, AlloyVariable v3) {
		return buildP3(v1, new ExprVariable(v2), v3);
	}

	private PredicateFormula buildP3(AlloyVariable v1, AlloyExpression e2, AlloyVariable v3) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprJoin(new ExprVariable(v1), new ExprUnion(e2, new ExprVariable(v3))));
		ps.add(new ExprVariable(v1));
		PredicateFormula p1 = new PredicateFormula(null, "P3", ps);
		return p1;
	}

	@Test
	public void translateSimpleExprActionA1() {
		ProgramTranslator visitor = new ProgramTranslator(buildActionA1Context());

		List<AlloyExpression> actualPs = new LinkedList<AlloyExpression>();
		actualPs.add(new ExprVariable(a));
		actualPs.add(new ExprVariable(b));
		actualPs.add(new ExprVariable(c));

		DynalloyProgram test = (DynalloyProgram) new InvokeAction(null, "A1", actualPs);
		AlloyFormula f = ((AlloyFormulaWithLocals) test.accept(visitor)).getFormula();

	}

	@Test
	public void translateCplxExprActionA1() {
		ProgramTranslator visitor = new ProgramTranslator(buildActionA1Context());

		List<AlloyExpression> actualPs = new LinkedList<AlloyExpression>();
		actualPs.add(new ExprVariable(a));
		actualPs.add(buildExpr(a, b));
		actualPs.add(new ExprVariable(c));

		DynalloyProgram test = (DynalloyProgram) new InvokeAction(null, "A1", actualPs);
		AlloyFormula f = ((AlloyFormulaWithLocals) test.accept(visitor)).getFormula();

	}

	private AlloyExpression buildExpr(AlloyVariable v1, AlloyVariable v2) {
		return new ExprUnion(new ExprJoin(new ExprVariable(v1), new ExprVariable(v1)), new ExprVariable(v2));
	}

	private SpecContext buildActionA1Context() {
		Map<String, ActionDeclaration> actions = new HashMap<String, ActionDeclaration>();

		List<VariableId> formalPs = new LinkedList<VariableId>();
		formalPs.add(new VariableId("x"));
		formalPs.add(new VariableId("y"));
		formalPs.add(new VariableId("z"));

		AlloyVariable x = new AlloyVariable("x");
		AlloyVariable y = new AlloyVariable("y");
		AlloyVariable z = new AlloyVariable("z");

		AlloyVariable x_prime = new AlloyVariable("x", true);
		AlloyVariable z_prime = new AlloyVariable("z", true);

		AlloyTyping typing = new AlloyTyping();
		typing.put(x, "T1");
		typing.put(y, "T2");
		typing.put(z, "T3");
		ActionDeclaration a1Body = new ActionDeclaration("A1", formalPs, buildP3(x, y, z), buildP1(x_prime, z_prime), typing);

		actions.put(a1Body.getActionId(), a1Body);
		DynalloyModule dynalloyAST = new DynalloyModule("moduleId", Collections.<OpenDeclaration> emptySet(), "...", new HashSet<ActionDeclaration>(actions
				.values()), Collections.<ProgramDeclaration> emptySet(), Collections.<AssertionDeclaration> emptySet(), null, null);

		SpecContext context = new SpecContext(dynalloyAST);
		context.setFields(new AlloyTyping());
		context.switchToModule("moduleId");
		a1Body.accept(new DynalloyXlatorVisitor(context, null, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false));
		return context;
	}

}
