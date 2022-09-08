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

import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;

public class AlloyPrinterTest {

	private AlloyPrinter subject;

	@Before
	public void setUp() throws Exception {
		subject = new AlloyPrinter();
	}

	@Test
	public void printAlloySpec() {
		// input
		AlloyAssertion alloyAssertion = new AlloyAssertion("assertionId",
				new AlloyTyping(), buildEmptyPred());
		AlloyModule input = new AlloyModule("compilableA4Spec", Collections
				.<AlloyFact> emptyList(), Collections
				.<AlloyAssertion> singleton(alloyAssertion));
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("compilableA4Spec assert assertionId{ predId[]}"),
				tokenize(actual));
	}

	@Test
	public void printEmptyAssertion() {
		// input
		AlloyAssertion input = new AlloyAssertion("assertionId",
				new AlloyTyping(), buildEmptyPred());
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("assert assertionId{predId[]}"), tokenize(actual));
	}

	@Test
	public void printQuantifiedAssertion() {
		// input
		AlloyTyping typing = new AlloyTyping();
		typing.put(new AlloyVariable("v1"), "T1");
		typing.put(new AlloyVariable("v2"), "T2");
		AlloyAssertion input = new AlloyAssertion("assertionId", typing,
				buildEmptyPred());
		// test
		String actual = (String) input.accept(subject);
		// check
	}

	@Test
	public void printEmptyAlloySig() {
		// input
		AlloySig input = new AlloySig("MySigId");
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("sig MySigId {}"), tokenize(actual));
	}

	@Test
	public void printNonEmptyAlloySig() {
		// input
		AlloyTyping typing = new AlloyTyping();
		typing.put(new AlloyVariable("v1"), "T1");
		typing.put(new AlloyVariable("v2"), "T2");
		typing.put(new AlloyVariable("v3"), "T3");
		AlloySig input = new AlloySig(false, true, "MySigId", typing, null);
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("one sig MySigId {v1:T1,v2:T2,v3:T3}"),
				tokenize(actual));
	}

	private Vector<String> tokenize(String s) {
		StringTokenizer t = new StringTokenizer(s);
		Vector<String> result = new Vector<String>();
		while (t.hasMoreTokens()) {
			result.add(t.nextToken());
		}
		return result;
	}

	private PredicateFormula buildEmptyPred() {
		return new PredicateFormula(null,"predId", Collections
				.<AlloyExpression> emptyList());
	}

	@Test
	public void printOnePredFact() {
		// input
		AlloyFact input = new AlloyFact(buildEmptyPred());
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("fact {predId[]}"), tokenize(actual));
	}

	@Test
	public void printAndFormFact() {
		// input
		AlloyFact input = new AlloyFact(new AndFormula(buildEmptyPred(),
				buildEmptyPred()));
		// test
		String actual = (String) input.accept(subject);
		// check
		assertEquals(tokenize("fact {predId[] and predId[]}"), tokenize(actual));
	}

}
