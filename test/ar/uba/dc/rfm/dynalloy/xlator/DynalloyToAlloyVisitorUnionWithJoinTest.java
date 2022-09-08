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
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.xlator.IdxRangeCollectHelper;
import ar.uba.dc.rfm.dynalloy.xlator.ProgramTranslator;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class DynalloyToAlloyVisitorUnionWithJoinTest {

	private SpecContext context;

	@Before
	public void setUp() throws Exception {
		context = buildContext();
	}

	private SpecContext buildContext() {

		AlloyVariable x = new AlloyVariable("x");
		AlloyVariable y = new AlloyVariable("y");
		AlloyVariable y_prime = new AlloyVariable("y", true);

		PredicateFormula pre = buildPre(x, y);
		PredicateFormula post = buildPost(y_prime);

		List<VariableId> actionFormalParams = new LinkedList<VariableId>();
		actionFormalParams.add(new VariableId("x"));
		actionFormalParams.add(new VariableId("y"));

		AlloyTyping alloyTyping = new AlloyTyping();
		alloyTyping.put(x,"T1");
		alloyTyping.put(y,"T2");
		ActionDeclaration body = new ActionDeclaration("A1",
				actionFormalParams, pre, post, alloyTyping);
		Map<String, ActionDeclaration> map = new HashMap<String, ActionDeclaration>();
		map.put(body.getActionId(), body);
		DynalloyModule dynalloyAST = new DynalloyModule("moduleId", Collections
				.<OpenDeclaration> emptySet(), "...",
				new HashSet<ActionDeclaration>(map.values()), Collections
						.<ProgramDeclaration> emptySet(), Collections
						.<AssertionDeclaration> emptySet(), null, null);
		SpecContext result = new SpecContext( dynalloyAST );
		result.setFields(alloyTyping);
		result.switchToModule("moduleId");
		body.accept(new DynalloyXlatorVisitor(result, null, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false));
		return result;
	}

	private PredicateFormula buildPost(AlloyVariable v1) {
		List<AlloyExpression> paramsPost = new LinkedList<AlloyExpression>();
		paramsPost.add(new ExprVariable(v1));
		PredicateFormula post = new PredicateFormula(null, "post", paramsPost);
		return post;
	}

	private PredicateFormula buildPre(AlloyVariable v1, AlloyVariable v2) {
		List<AlloyExpression> paramsPre = new LinkedList<AlloyExpression>();
		paramsPre
				.add(new ExprUnion(new ExprVariable(v1), new ExprVariable(v2)));
		PredicateFormula pre = new PredicateFormula(null, "pre", paramsPre);
		return pre;
	}

	private PredicateFormula buildPre(AlloyExpression e, AlloyVariable v) {
		List<AlloyExpression> paramsPre = new LinkedList<AlloyExpression>();
		paramsPre.add(new ExprUnion(e, new ExprVariable(v)));
		PredicateFormula pre = new PredicateFormula(null, "pre", paramsPre);
		return pre;
	}

	@Test
	public void inlineUnionWithJoin() {
		AlloyVariable a = new AlloyVariable("a");
		AlloyVariable b = new AlloyVariable("b");

		List<AlloyExpression> actualParams = new LinkedList<AlloyExpression>();
		actualParams
				.add(new ExprJoin(new ExprVariable(a), new ExprVariable(b)));
		actualParams.add(new ExprVariable(a));

		ProgramTranslator inliner = new ProgramTranslator(context);
		AlloyFormulaWithLocals rf = (AlloyFormulaWithLocals) (new InvokeAction(null, "A1",
				actualParams)).accept(inliner);

		AlloyVariable a_0 = new AlloyVariable("a", 0);
		AlloyVariable a_1 = new AlloyVariable("a", 1);
		AlloyVariable b_0 = new AlloyVariable("b", 0);

		ExprJoin a_0dotb_0 = new ExprJoin(new ExprVariable(a_0),
				new ExprVariable(b_0));


		IdxRangeCollectHelper helper = new IdxRangeCollectHelper();
		IdxRangeMap actualRanges = helper.collect(rf.getFormula());

		IdxRangeMap expectedRanges = new IdxRangeMap();
		expectedRanges.addIdxRange(a.getVariableId(), 0, 1);
		expectedRanges.addIdxRange(b.getVariableId(), 0, 0);

		assertEquals(expectedRanges, actualRanges);
	}
}
