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
package ar.uba.dc.rfm.da2a.prepare;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.da2a.prepare.ClosureRemover;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;

public class ClosureRemoverVisitorTest {

	@Test
	public void removeClosure() {
		// input
		Closure input = new Closure(new InvokeAction(null, "actionId", Collections
						.<AlloyExpression> emptyList()));
		// subject
		ClosureRemover visitor = new ClosureRemover(0, false, false);
		// test
		DynalloyProgram actual = (DynalloyProgram) input.accept(visitor);
		// check
		assertEquals(new Skip(), actual);
	}

	@Test
	public void oneLoopUnroll() {
		InvokeAction invokeAction = new InvokeAction(null, "actionId", Collections
										.<AlloyExpression> emptyList());
		// input
		Closure input = new Closure(invokeAction);
		// subject
		ClosureRemover visitor = new ClosureRemover(1, false, false);
		// test
		DynalloyProgram actual = (DynalloyProgram) input.accept(visitor);
		// check
		assertEquals(invokeAction, actual);
	}

	@Test
	public void oneLoopUnrollWithSkip() {
		InvokeAction invokeAction = new InvokeAction(null, "actionId", Collections
										.<AlloyExpression> emptyList());
		// input
		Closure input = new Closure(invokeAction);
		// expected
		Choice expected = new Choice(invokeAction, new Skip());
		// subject
		ClosureRemover visitor = new ClosureRemover(1, true, false);
		// test
		DynalloyProgram actual = (DynalloyProgram) input.accept(visitor);
		// check
		assertEquals(expected, actual);
	}
	
	@Test
	public void twoLoopUnroll() {
		// input
		InvokeAction invokeAction = new InvokeAction(null, "actionId", Collections
										.<AlloyExpression> emptyList());
		Closure input = new Closure(invokeAction);
		// expected 
		Composition expected = new Composition(invokeAction,invokeAction);
		// subject
		ClosureRemover visitor = new ClosureRemover(2, false, false);
		// test
		DynalloyProgram actual = (DynalloyProgram) input.accept(visitor);
		// check
		assertEquals(expected, actual);
	}
	
	@Test
	public void twoLoopUnrollWithSkip() {
		// input
		InvokeAction invokeAction = new InvokeAction(null, "actionId", Collections
										.<AlloyExpression> emptyList());
		Closure input = new Closure(invokeAction);
		// expected 
		Choice invokeOrSkip = new Choice(invokeAction, new Skip());
		Composition expected = new Composition(invokeOrSkip,invokeOrSkip);
		// subject
		ClosureRemover visitor = new ClosureRemover(2, true, false);
		// test
		DynalloyProgram actual = (DynalloyProgram) input.accept(visitor);
		// check
		assertEquals(expected, actual);
	}	
	
	
}
