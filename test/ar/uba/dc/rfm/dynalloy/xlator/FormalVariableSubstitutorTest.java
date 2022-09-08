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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaCloner;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.dynalloy.xlator.FormalVariableSubstitutor;

public class FormalVariableSubstitutorTest {

	private static final String PRED2_ID = "P2";

	private static final String PRED1_ID = "P1";

	private AlloyVariable x = new AlloyVariable("x");

	private AlloyVariable y = new AlloyVariable("y");

	private AlloyVariable x_prime = new AlloyVariable("x", true);

	private AlloyVariable z = new AlloyVariable("z");

	private AlloyVariable a = new AlloyVariable("a");

	private AlloyVariable a_prime = new AlloyVariable("a", true);

	private AlloyVariable b = new AlloyVariable("b");

	private AlloyVariable c = new AlloyVariable("c");

	private Map<VariableId, AlloyExpression> map;

	@Before
	public void setUp() throws Exception {
		buildMapping();
		buildP1();
		buildP2();

		buildExpectedPred2();
	}

	private PredicateFormula buildExpectedPred2() {
		List<AlloyExpression> expectedActualParamsPred2 = new LinkedList<AlloyExpression>();
		expectedActualParamsPred2.add(new ExprVariable(a_prime));
		return new PredicateFormula(null,PRED2_ID, expectedActualParamsPred2);
	}

	private PredicateFormula buildExpectedPred1() {
		List<AlloyExpression> expectedActualParamsPred1 = new LinkedList<AlloyExpression>();
		expectedActualParamsPred1.add(new ExprVariable(a));
		expectedActualParamsPred1.add(new ExprVariable(b));
		return new PredicateFormula(null, PRED1_ID, expectedActualParamsPred1);
	}

	private PredicateFormula buildP2() {
		List<AlloyExpression> formalParamsPred2 = new LinkedList<AlloyExpression>();
		formalParamsPred2.add(new ExprVariable(x_prime));
		return new PredicateFormula(null, PRED2_ID, formalParamsPred2);
	}

	private PredicateFormula buildP1() {
		List<AlloyExpression> formalParamsPred1 = new LinkedList<AlloyExpression>();
		formalParamsPred1.add(new ExprVariable(x));
		formalParamsPred1.add(new ExprVariable(y));
		return new PredicateFormula(null,PRED1_ID, formalParamsPred1);
	}

	private void buildMapping() {
		map = new HashMap<VariableId, AlloyExpression>();
		map.put(new VariableId("x"), new ExprVariable(a));
		map.put(new VariableId("y"), new ExprVariable(b));
	}

	/**
	 * Map: x->z, y->next P(x+y) => P(z+next)
	 */
	@Test
	public void substituteV2V_P1() {
		PredicateFormula expectedPred1 = buildExpectedPred1();
		PredicateFormula pred1 = buildP1();

		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(map, new HashSet<AlloyVariable>());
		PredicateFormula actualPred1 = (PredicateFormula) pred1
				.accept(new FormulaMutator(visitor));
		assertEquals(expectedPred1, actualPred1);
	}

	/**
	 * Map: x->z, y->next P(y') => P(next')
	 */
	@Test
	public void substituteV2V_P2() {
		PredicateFormula expectedPred2 = buildExpectedPred2();
		PredicateFormula pred2 = buildP2();

		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(map, new HashSet<AlloyVariable>());
		PredicateFormula actualPred2 = (PredicateFormula) pred2
				.accept(new FormulaMutator(visitor));
		assertEquals(expectedPred2, actualPred2);
	}

	@Test
	public void failWhenIncompleteMapping() {
		PredicateFormula pred1 = buildP1();
		Map<VariableId, AlloyExpression> incompleteMap = new HashMap<VariableId, AlloyExpression>();
		incompleteMap.put(new VariableId("x"), new ExprVariable(b));

		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(
				incompleteMap, new HashSet<AlloyVariable>());
		try {
			pred1.accept(new FormulaMutator(visitor));
			fail();
		} catch (RuntimeException ex) {
			assertEquals("no actual parameter for formal parameter: y", ex
					.getMessage());
		}
	}

	@Test
	public void failWhenPrimedExpression() {
		PredicateFormula p2 = buildP2();
		Map<VariableId, AlloyExpression> map = new HashMap<VariableId, AlloyExpression>();
		map.put(new VariableId("x"), new ExprJoin(new ExprVariable(a),
				new ExprVariable(b)));
		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(map, new HashSet<AlloyVariable>());
		try {
			p2.accept(new FormulaMutator(visitor));
			fail();
		} catch (RuntimeException ex) {
			assertEquals("The left-hand side of an update must be a variable",
					ex.getMessage());
		}
	}

	private PredicateFormula buildP3(AlloyVariable v1, AlloyExpression e2,
			AlloyVariable v3) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprJoin(new ExprVariable(v1), new ExprUnion(e2,
				new ExprVariable(v3))));
		PredicateFormula p1 = new PredicateFormula(null,"P3", ps);
		return p1;
	}

	@Test
	public void substituteV2E_P3() {
		// build input data
		PredicateFormula pred = buildP3(x, new ExprVariable(y), z);
		Map<VariableId, AlloyExpression> m = new HashMap<VariableId, AlloyExpression>();
		m.put(x.getVariableId(), new ExprVariable(a));
		m.put(y.getVariableId(), buildUnionJoin(a, b));
		m.put(z.getVariableId(), new ExprVariable(c));

		// build expected result
		PredicateFormula expectedPred = buildP3(a, buildUnionJoin(a, b), c);

		// perform test
		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(m, new HashSet<AlloyVariable>());
		PredicateFormula actualPred = (PredicateFormula) pred
				.accept(new FormulaMutator(visitor));

		// check result
		assertEquals(expectedPred, actualPred);

	}

	private AlloyExpression buildUnionJoin(AlloyVariable v1, AlloyVariable v2) {
		return new ExprUnion(new ExprJoin(new ExprVariable(v1),
				new ExprVariable(v1)), new ExprVariable(v2));
	}
	
	@Test
	public void substituteV2E_P1() {
		// build input data
		PredicateFormula pred = buildP1();
		Map<VariableId, AlloyExpression> m = new HashMap<VariableId, AlloyExpression>();
		m.put(x.getVariableId(), new ExprVariable(a));
		m.put(y.getVariableId(), new ExprConstant(null, "MyConstant"));

		// build expected result
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		ps.add(new ExprVariable(a));
		ps.add(new ExprConstant(null, "MyConstant"));
		PredicateFormula expectedPred = new PredicateFormula(null,PRED1_ID, ps);

		// perform test
		FormalVariableSubstitutor visitor = new FormalVariableSubstitutor(m, new HashSet<AlloyVariable>());
		PredicateFormula actualPred = (PredicateFormula) pred
				.accept(new FormulaMutator(visitor));

		// check result
		assertEquals(expectedPred, actualPred);

	}
	

}
