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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.xlator.FirstSubindexer;
import ar.uba.dc.rfm.dynalloy.xlator.FormalVariableSubstitutor;

public class CplxExprSubsAndIndexTest {

	private AlloyVariable a = new AlloyVariable("a");

	private AlloyVariable a_0 = new AlloyVariable("a", 0);

	private AlloyVariable b = new AlloyVariable("b");

	private AlloyVariable b_0 = new AlloyVariable("b", 0);

	private AlloyVariable c = new AlloyVariable("c");

	private AlloyVariable c_0 = new AlloyVariable("c", 0);
	
	private AlloyVariable x = new AlloyVariable("x");

	private AlloyVariable y = new AlloyVariable("y");

	private AlloyVariable z = new AlloyVariable("z");
	
	@Before
	public void setUp() throws Exception {
	}

	private AlloyExpression buildExprVariable(AlloyVariable v1) {
		return new ExprVariable(v1);
	}

	private AlloyExpression buildExprUnionJoin(AlloyVariable v1,
			AlloyVariable v2) {
		return new ExprUnion(new ExprJoin(new ExprVariable(v1),
				new ExprVariable(v1)), new ExprVariable(v2));
	}

	private AlloyExpression buildExprJoinUnionJoin(AlloyVariable v1, AlloyVariable v2,
			AlloyVariable v3) {
		return new ExprJoin(new ExprVariable(v1), new ExprUnion(new ExprUnion(
				new ExprJoin(new ExprVariable(v1), new ExprVariable(v1)),
				new ExprVariable(v2)), new ExprVariable(v3)));
	}

	private AlloyExpression buildExprJoin(AlloyVariable v1, AlloyVariable v2) {
		return new ExprJoin(new ExprVariable(v1), new ExprVariable(v2));
	}

	private AlloyExpression buildExprUnion(AlloyVariable v1, AlloyVariable v2) {
		return new ExprUnion(new ExprVariable(v1), new ExprVariable(v2));
	}

	@Test
	public void exprUnion() {
		// prepare input
		AlloyExpression expr = buildExprUnion(x, y);
		Map<VariableId, AlloyExpression> mapXtoA = new HashMap<VariableId, AlloyExpression>();
		mapXtoA.put(new VariableId("x"), new ExprVariable(a));
		mapXtoA.put(new VariableId("y"), new ExprVariable(b));
		// prepare expected result
		AlloyExpression expectedExpr = buildExprUnion(a_0, b_0);
		// perform test
		AlloyExpression actualExpr = indexVariables(substituteFormalVariables(
				expr, mapXtoA));
		// check
		assertEquals(expectedExpr, actualExpr);
	}

	@Test
	public void exprUnionJoin() {
		// prepare input
		AlloyExpression expr = buildExprUnionJoin(x, y);
		Map<VariableId, AlloyExpression> mapXtoA = new HashMap<VariableId, AlloyExpression>();
		mapXtoA.put(new VariableId("x"), new ExprVariable(a));
		mapXtoA.put(new VariableId("y"), new ExprVariable(b));
		// prepare expected result
		AlloyExpression expectedExpr = buildExprUnionJoin(a_0, b_0);
		// perform test
		AlloyExpression actualExpr = indexVariables(substituteFormalVariables(
				expr, mapXtoA));
		// check
		assertEquals(expectedExpr, actualExpr);
	}

	@Test
	public void exprVariable() {
		// prepare input
		AlloyExpression expr = buildExprVariable(x);
		Map<VariableId, AlloyExpression> mapXtoA = new HashMap<VariableId, AlloyExpression>();
		mapXtoA.put(new VariableId("x"), new ExprVariable(a));
		// prepare expected result
		AlloyExpression expectedExpr = buildExprVariable(a_0);
		// perform test
		AlloyExpression actualExpr = indexVariables(substituteFormalVariables(
				expr, mapXtoA));
		// check
		assertEquals(expectedExpr, actualExpr);
	}

	@Test
	public void exprJoinUnionJoin() {
		// prepare input
		AlloyExpression expr = buildExprJoinUnionJoin(x, y, z);
		Map<VariableId, AlloyExpression> mapXtoA = new HashMap<VariableId, AlloyExpression>();
		mapXtoA.put(new VariableId("x"), new ExprVariable(a));
		mapXtoA.put(new VariableId("y"), new ExprVariable(b));
		mapXtoA.put(new VariableId("z"), new ExprVariable(c));
		// prepare expected result
		AlloyExpression expectedExpr = buildExprJoinUnionJoin(a_0, b_0, c_0);
		// perform test
		AlloyExpression actualExpr = indexVariables(substituteFormalVariables(
				expr, mapXtoA));
		// check
		assertEquals(expectedExpr, actualExpr);
		
	}
	
	@Test
	public void exprJoin() {
		// prepare input
		AlloyExpression expr = buildExprJoin(x, y);
		Map<VariableId, AlloyExpression> mapXtoA = new HashMap<VariableId, AlloyExpression>();
		mapXtoA.put(new VariableId("x"), new ExprVariable(a));
		mapXtoA.put(new VariableId("y"), new ExprVariable(b));
		// prepare expected result
		AlloyExpression expectedExpr = buildExprJoin(a_0, b_0);
		// perform test
		AlloyExpression actualExpr = indexVariables(substituteFormalVariables(
				expr, mapXtoA));
		// check
		assertEquals(expectedExpr, actualExpr);

	}

	private AlloyExpression substituteFormalVariables(AlloyExpression expr,
			Map<VariableId, AlloyExpression> map) {
		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(map, new HashSet<AlloyVariable>());
		AlloyExpression result = (AlloyExpression) expr.accept(visitor);
		return result;
	}

	private AlloyExpression indexVariables(AlloyExpression expr) {
		FirstSubindexer visitor = new FirstSubindexer();
		AlloyExpression result = (AlloyExpression) expr.accept(visitor);
		return result;
	}
}
