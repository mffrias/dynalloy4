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
package ar.uba.dc.rfm.alloy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.IdxRange;


public class IdxRangeTest {

	@Test(expected=IllegalArgumentException.class)
	public void failConstructNegativeBegin() {
		new IdxRange(-1,2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void failConstructNegativeRange() {
		new IdxRange(5,4);
	}
	
	@Test
	public void isInitSingleton() {
		IdxRange r = new IdxRange(0,0);
		assertTrue(r.isInitSingleton());
	}
	
	@Test
	public void isNotInitSingleton() {
		IdxRange r = new IdxRange(1,1);
		assertTrue(!r.isInitSingleton());
	}

	@Test
	public void isNotSingleton() {
		IdxRange r = new IdxRange(1,2);
		assertEquals(1,r.getBegin());
		assertEquals(2,r.getEnd());
	}

	@Test
	public void isSingleton() {
		IdxRange r = new IdxRange(1,1);
		assertTrue(r.isSingleton());
		assertTrue(!r.isInitSingleton());
	}
	
	
}
