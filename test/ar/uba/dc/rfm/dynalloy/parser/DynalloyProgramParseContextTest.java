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
package ar.uba.dc.rfm.dynalloy.parser;

import static ar.uba.dc.rfm.alloy.util.AlloyHelper.var;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;

public class DynalloyProgramParseContextTest {

	@Test
	public void NoDeclaredImplesNoVariable() throws Exception {
		DynalloyProgramParseContext ctx = getContext(Arrays.<AlloyVariable>asList(var("a")), false);
		assertFalse(ctx.isVariableName("b"));
	}

	@Test
	public void DeclaredImplesVariable() throws Exception {
		DynalloyProgramParseContext ctx = getContext(Arrays.<AlloyVariable>asList(var("a")), true);
		assertTrue(ctx.isVariableName("a"));
		assertEquals(new AlloyVariable("a"), ctx.getAlloyVariable("a"));

		assertTrue(ctx.isVariableName("a'"));
		assertEquals(new AlloyVariable("a", true), ctx.getAlloyVariable("a'"));
	}

	@Test
	public void DeclaredImplesVariableButPrimed() throws Exception {
		DynalloyProgramParseContext ctx = getContext(Arrays.<AlloyVariable>asList(var("a")), false);
		assertTrue(ctx.isVariableName("a"));
		assertEquals(new AlloyVariable("a"), ctx.getAlloyVariable("a"));

		assertFalse(ctx.isVariableName("a'"));
	}

	static DynalloyProgramParseContext getContext(List<AlloyVariable> vars,
			boolean allowPrimed) {
		return new DynalloyProgramParseContext(
				new HashSet<AlloyVariable>(vars), new HashSet<AlloyVariable>(), allowPrimed);
	}

}
