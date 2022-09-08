package ar.uba.dc.rfm.alloy.parser;

import java.io.StringReader;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public class ParseExpressionTest {

	@Test
	public void testIntersection() throws RecognitionException,
			TokenStreamException {
		String s = "Int & 15";

		MockContext ctx = new MockContext();
		StringReader reader = new StringReader(s);
		ExpressionLexer lexer = new ExpressionLexer(reader);
		ExpressionParser parser = new ExpressionParser(lexer);
		AlloyExpression expr = parser.termExpression(ctx);

	}
	
	@Test
	public void testIntersection_Var() throws RecognitionException,
			TokenStreamException {
		String s = "x & 15";

		MockContext ctx = new MockContext();
		ctx.addVariable("x");
		StringReader reader = new StringReader(s);
		ExpressionLexer lexer = new ExpressionLexer(reader);
		ExpressionParser parser = new ExpressionParser(lexer);
		AlloyExpression expr = parser.termExpression(ctx);

	}
	

}
