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
package ar.uba.dc.rfm.alloy.util;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;

public class FormulaPrinterTest {

	private FormulaPrinter subject;

	@Before
	public void setUp() throws Exception {
		subject = new FormulaPrinter();
	}

	@Test
	public void visitAndOr() {
		// input
		OrFormula or = new OrFormula(buildEmptyPred(), buildEmptyPred());
		AndFormula input = new AndFormula(buildEmptyPred(), or);
		// expected
		String expected = "predId[] and (predId[] or predId[])";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}
	@Test
	public void visitOrAnd() {
		// input
		AndFormula and = new AndFormula(buildEmptyPred(), buildEmptyPred());
		OrFormula input = new OrFormula(buildEmptyPred(), and);
		// expected
		String expected = "predId[] or (predId[] and predId[])";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}	
	@Test
	public void visitAndAnd() {
		// input
		AndFormula and = new AndFormula(buildEmptyPred(), buildEmptyPred());
		AndFormula input = new AndFormula(and, buildEmptyPred());
		// expected
		String expected = "predId[] and predId[] and predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	@Test
	public void visitOrOr() {
		// input
		OrFormula or = new OrFormula(buildEmptyPred(), buildEmptyPred());
		OrFormula input = new OrFormula(or, buildEmptyPred());
		// expected
		String expected = "predId[] or predId[] or predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}	
	@Test
	public void visitSimpleOrFormula() {
		// input
		OrFormula input = new OrFormula(buildEmptyPred(), buildEmptyPred());
		// expected
		String expected = "predId[] or predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	@Test
	public void visitSimpleAndFormula() {
		// input
		AndFormula input = new AndFormula(buildEmptyPred(), buildEmptyPred());
		// expected
		String expected = "predId[] and predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}
	
	@Test
	public void visitImpliesNoWrap() {
		// input
		ImpliesFormula input = new ImpliesFormula(buildEmptyPred(), buildEmptyPred());
		// expected
		String expected = "predId[] implies predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	@Test
	public void visitImpliesWrapOr() {
		// input
		OrFormula or = new OrFormula(buildEmptyPred(), buildEmptyPred());
		ImpliesFormula input = new ImpliesFormula(or, buildEmptyPred());
		// expected
		String expected = "(predId[] or predId[]) implies predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	@Test
	public void visitImpliesWrapAnd() {
		// input
		AndFormula and = new AndFormula(buildEmptyPred(), buildEmptyPred());
		ImpliesFormula input = new ImpliesFormula(and, buildEmptyPred());
		// expected
		String expected = "(predId[] and predId[]) implies predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}
	
	@Test
	public void visitNotFormula() {
		// input
		NotFormula input = new NotFormula(buildEmptyPred());
		// expected
		String expected = "not (predId[])";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	@Test
	public void visitEmptyPred() {
		// input
		PredicateFormula input = buildEmptyPred();
		// expected
		String expected = "predId[]";
		// test
		String s = (String) input.accept(subject);
		// check
		assertEquals(expected, s);
	}

	private PredicateFormula buildEmptyPred() {
		return new PredicateFormula(null,"predId", Collections
				.<AlloyExpression> emptyList());
	}

}
