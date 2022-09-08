// $ANTLR 2.7.6 (2005-12-22): "expression.g" -> "ExpressionParser.java"$

package ar.uba.dc.rfm.alloy.parser;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.expressions.*;
import ar.uba.dc.rfm.alloy.AlloyVariable;

import ar.uba.dc.rfm.alloy.parser.FormalParametersDeclaration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;	

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class ExpressionParser extends antlr.LLkParser       implements ExpressionParserTokenTypes
 {

	public static AlloyExpression buildTerm(IAlloyExpressionParseContext ctx, Token t, Token aliasModuleIdToken) {
		String text = t.getText();
		if (ctx.isIntLiteral(text))
		    return new ExprIntLiteral(ctx.getIntLiteral(text));
		if (ctx.isVariableName(text))
			return new ExprVariable(ctx.getAlloyVariable(text));
		else
			return new ExprConstant(aliasModuleIdToken==null? null: aliasModuleIdToken.getText(), text); 
	}
	
	public static AlloyExpression buildPreTerm(IAlloyExpressionParseContext ctx, Token t) {
    String text = t.getText();
	  return new ExprVariable(AlloyVariable.buildPreStateVariable(text));
	}
	
	
	public static void extendAlloyTyping(FormalParametersDeclaration at, List<String> vars, String t) {
		for (String var : vars) {
			at.put(new AlloyVariable(var), t); 
		}
	}
	
	public static String join(String s1, String s2)
	{
		if ( s1 == null || s1.length() == 0 )
			return s2;
		else if ( s2 == null || s2.length() == 0 )
			return s1;
		else
			return s1 + " " + s2;
	}
	
	public static String relationType(String type1, String mult1, String mult2, String type2)
	{
		return ExpressionParser.join(type1, mult1) + " -> " + ExpressionParser.join(mult2, type2); 
	}
	

protected ExpressionParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public ExpressionParser(TokenBuffer tokenBuf) {
  this(tokenBuf,4);
}

protected ExpressionParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public ExpressionParser(TokenStream lexer) {
  this(lexer,4);
}

public ExpressionParser(ParserSharedInputState state) {
  super(state,4);
  tokenNames = _tokenNames;
}

	public final FormalParametersDeclaration  variablesDeclaration() throws RecognitionException, TokenStreamException {
		FormalParametersDeclaration r;
		
		r = new FormalParametersDeclaration();
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IDENT:
			{
				variablesDeclarationSingleType(r);
				break;
			}
			case EOF:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop4:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					variablesDeclarationSingleType(r);
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return r;
	}
	
	public final void variablesDeclarationSingleType(
		FormalParametersDeclaration r
	) throws RecognitionException, TokenStreamException {
		
		List<String> vars = null; String t = null;
		
		try {      // for error handling
			vars=variables();
			match(COLON);
			t=typeName();
			ExpressionParser.extendAlloyTyping(r, vars, t);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
	}
	
	public final List<String>  variables() throws RecognitionException, TokenStreamException {
		List<String> r;
		
		Token  id = null;
		Token  id2 = null;
		r = new LinkedList<String>();
		
		try {      // for error handling
			id = LT(1);
			match(IDENT);
			r.add(id.getText());
			{
			_loop8:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					id2 = LT(1);
					match(IDENT);
					r.add(id2.getText());
				}
				else {
					break _loop8;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
		return r;
	}
	
	public final String  typeName() throws RecognitionException, TokenStreamException {
		String r;
		
		Token  id = null;
		r = ""; String m=null;
		
		try {      // for error handling
			{
			int _cnt11=0;
			_loop11:
			do {
				switch ( LA(1)) {
				case LITERAL_lone:
				case LITERAL_one:
				case LITERAL_some:
				case LITERAL_seq:
				case LITERAL_set:
				{
					m=multiplicity();
					r=r+=" " + m;
					break;
				}
				case LPAREN:
				{
					match(LPAREN);
					r=r+=" (";
					break;
				}
				case RPAREN:
				{
					match(RPAREN);
					r=r+=" )";
					break;
				}
				case RARROW:
				{
					match(RARROW);
					r=r+=" ->";
					break;
				}
				case PLUS:
				{
					match(PLUS);
					r=r+=" +";
					break;
				}
				case IDENT:
				{
					id = LT(1);
					match(IDENT);
					r=r+=" " + id.getText();
					break;
				}
				default:
				{
					if ( _cnt11>=1 ) { break _loop11; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt11++;
			} while (true);
			}
			
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_1);
		}
		return r;
	}
	
	public final String  multiplicity() throws RecognitionException, TokenStreamException {
		String r;
		
		r = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_lone:
			{
				match(LITERAL_lone);
				r = "lone";
				break;
			}
			case LITERAL_one:
			{
				match(LITERAL_one);
				r = "one";
				break;
			}
			case LITERAL_some:
			{
				match(LITERAL_some);
				r = "some";
				break;
			}
			case LITERAL_seq:
			{
				match(LITERAL_seq);
				r = "seq";
				break;
			}
			case LITERAL_set:
			{
				match(LITERAL_set);
				r = "set";
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return r;
	}
	
	public final PredicateFormula  predicateFormula(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		PredicateFormula r;
		
		Token  aliasModuleId = null;
		Token  pred = null;
		r = null; List<AlloyExpression> args = null;
		
		try {      // for error handling
			{
			if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
				aliasModuleId = LT(1);
				match(IDENT);
				match(SLASH);
			}
			else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			pred = LT(1);
			match(IDENT);
			match(LBRACKET);
			args=actualParams(ctx);
			match(RBRACKET);
			r = new PredicateFormula(aliasModuleId==null?null:aliasModuleId.getText(),pred.getText(), args);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
		return r;
	}
	
	public final List<AlloyExpression>  actualParams(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		List<AlloyExpression> r;
		
		r = new LinkedList<AlloyExpression>(); AlloyExpression p;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case IDENT:
			case LPAREN:
			case NUMBER:
			case BACKSLASH_PRE:
			{
				p=termExpression(ctx);
				r.add(p);
				break;
			}
			case COMMA:
			case RBRACKET:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop18:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					p=termExpression(ctx);
					r.add(p);
				}
				else {
					break _loop18;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		return r;
	}
	
	public final AlloyExpression  termExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		try {      // for error handling
			r=joinExpression(ctx);
			{
			if ((LA(1)==PLUS) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(PLUS);
				other=termExpression(ctx);
				r = new ExprUnion(r, other);
			}
			else if ((LA(1)==EOF) && (_tokenSet_8.member(LA(2))) && (_tokenSet_9.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(Token.EOF_TYPE);
			}
			else if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2))) && (_tokenSet_7.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  joinExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		try {      // for error handling
			r=productExpression(ctx);
			{
			if ((LA(1)==DOT) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(DOT);
				other=joinExpression(ctx);
				r = new ExprJoin(r, other);
			}
			else if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2))) && (_tokenSet_7.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  productExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		try {      // for error handling
			r=overrideExpression(ctx);
			{
			if ((LA(1)==RARROW) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(RARROW);
				other=productExpression(ctx);
				r = new ExprProduct(r, other);
			}
			else if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2))) && (_tokenSet_7.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  overrideExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		try {      // for error handling
			r=exprIntersectionExpression(ctx);
			{
			if ((LA(1)==PLUSPLUS) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(PLUSPLUS);
				other=overrideExpression(ctx);
				r = new ExprOverride(r, other);
			}
			else if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2))) && (_tokenSet_7.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  exprIntersectionExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		r = null; AlloyExpression other = null;
		
		try {      // for error handling
			r=atomicExpression(ctx);
			{
			if ((LA(1)==AMP) && (_tokenSet_5.member(LA(2))) && (_tokenSet_6.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
				match(AMP);
				other=exprIntersectionExpression(ctx);
				r = new ExprIntersection(r, other);
			}
			else if ((_tokenSet_8.member(LA(1))) && (_tokenSet_9.member(LA(2))) && (_tokenSet_7.member(LA(3))) && (_tokenSet_7.member(LA(4)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  atomicExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		Token  id = null;
		Token  number = null;
		Token  id2 = null;
		r = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case NUMBER:
			{
				number = LT(1);
				match(NUMBER);
				r = ExpressionParser.buildTerm(ctx, number, null);
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				r=termExpression(ctx);
				match(RPAREN);
				break;
			}
			case BACKSLASH_PRE:
			{
				match(BACKSLASH_PRE);
				match(LBRACKET);
				id2 = LT(1);
				match(IDENT);
				match(RBRACKET);
				r = ExpressionParser.buildPreTerm(ctx, id2);
				break;
			}
			default:
				if ((LA(1)==IDENT) && (_tokenSet_8.member(LA(2)))) {
					id = LT(1);
					match(IDENT);
					r = ExpressionParser.buildTerm(ctx, id, null);
				}
				else if ((LA(1)==IDENT) && (LA(2)==SLASH||LA(2)==LBRACKET)) {
					r=functionOrIfExpression(ctx);
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	public final AlloyExpression  functionOrIfExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		Token  aliasModuleId = null;
		Token  functionId = null;
		r = null; List<AlloyExpression> args = null; AlloyExpression thenPart = null; AlloyExpression elsePart = null; boolean isIfExpression = false;
		
		try {      // for error handling
			{
			if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
				aliasModuleId = LT(1);
				match(IDENT);
				match(SLASH);
			}
			else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			functionId = LT(1);
			match(IDENT);
			match(LBRACKET);
			args=actualParams(ctx);
			match(RBRACKET);
			{
			switch ( LA(1)) {
			case IF_EXPR:
			{
				match(IF_EXPR);
				thenPart=termExpression(ctx);
				match(ELSE);
				elsePart=termExpression(ctx);
				isIfExpression = true;
				break;
			}
			case EOF:
			case COMMA:
			case RPAREN:
			case RARROW:
			case PLUS:
			case RBRACKET:
			case DOT:
			case PLUSPLUS:
			case AMP:
			case ELSE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
					if (isIfExpression) {
					  PredicateFormula pred = new PredicateFormula(aliasModuleId==null?null:aliasModuleId.getText(),functionId.getText(), args);
					  r = new ExprIfCondition(pred, thenPart, elsePart);
					} else {
						r = new ExprFunction(aliasModuleId==null ? null : aliasModuleId.getText(),functionId.getText(), args); 
					} 
				
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_8);
		}
		return r;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"COMMA",
		"COLON",
		"IDENT",
		"LPAREN",
		"RPAREN",
		"RARROW",
		"PLUS",
		"\"lone\"",
		"\"one\"",
		"\"some\"",
		"\"seq\"",
		"\"set\"",
		"SLASH",
		"LBRACKET",
		"RBRACKET",
		"DOT",
		"PLUSPLUS",
		"AMP",
		"NUMBER",
		"BACKSLASH_PRE",
		"IF_EXPR",
		"ELSE",
		"VAL",
		"SEMICOLON",
		"EQUALS",
		"STAR",
		"MINUS",
		"LBRACE",
		"RBRACE",
		"COMMENT",
		"COMMENT_SLASH_SLASH",
		"COMMENT_ML",
		"WS",
		"MODULE_ID"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 18L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 32L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 65490L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 262144L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 12583104L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 50268114L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 67045330L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 37488402L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 66848722L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	
	}
