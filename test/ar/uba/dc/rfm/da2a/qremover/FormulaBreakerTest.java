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

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;


public class FormulaBreakerTest {

	private PredicateFormula buildP() {
		return new PredicateFormula(null,"P",new LinkedList<AlloyExpression>());
	}
	
	@Test
	public void breakSimpleImpliesFormula() {
		// input
		ImpliesFormula input = new ImpliesFormula(new AndFormula(buildP(),buildP()),buildP());
		// expected
		List<PredicateFormula> expected = new LinkedList<PredicateFormula>();
		expected.add(buildP());
		expected.add(buildP());
		expected.add(buildP());
		// perform test
		FormulaBreaker breaker = new FormulaBreaker();
		List<AlloyFormula> actual = (List<AlloyFormula>)input.accept(breaker);
		// check
		assertEquals(expected,actual);
	}
	
	@Test
	public void breakComposition() {
		// input
		ImpliesFormula input = new ImpliesFormula(new AndFormula(buildP(),buildCompositionOfAtomicInvokactions()),buildP());
		// expected
		List<PredicateFormula> expected = new LinkedList<PredicateFormula>();
		//pre
		expected.add(buildP());
		//program
		expected.add(buildP());
		expected.add(buildP());
		expected.add(buildP());
		expected.add(buildP());
		//post
		expected.add(buildP());
		// perform test
		FormulaBreaker breaker = new FormulaBreaker();
		List<AlloyFormula> actual = (List<AlloyFormula>)input.accept(breaker);
		// check
		assertEquals(expected,actual);
		
	}

	private AlloyFormula buildCompositionOfAtomicInvokactions() {
		return new AndFormula(new AndFormula(buildP(),buildP()),new AndFormula(buildP(),buildP()));
	}
	
	@Test
	public void breakChoice() {
		// input
		ImpliesFormula input = new ImpliesFormula(new AndFormula(buildP(),buildChoiceOfAtomicInvokactions()),buildP());
		// expected
		List<AlloyFormula> expected = new LinkedList<AlloyFormula>();
		//pre
		expected.add(buildP());
		//program
		expected.add(buildChoiceOfAtomicInvokactions());
		//post
		expected.add(buildP());
		// perform test
		FormulaBreaker breaker = new FormulaBreaker();
		List<AlloyFormula> actual = (List<AlloyFormula>)input.accept(breaker);
		// check
		assertEquals(expected,actual);
	}

	private AlloyFormula buildChoiceOfAtomicInvokactions() {
		return new OrFormula(new AndFormula(buildP(),buildP()),new AndFormula(buildP(),buildP()));
	}
	
	@Test
	public void breakCompositionOfChoices() {
		// input
		ImpliesFormula input = new ImpliesFormula(new AndFormula(buildP(),buildCompositionOfChoices()),buildP());
		// expected
		List<AlloyFormula> expected = new LinkedList<AlloyFormula>();
		//pre
		expected.add(buildP());
		//program
		expected.add(new OrFormula(buildP(),buildP()));
		expected.add(new OrFormula(buildP(),buildP()));
		//post
		expected.add(buildP());
		// perform test
		FormulaBreaker breaker = new FormulaBreaker();
		List<AlloyFormula> actual = (List<AlloyFormula>)input.accept(breaker);
		// check
		assertEquals(expected,actual);
	}

	private AlloyFormula buildCompositionOfChoices() {
		return new AndFormula(new OrFormula(buildP(),buildP()),new OrFormula(buildP(),buildP()));
	}	
	
	@Test(expected=RuntimeException.class)
	public void failsWhenBeginsAtAndFormula() {
		(new AndFormula(buildP(),buildP())).accept(new FormulaBreaker());
	}

	@Test(expected=RuntimeException.class)
	public void failsWhenBeginsAtOrFormula() {
		(new OrFormula(buildP(),buildP())).accept(new FormulaBreaker());
	}

	@Test(expected=RuntimeException.class)
	public void failsWhenBeginsAtNotFormula() {
		(new NotFormula(buildP())).accept(new FormulaBreaker());
	}

	@Test(expected=RuntimeException.class)
	public void failsWhenBeginsAtPredForm() {
		buildP().accept(new FormulaBreaker());
	}

	@Test(expected=RuntimeException.class)
	public void failsWhenBreakingInnerImpliesFormula() {
		(new AndFormula(buildP(),new ImpliesFormula(buildP(),buildP()))).accept(new FormulaBreaker());
	}	
}
