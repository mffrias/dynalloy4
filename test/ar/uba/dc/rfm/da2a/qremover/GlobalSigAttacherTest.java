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

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.da2a.qremover.GlobalSigAttacher;


public class GlobalSigAttacherTest {

	@Test
	public void attachExprVariable() {
		// input
		ExprVariable v = new ExprVariable(new AlloyVariable("v"));
		// expected
		ExprJoin expected = new ExprJoin(new ExprConstant(null, "Q"), v);
		// perform test
		GlobalSigAttacher attacher = new GlobalSigAttacher("Q");
		AlloyExpression actual = (AlloyExpression) v.accept(attacher);
		// check
		assertEquals(expected,actual);
	}
	
	@Test
	public void attachExprConstant() {
		// input
		ExprConstant c = new ExprConstant(null, "C");
		// expected
		ExprConstant expected = new ExprConstant(null, "C");
		// perform test
		GlobalSigAttacher attacher = new GlobalSigAttacher("Q");
		AlloyExpression actual = (AlloyExpression) c.accept(attacher);
		// check
		assertEquals(expected,actual);
	}
	
	@Test
	public void attachExprJoin() {
		// input
		ExprJoin j = new ExprJoin(new ExprConstant(null, "T"),new ExprVariable(new AlloyVariable("x")));
		// expected
		ExprJoin expected = new ExprJoin(new ExprConstant(null, "T"),new ExprJoin(new ExprConstant(null, "Q"), new ExprVariable(new AlloyVariable("x"))));
		// perform test
		GlobalSigAttacher attacher = new GlobalSigAttacher("Q");
		AlloyExpression actual = (AlloyExpression) j.accept(attacher);
		// check
		assertEquals(expected,actual);
	}

	@Test
	public void attachExprUnion() {
		// input
		ExprUnion u = new ExprUnion(new ExprConstant(null, "T"),new ExprVariable(new AlloyVariable("x")));
		// expected
		ExprUnion expected = new ExprUnion(new ExprConstant(null, "T"),new ExprJoin(new ExprConstant(null, "Q"), new ExprVariable(new AlloyVariable("x"))));
		// perform test
		GlobalSigAttacher attacher = new GlobalSigAttacher("Q");
		AlloyExpression actual = (AlloyExpression) u.accept(attacher);
		// check
		assertEquals(expected,actual);
	}
	
}
