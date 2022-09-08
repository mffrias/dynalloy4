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

import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;

public class ExpressionPrinterTest {

	private ExpressionPrinter subject;

	@Before
	public void setUp() throws Exception {
		subject = new ExpressionPrinter();
	}

	@Test
	public void visitExprVariable() {
		// input
		AlloyVariable v = new AlloyVariable("MY_VARIABLE");
		ExprVariable input = new ExprVariable(v);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals(v.toString(), s);
	}

	@Test
	public void visitSimpleExprJoin() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprJoin input = new ExprJoin(my_constant, my_constant);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "." + "MY_CONSTANT", s);
	}

	@Test
	public void visitMultiExprJoin() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprJoin input = new ExprJoin(my_constant, new ExprJoin(my_constant, my_constant));
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "." + "(MY_CONSTANT"+ "." + "MY_CONSTANT)", s);
	}

	@Test
	public void visitSimpleExprUnion() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprUnion input = new ExprUnion(my_constant, my_constant);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "+" + "MY_CONSTANT", s);	}

	@Test
	public void visitExprConstant() {
		// input
		ExprConstant input = new ExprConstant(null, "MY_CONSTANT");
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT", s);
	}

	@Test
	public void visitMultiExprUnion() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprUnion input = new ExprUnion(new ExprUnion(my_constant, my_constant), my_constant);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "+"+ "MY_CONSTANT" + "+" + "MY_CONSTANT", s);	}

	@Test
	public void visitJoinUnion() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprUnion union = new ExprUnion(my_constant, my_constant);
		ExprJoin input = new ExprJoin(my_constant, union);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "." + "("+ "MY_CONSTANT"+ "+" + "MY_CONSTANT" + ")", s);
	}

	@Test
	public void visitUnionJoin() {
		// input
		ExprConstant my_constant = new ExprConstant(null, "MY_CONSTANT");
		ExprJoin union = new ExprJoin(my_constant, my_constant);
		ExprUnion input = new ExprUnion(my_constant, union);
		// test
		String s = (String) input.accept(subject);
		// check 
		assertEquals("MY_CONSTANT" + "+" + "("+ "MY_CONSTANT"+ "." + "MY_CONSTANT" + ")", s);
	}

}
