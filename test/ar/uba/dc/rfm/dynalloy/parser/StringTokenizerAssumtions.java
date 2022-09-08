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

import static org.junit.Assert.*;

import java.util.StringTokenizer;

import org.junit.Test;


public class StringTokenizerAssumtions {

	@Test
	public void initialSpaceAreSkipped() throws Exception {
		StringTokenizer t = new StringTokenizer("  token1");
		assertTrue(t.hasMoreTokens());
		assertEquals("token1", t.nextToken());
	}
	
	@Test
	public void newlineStopsTokenizer() throws Exception {
		StringTokenizer t = new StringTokenizer("\n  token1");
		assertTrue(t.hasMoreTokens());
		assertEquals("token1", t.nextToken());
	}	
}
