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

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRangeMap;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.dynalloy.xlator.FirstSubindexer;
import ar.uba.dc.rfm.dynalloy.xlator.IdxRangeCollectHelper;

public class FirstSubIndexerTest {

	private AlloyVariable a = new AlloyVariable("a");
	private AlloyVariable c = new AlloyVariable("c");
	private AlloyVariable e = new AlloyVariable("e");

	private AlloyVariable a_prime = new AlloyVariable("a",true);
	private AlloyVariable b_prime = new AlloyVariable("b",true);
	private AlloyVariable c_prime = new AlloyVariable("c",true);
	private AlloyVariable a_0 = new AlloyVariable("a",0);
	private AlloyVariable b_0 = new AlloyVariable("b",0);
	private AlloyVariable c_0 = new AlloyVariable("c",0);
	private AlloyVariable e_0 = new AlloyVariable("e",0);
	
	private AlloyVariable a_1 = new AlloyVariable("a",1);
	private AlloyVariable b_1 = new AlloyVariable("b",1);
	private AlloyVariable c_1 = new AlloyVariable("c",1);
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void predOnlyVariables() {
		List<AlloyExpression> params = new LinkedList<AlloyExpression>();
		params.add(new ExprVariable(a));
		params.add(new ExprVariable(b_prime)); // just b'
		params.add(new ExprVariable(c_prime)); // fst c', snd c
		params.add(new ExprVariable(e));  // just e
		params.add(new ExprVariable(a_prime)); // fs a, snd a'
		params.add(new ExprVariable(e)); //repetition
		params.add(new ExprVariable(c));
		PredicateFormula pred = new PredicateFormula(null,"P1",params);
		
		FirstSubindexer visitor = new FirstSubindexer();
		PredicateFormula actualPred = (PredicateFormula) pred.accept(new FormulaMutator(visitor));

		List<AlloyExpression> expectedParams = new LinkedList<AlloyExpression>();
		expectedParams.add(new ExprVariable(a_0));
		expectedParams.add(new ExprVariable(b_1)); 
		expectedParams.add(new ExprVariable(c_1)); 
		expectedParams.add(new ExprVariable(e_0)); 
		expectedParams.add(new ExprVariable(a_1)); 
		expectedParams.add(new ExprVariable(e_0)); 
		expectedParams.add(new ExprVariable(c_0));
		PredicateFormula expectedPred = new PredicateFormula(null,"P1",expectedParams);
		
		assertEquals(expectedPred, actualPred);
		
		IdxRangeCollectHelper collector = new IdxRangeCollectHelper();
		IdxRangeMap actualRangedVars = collector.collect(actualPred);
		IdxRangeMap expectedRangedVars = new IdxRangeMap();
		expectedRangedVars.addIdxRange(a_0.getVariableId(), 0, 1);
		expectedRangedVars.addIdxRange(b_0.getVariableId(), 1, 1);
		expectedRangedVars.addIdxRange(c_0.getVariableId(), 0, 1);
		expectedRangedVars.addIdxRange(e_0.getVariableId(), 0, 0);

		assertEquals(expectedRangedVars, actualRangedVars);
	}

}
