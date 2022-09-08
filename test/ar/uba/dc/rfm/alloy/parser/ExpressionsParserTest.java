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
package ar.uba.dc.rfm.alloy.parser;

import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;
import static ar.uba.dc.rfm.alloy.util.AlloyHelper._const;
import static ar.uba.dc.rfm.alloy.util.AlloyHelper._var;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

public class ExpressionsParserTest {

	@Test
	public void parseExprUnion() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ctx.addVariable("var1");
		ctx.addVariable("var2");
		ExprUnion expected = new ExprUnion(buildExprVariable("var1"),
				buildExprVariable("var2"));
		AlloyExpression actual = parseTerm("var1+var2", ctx);
		assertEquals(expected, actual);
	}
	
	@Test
	public void parseExprIntersection() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ctx.addVariable("var1");
		ctx.addVariable("var2");
		ExprIntersection expected = new ExprIntersection(buildExprVariable("var1"),
				buildExprVariable("var2"));
		AlloyExpression actual = parseTerm("var1&var2", ctx);
		assertEquals(expected, actual);
	}	
	
	@Test
	public void parseExprIfCondition() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ctx.addVariable("var1");
		ctx.addVariable("var2");
		
		ArrayList<AlloyExpression> formulaParams = new ArrayList<AlloyExpression>();
		formulaParams.add(ExprVariable.buildExprVariable("var1"));
		formulaParams.add(new ExprIntLiteral(1));
		PredicateFormula formula = new PredicateFormula(null, "eq", formulaParams);
		
		ExprIfCondition expected = new ExprIfCondition(formula,buildExprVariable("var1"),
				buildExprVariable("var2"));
		AlloyExpression actual = parseTerm("eq[var1,1] => var1 else var2", ctx);
		assertEquals(expected, actual);
	}	
	
	@Test
	public void parseExprIfCondition2() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ctx.addVariable("var1");
		ctx.addVariable("var2");
		
		ArrayList<AlloyExpression> formulaParams = new ArrayList<AlloyExpression>();
		formulaParams.add(ExprVariable.buildExprVariable("var1"));
		formulaParams.add(new ExprIntLiteral(1));
		PredicateFormula formula = new PredicateFormula(null, "eq", formulaParams);
		
		ExprIfCondition expected = new ExprIfCondition(formula,new ExprIntLiteral(2),
				new ExprIntLiteral(3));
		AlloyExpression actual = parseTerm("eq[var1,1] => 2 else 3", ctx);
		assertEquals(expected, actual);
	}	

	@Test
	public void parseExprIntLiteral15() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ExprIntLiteral expected = new ExprIntLiteral(15);
		AlloyExpression actual = parseTerm("15", ctx);
		assertEquals(expected, actual);
	}

	@Test
	public void parseExprIntLiteralMinus15() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		AlloyExpression actual = parseTerm("-15", ctx);
		assertNull(actual);
	}

	@Test
	public void parseExprIntLiteralPlus15() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		AlloyExpression actual = parseTerm("+15", ctx);
		assertNull(actual);
	}

	@Test
	public void parseExprIntLiteral37() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ExprIntLiteral expected = new ExprIntLiteral(37);
		AlloyExpression actual = parseTerm("37", ctx);
		assertEquals(expected, actual);
	}

	@Test
	public void parseAdd37To105() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ExprFunction expected = ExprFunction.buildExprFunction("add",
				new ExprIntLiteral(37), new ExprIntLiteral(105));
		AlloyExpression actual = parseTerm("add[37,105]", ctx);
		assertEquals(expected, actual);
	}

	@Test
	public void parseAdd37Toi() throws RecognitionException,
			TokenStreamException {
		MockContext ctx = new MockContext();
		ctx.addVariable("i");
		ExprFunction expected = ExprFunction.buildExprFunction("add",
				new ExprIntLiteral(37), ExprVariable.buildExprVariable("i"));
		AlloyExpression actual = parseTerm("add[37,i]", ctx);
		assertEquals(expected, actual);
	}

	@Test
	public void arrowSet() throws Exception {
		AlloyTyping e = new AlloyTyping();
		e.put(new AlloyVariable("v1"), "Type1 -> set Type2");
		AlloyTyping a = parseAlloyTyping("v1:Type1 -> set Type2");
		
		String typeOf = a.get(new AlloyVariable("v1"));
		assertEquals("Type1 -> set Type2".trim(), typeOf.trim());
	}


	@Test
	public void TokenInContextImpliesVariable() throws Exception {
		MockContext ctx = new MockContext();
		ctx.addVariable("variable1");
		AlloyExpression e = parseTerm("variable1", ctx);
		assertEquals(_var("variable1"), e);
	}

	@Test
	public void TokenNotInContextImpliesConstant() throws Exception {
		MockContext ctx = new MockContext();
		AlloyExpression e = parseTerm("true", ctx);
		assertEquals(_const("true"), e);
	}

	@Test
	public void emptyTyping() throws Exception {
		AlloyTyping e = parseAlloyTyping("");
		assertEquals(new AlloyTyping(), e);
	}

	@Test
	public void singleVarTyping() throws Exception {
		AlloyTyping a = parseAlloyTyping("v1:Type1");
		String typeOf = a.get(new AlloyVariable("v1"));
		assertEquals("Type1", typeOf.trim());
	}

	@Test
	public void multiVarTyping() throws Exception {

		AlloyTyping a = parseAlloyTyping("v1:Type1, v2:Type2");
		
		String typeOf_v1 = a.get(new AlloyVariable("v1"));
		assertEquals("Type1", typeOf_v1.trim());

		String typeOf_v2 = a.get(new AlloyVariable("v2"));
		assertEquals("Type2", typeOf_v2.trim());
	}

	@Test
	public void multiVarSingleTypeTyping() throws Exception {
		AlloyTyping a = parseAlloyTyping("v1, v2 :Type1");
		
		String typeOf_v1 = a.get(new AlloyVariable("v1"));
		assertEquals("Type1", typeOf_v1.trim());
		
		String typeOf_v2 = a.get(new AlloyVariable("v2"));
		assertEquals("Type1", typeOf_v2.trim());
	}

	@Test
	public void complexTyping() throws Exception {
		AlloyTyping e = new AlloyTyping();
		AlloyTyping a = parseAlloyTyping("a: T one -> some S, b:T   -> S, c:S -> lone   T, d:(S+N) -> lone (T+Q), e : S+N");
		
		String typeOf_a = a.get(new AlloyVariable("a"));
		assertEquals("T one -> some S", typeOf_a.trim());

		String typeOf_b = a.get(new AlloyVariable("b"));
		assertEquals("T -> S",typeOf_b.trim());
		
		String typeOf_c = a.get(new AlloyVariable("c"));
		assertEquals("S -> lone T", typeOf_c.trim());
		

		String typeOf_d = a.get(new AlloyVariable("d"));
		assertEquals("( S + N ) -> lone ( T + Q )", typeOf_d.trim());
		
		String typeOf_e = a.get(new AlloyVariable("e"));
		assertEquals("S + N", typeOf_e.trim());	
	}

	@Test
	public void twoArrowsTyping() throws Exception {
		AlloyTyping a = parseAlloyTyping("a: R -> S ->   T");
		String typeOf_a = a.get(new AlloyVariable("a"));
		assertEquals("R -> S -> T",typeOf_a.trim());
	}

	@Test
	public void twoArrowsTypingWithMultiplicity() throws Exception {
		AlloyTyping a = parseAlloyTyping("a: R one -> lone   S  some -> one   T");
		String typeOf_a = a.get(new AlloyVariable("a"));
		assertEquals("R one -> lone S some -> one T", typeOf_a.trim());
	}

	@Test
	public void parsePredicate() throws Exception {
		MockContext ctx = new MockContext();
		ctx.addVariable("var2");
		AlloyFormula a = parseFormula("aPredicate[var2, true]", ctx);
		assertEquals(PredicateFormula.buildPredicate("aPredicate", _var("var2"), _const("true")), a);
	}

	AlloyExpression parseTerm(String s, IAlloyExpressionParseContext ctx)
			throws RecognitionException, TokenStreamException {
		StringReader reader = new StringReader(s);
		ExpressionLexer lexer = new ExpressionLexer(reader);
		ExpressionParser parser = new ExpressionParser(lexer);
		return parser.termExpression(ctx);
	}

	AlloyFormula parseFormula(String s, IAlloyExpressionParseContext ctx)
			throws RecognitionException, TokenStreamException {
		StringReader reader = new StringReader(s);
		ExpressionLexer lexer = new ExpressionLexer(reader);
		ExpressionParser parser = new ExpressionParser(lexer);
		return parser.predicateFormula(ctx);
	}

	AlloyTyping parseAlloyTyping(String s) throws RecognitionException,
			TokenStreamException {
		StringReader reader = new StringReader(s);
		ExpressionLexer lexer = new ExpressionLexer(reader);
		ExpressionParser parser = new ExpressionParser(lexer);
		return parser.variablesDeclaration().getTyping();
	}

	@Test
	public void parseSet() throws Exception {
		AlloyTyping a = parseAlloyTyping("v1:set Type1");
		String typeOf_v1 = a.get(new AlloyVariable("v1"));
		assertEquals("set Type1", typeOf_v1.trim());
	}

	@Test
	public void parseSeq() throws Exception {
		AlloyTyping a = parseAlloyTyping("v1:seq Type1");
		String typeOf_v1 = a.get(new AlloyVariable("v1"));
		assertEquals("seq Type1", typeOf_v1.trim());
	}


}

class MockContext implements IAlloyExpressionParseContext {

	Set<String> variables = new HashSet<String>();

	public boolean isVariableName(String token) {
		return variables.contains(token);
	}

	public void addVariable(String var) {
		variables.add(var);
	}

	public AlloyVariable getAlloyVariable(String token) {
		return new AlloyVariable(token);
	}

	public int getIntLiteral(String token) {
		return Integer.parseInt(token);
	}

	public boolean isIntLiteral(String token) {
		try {
			Integer.parseInt(token);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

}