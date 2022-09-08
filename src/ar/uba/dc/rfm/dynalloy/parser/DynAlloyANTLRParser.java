// $ANTLR 2.7.6 (2005-12-22): "expandeddynalloy.g" -> "DynAlloyANTLRParser.java"$

package ar.uba.dc.rfm.dynalloy.parser;

import java.util.ArrayList;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.dynalloy.ast.*;	
import ar.uba.dc.rfm.dynalloy.ast.programs.*;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;
import ar.uba.dc.rfm.alloy.ast.expressions.*;
import ar.uba.dc.rfm.alloy.parser.*;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.TestPredicateLabel;
import java.io.StringReader;
import java.util.Collections;

import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;		

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
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class DynAlloyANTLRParser extends antlr.LLkParser       implements DynAlloyANTLRParserTokenTypes
 {

	private AlloyTyping fields = new AlloyTyping();
	private Position initialPosition;
	
	public void bindFields(AlloyTyping _fields) {
		fields = _fields;
	}
	
	public AlloyTyping getFields() {
		return fields;
	}
	
	private Position positionFromAST() {
		AST ast = getAST();
		return (new Position(ast.getLine(), ast.getColumn()))
			.add(initialPosition);
	}
	
	private Position ifTokenPosition() {
		AST ast = DynalloyAST.pollIfToken();
		return (new Position(ast.getLine(), ast.getColumn()))
			.add(initialPosition);
	}

		private Position whileTokenPosition() {
		AST ast = DynalloyAST.pollWhileToken();
		return (new Position(ast.getLine(), ast.getColumn()))
			.add(initialPosition);
	}
	
	public void setInitialPosition(Position initialPosition) {
		this.initialPosition = initialPosition;
	}
	

protected DynAlloyANTLRParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public DynAlloyANTLRParser(TokenBuffer tokenBuf) {
  this(tokenBuf,4);
}

protected DynAlloyANTLRParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public DynAlloyANTLRParser(TokenStream lexer) {
  this(lexer,4);
}

public DynAlloyANTLRParser(ParserSharedInputState state) {
  super(state,4);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void alloySignature(
		
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST alloySignature_AST = null;
		Token  signatureId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST signatureId_AST = null;
		FormalParametersDeclaration formalParams=null;
		
		{
		switch ( LA(1)) {
		case LITERAL_abstract:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp1_AST = null;
			tmp1_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp1_AST);
			match(LITERAL_abstract);
			break;
		}
		case EOF:
		case LITERAL_one:
		case LITERAL_sig:
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
		switch ( LA(1)) {
		case LITERAL_one:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp2_AST = null;
			tmp2_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp2_AST);
			match(LITERAL_one);
			break;
		}
		case EOF:
		case LITERAL_sig:
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
		switch ( LA(1)) {
		case LITERAL_sig:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp3_AST = null;
			tmp3_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp3_AST);
			match(LITERAL_sig);
			signatureId = LT(1);
			signatureId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(signatureId);
			astFactory.addASTChild(currentAST, signatureId_AST);
			match(IDENT);
			{
			switch ( LA(1)) {
			case LITERAL_extends:
			case LITERAL_in:
			{
				{
				switch ( LA(1)) {
				case LITERAL_extends:
				{
					ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp4_AST = null;
					tmp4_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp4_AST);
					match(LITERAL_extends);
					break;
				}
				case LITERAL_in:
				{
					ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp5_AST = null;
					tmp5_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp5_AST);
					match(LITERAL_in);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				typeName();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case LBRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp6_AST = null;
			tmp6_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp6_AST);
			match(LBRACE);
			formalParams=variablesDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp7_AST = null;
			tmp7_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp7_AST);
			match(RBRACE);
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		
				if (formalParams!=null) {
			  		for (AlloyVariable v: formalParams.getTyping()) {
			  	   	//if (fields.contains(v))
			  	   	//	throw new AmbiguosFieldDeclarationException("cannot handle multiple field declarations.");
			  	   	fields.put(v,signatureId.getText() + "->" + "(" + formalParams.getTyping().get(v) + ")");
			  		}
				}
			
		alloySignature_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = alloySignature_AST;
	}
	
	public final String  typeName() throws RecognitionException, TokenStreamException {
		String r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST typeName_AST = null;
		Token  id = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST id_AST = null;
		r = ""; String m=null;
		
		{
		int _cnt50=0;
		_loop50:
		do {
			switch ( LA(1)) {
			case LITERAL_lone:
			case LITERAL_one:
			case LITERAL_some:
			case LITERAL_seq:
			case LITERAL_set:
			{
				m=multiplicity();
				astFactory.addASTChild(currentAST, returnAST);
				r=r+=" " + m;
				break;
			}
			case LPAREN:
			{
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp8_AST = null;
				tmp8_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp8_AST);
				match(LPAREN);
				r=r+=" (";
				break;
			}
			case RPAREN:
			{
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp9_AST = null;
				tmp9_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp9_AST);
				match(RPAREN);
				r=r+=" )";
				break;
			}
			case RARROW:
			{
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp10_AST = null;
				tmp10_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp10_AST);
				match(RARROW);
				r=r+=" ->";
				break;
			}
			case PLUS:
			{
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp11_AST = null;
				tmp11_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp11_AST);
				match(PLUS);
				r=r+=" +";
				break;
			}
			case IDENT:
			{
				id = LT(1);
				id_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(id);
				astFactory.addASTChild(currentAST, id_AST);
				match(IDENT);
				r=r+=" " + id.getText();
				break;
			}
			default:
			{
				if ( _cnt50>=1 ) { break _loop50; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			}
			_cnt50++;
		} while (true);
		}
		
			
		typeName_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = typeName_AST;
		return r;
	}
	
	public final FormalParametersDeclaration  variablesDeclaration() throws RecognitionException, TokenStreamException {
		FormalParametersDeclaration r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST variablesDeclaration_AST = null;
		r = new FormalParametersDeclaration();
		
		{
		switch ( LA(1)) {
		case IDENT:
		{
			variablesDeclarationSingleType(r);
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case COMMA:
		case RBRACKET:
		case RBRACE:
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
		_loop43:
		do {
			if ((LA(1)==COMMA)) {
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp12_AST = null;
				tmp12_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp12_AST);
				match(COMMA);
				variablesDeclarationSingleType(r);
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop43;
			}
			
		} while (true);
		}
		variablesDeclaration_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = variablesDeclaration_AST;
		return r;
	}
	
	public final String  alloyModule() throws RecognitionException, TokenStreamException {
		String moduleId;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST alloyModule_AST = null;
		
		
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp13_AST = null;
		tmp13_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp13_AST);
		match(LITERAL_module);
		moduleId=modulePath();
		astFactory.addASTChild(currentAST, returnAST);
		alloyModule_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = alloyModule_AST;
		return moduleId;
	}
	
	public final String  modulePath() throws RecognitionException, TokenStreamException {
		String path;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST modulePath_AST = null;
		Token  i = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST i_AST = null;
		Token  j = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST j_AST = null;
		
		
		i = LT(1);
		i_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(i);
		astFactory.addASTChild(currentAST, i_AST);
		match(IDENT);
		path = i.getText();
		{
		_loop12:
		do {
			if ((LA(1)==SLASH)) {
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp14_AST = null;
				tmp14_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp14_AST);
				match(SLASH);
				j = LT(1);
				j_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(j);
				astFactory.addASTChild(currentAST, j_AST);
				match(IDENT);
				path = path + "/" + j.getText();
			}
			else {
				break _loop12;
			}
			
		} while (true);
		}
		modulePath_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = modulePath_AST;
		return path;
	}
	
	public final OpenDeclaration  alloyOpen() throws RecognitionException, TokenStreamException {
		OpenDeclaration r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST alloyOpen_AST = null;
		Token  aliasModuleId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId_AST = null;
		String moduleId = null; String alias = null;
		
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp15_AST = null;
		tmp15_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp15_AST);
		match(LITERAL_open);
		moduleId=modulePath();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case LITERAL_as:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp16_AST = null;
			tmp16_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(LITERAL_as);
			aliasModuleId = LT(1);
			aliasModuleId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(aliasModuleId);
			astFactory.addASTChild(currentAST, aliasModuleId_AST);
			match(IDENT);
			alias = aliasModuleId.getText();
			break;
		}
		case EOF:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		
				r = new OpenDeclaration(moduleId, alias);
			
		alloyOpen_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = alloyOpen_AST;
		return r;
	}
	
	public final void typescope(
		AlloyCheckCommand ctx
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST typescope_AST = null;
		Token  scope = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST scope_AST = null;
		Token  signature = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST signature_AST = null;
		boolean isExactly = false;
		
		{
		switch ( LA(1)) {
		case LITERAL_exactly:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp17_AST = null;
			tmp17_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(LITERAL_exactly);
			isExactly = true;
			break;
		}
		case NUMBER:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		scope = LT(1);
		scope_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(scope);
		astFactory.addASTChild(currentAST, scope_AST);
		match(NUMBER);
		signature = LT(1);
		signature_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(signature);
		astFactory.addASTChild(currentAST, signature_AST);
		match(IDENT);
		ctx.put(signature.getText(), isExactly, Integer.parseInt(scope.getText()));
		typescope_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = typescope_AST;
	}
	
	public final ActionDeclaration  dynalloyAction() throws RecognitionException, TokenStreamException {
		ActionDeclaration r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST dynalloyAction_AST = null;
		Token  actionId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST actionId_AST = null;
		
				r = null;
				FormalParametersDeclaration formalParams;
			  	PredicateFormula pre = null, post = null;				
			
		
		{
		switch ( LA(1)) {
		case LITERAL_action:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp18_AST = null;
			tmp18_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(LITERAL_action);
			break;
		}
		case LITERAL_act:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp19_AST = null;
			tmp19_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp19_AST);
			match(LITERAL_act);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		actionId = LT(1);
		actionId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(actionId);
		astFactory.addASTChild(currentAST, actionId_AST);
		match(IDENT);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp20_AST = null;
		tmp20_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp20_AST);
		match(LBRACKET);
		formalParams=variablesDeclaration();
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp21_AST = null;
		tmp21_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp21_AST);
		match(RBRACKET);
		
					Set<AlloyVariable> variables = formalParams.getTyping().varSet();
				    IAlloyExpressionParseContext ctxPre = new DynalloyProgramParseContext(variables,Collections.<AlloyVariable>emptySet(),false);
				    IAlloyExpressionParseContext ctxPost = new DynalloyProgramParseContext(variables,Collections.<AlloyVariable>emptySet(),true);	  	 			
				
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp22_AST = null;
		tmp22_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp22_AST);
		match(LBRACE);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp23_AST = null;
		tmp23_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp23_AST);
		match(LITERAL_pre);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp24_AST = null;
		tmp24_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp24_AST);
		match(LBRACE);
		pre=predicateFormula(ctxPre);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp25_AST = null;
		tmp25_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp25_AST);
		match(RBRACE);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp26_AST = null;
		tmp26_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp26_AST);
		match(LITERAL_post);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp27_AST = null;
		tmp27_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp27_AST);
		match(LBRACE);
		post=predicateFormula(ctxPost);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp28_AST = null;
		tmp28_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp28_AST);
		match(RBRACE);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp29_AST = null;
		tmp29_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp29_AST);
		match(RBRACE);
		
				r = new ActionDeclaration(actionId.getText(),
					 formalParams.getParameters(),
					 pre, post, formalParams.getTyping());
			
		dynalloyAction_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = dynalloyAction_AST;
		return r;
	}
	
	public final PredicateFormula  predicateFormula(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		PredicateFormula r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST predicateFormula_AST = null;
		Token  aliasModuleId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId_AST = null;
		Token  pred = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST pred_AST = null;
		r = null; List<AlloyExpression> args = null;
		
		{
		if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
			aliasModuleId = LT(1);
			aliasModuleId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(aliasModuleId);
			astFactory.addASTChild(currentAST, aliasModuleId_AST);
			match(IDENT);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp30_AST = null;
			tmp30_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp30_AST);
			match(SLASH);
		}
		else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		pred = LT(1);
		pred_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(pred);
		astFactory.addASTChild(currentAST, pred_AST);
		match(IDENT);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp31_AST = null;
		tmp31_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp31_AST);
		match(LBRACKET);
		args=actualParams(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp32_AST = null;
		tmp32_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp32_AST);
		match(RBRACKET);
		r = new PredicateFormula(aliasModuleId==null?null:aliasModuleId.getText(),pred.getText(), args);
		predicateFormula_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = predicateFormula_AST;
		return r;
	}
	
	public final ProgramDeclaration  dynalloyProgram() throws RecognitionException, TokenStreamException {
		ProgramDeclaration r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST dynalloyProgram_AST = null;
		Token  programId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST programId_AST = null;
		
				r = null;
				FormalParametersDeclaration formalParams;
				FormalParametersDeclaration localVariables = null;
			  	DynalloyProgram prog;	
			  	Set<AlloyVariable> ctxVariables = null;			
			
		
		{
		switch ( LA(1)) {
		case LITERAL_program:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp33_AST = null;
			tmp33_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp33_AST);
			match(LITERAL_program);
			break;
		}
		case LITERAL_prog:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp34_AST = null;
			tmp34_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp34_AST);
			match(LITERAL_prog);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		programId = LT(1);
		programId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(programId);
		astFactory.addASTChild(currentAST, programId_AST);
		match(IDENT);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp35_AST = null;
		tmp35_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp35_AST);
		match(LBRACKET);
		formalParams=variablesDeclaration();
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp36_AST = null;
		tmp36_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp36_AST);
		match(RBRACKET);
		{
		switch ( LA(1)) {
		case LITERAL_var:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp37_AST = null;
			tmp37_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp37_AST);
			match(LITERAL_var);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp38_AST = null;
			tmp38_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp38_AST);
			match(LBRACKET);
			localVariables=variablesDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp39_AST = null;
			tmp39_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp39_AST);
			match(RBRACKET);
			break;
		}
		case LBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		
					ctxVariables = new HashSet<AlloyVariable>();
					ctxVariables.addAll(formalParams.getTyping().varSet());
					if (localVariables!=null)
								ctxVariables.addAll(localVariables.getTyping().varSet());
			      IDynalloyProgramParseContext ctxProgram = new DynalloyProgramParseContext(ctxVariables,Collections.<AlloyVariable>emptySet(),false);
				
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp40_AST = null;
		tmp40_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp40_AST);
		match(LBRACE);
		prog=program(ctxProgram);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp41_AST = null;
		tmp41_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp41_AST);
		match(RBRACE);
		
				AlloyTyping typing = new AlloyTyping();
				for(AlloyVariable v: formalParams.getTyping().varSet()) {
					typing.put(v,formalParams.getTyping().get(v));
				}
				if (localVariables!=null) {
				   for(AlloyVariable v: localVariables.getTyping().varSet()) {
					typing.put(v,localVariables.getTyping().get(v));
				   }
				}
				
				r = new ProgramDeclaration(programId.getText(),
					 formalParams.getParameters(),
					 localVariables == null ? Collections.<VariableId>emptyList() : localVariables.getParameters(),
					 prog, typing, new ArrayList<AlloyFormula>(), new AlloyTyping());
			
		dynalloyProgram_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = dynalloyProgram_AST;
		return r;
	}
	
	public final DynalloyProgram  program(
		IDynalloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		DynalloyProgram r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST program_AST = null;
		r = null;
		
		r=choiceProgram(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case EOF:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp42_AST = null;
			tmp42_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp42_AST);
			match(Token.EOF_TYPE);
			break;
		}
		case RBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		program_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = program_AST;
		return r;
	}
	
	public final AssertionDeclaration  dynalloyAssertion() throws RecognitionException, TokenStreamException {
		AssertionDeclaration r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST dynalloyAssertion_AST = null;
		Token  assertionId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST assertionId_AST = null;
		
			  r = null; 
			  FormalParametersDeclaration formalParams;
			  PredicateFormula pre = null, post = null;
			  DynalloyProgram prog = null;
			
		
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp43_AST = null;
		tmp43_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp43_AST);
		match(LITERAL_assertCorrectness);
		assertionId = LT(1);
		assertionId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(assertionId);
		astFactory.addASTChild(currentAST, assertionId_AST);
		match(IDENT);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp44_AST = null;
		tmp44_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp44_AST);
		match(LBRACKET);
		formalParams=variablesDeclaration();
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp45_AST = null;
		tmp45_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp45_AST);
		match(RBRACKET);
		
			  	Set<AlloyVariable> variables = formalParams.getTyping().varSet();
			    IAlloyExpressionParseContext ctxPre = new DynalloyProgramParseContext(variables,Collections.<AlloyVariable>emptySet(),false);
			    IDynalloyProgramParseContext ctxProgram = new DynalloyProgramParseContext(variables,Collections.<AlloyVariable>emptySet(),false);
			    IAlloyExpressionParseContext ctxPost = new DynalloyProgramParseContext(variables,Collections.<AlloyVariable>emptySet(),true);
			    Position prePosition = null;
			    Position postPosition = null;	  	 
			
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp46_AST = null;
		tmp46_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp46_AST);
		match(LBRACE);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp47_AST = null;
		tmp47_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp47_AST);
		match(LITERAL_pre);
		{
		switch ( LA(1)) {
		case EQUALS:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp48_AST = null;
			tmp48_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp48_AST);
			match(EQUALS);
			break;
		}
		case LBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp49_AST = null;
		tmp49_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp49_AST);
		match(LBRACE);
		pre=predicateFormula(ctxPre);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp50_AST = null;
		tmp50_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp50_AST);
		match(RBRACE);
		prePosition = positionFromAST();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp51_AST = null;
		tmp51_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp51_AST);
		match(LITERAL_program);
		{
		switch ( LA(1)) {
		case EQUALS:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp52_AST = null;
			tmp52_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp52_AST);
			match(EQUALS);
			break;
		}
		case LBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp53_AST = null;
		tmp53_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp53_AST);
		match(LBRACE);
		prog=program(ctxProgram);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp54_AST = null;
		tmp54_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp54_AST);
		match(RBRACE);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp55_AST = null;
		tmp55_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp55_AST);
		match(LITERAL_post);
		{
		switch ( LA(1)) {
		case EQUALS:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp56_AST = null;
			tmp56_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp56_AST);
			match(EQUALS);
			break;
		}
		case LBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp57_AST = null;
		tmp57_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp57_AST);
		match(LBRACE);
		post=predicateFormula(ctxPost);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp58_AST = null;
		tmp58_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp58_AST);
		match(RBRACE);
		postPosition = positionFromAST();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp59_AST = null;
		tmp59_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp59_AST);
		match(RBRACE);
		
			    pre.setPosition(prePosition);
			    post.setPosition(postPosition);
			  	r = new AssertionDeclaration(assertionId.getText(), formalParams.getTyping(), pre, prog, post);
			
		dynalloyAssertion_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = dynalloyAssertion_AST;
		return r;
	}
	
	public final DynalloyProgram  choiceProgram(
		IDynalloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		DynalloyProgram r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST choiceProgram_AST = null;
		r = null; DynalloyProgram other = null;
		
		r=compositionProgram(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case PLUS:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp60_AST = null;
			tmp60_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp60_AST);
			match(PLUS);
			other=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new Choice(r, other, positionFromAST());
			break;
		}
		case EOF:
		case RPAREN:
		case RBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		choiceProgram_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = choiceProgram_AST;
		return r;
	}
	
	public final DynalloyProgram  compositionProgram(
		IDynalloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		DynalloyProgram r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST compositionProgram_AST = null;
		r = null; DynalloyProgram other = null;
		
		r=closureProgram(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case SEMICOLON:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp61_AST = null;
			tmp61_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp61_AST);
			match(SEMICOLON);
			other=compositionProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new Composition(r, other, positionFromAST());
			break;
		}
		case EOF:
		case RPAREN:
		case PLUS:
		case RBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		compositionProgram_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = compositionProgram_AST;
		return r;
	}
	
	public final DynalloyProgram  closureProgram(
		IDynalloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		DynalloyProgram r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST closureProgram_AST = null;
		r = null;
		
		r=atomicPrograms(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case STAR:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp62_AST = null;
			tmp62_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp62_AST);
			match(STAR);
			r = new Closure(r, positionFromAST());
			break;
		}
		case EOF:
		case RPAREN:
		case PLUS:
		case SEMICOLON:
		case RBRACE:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		closureProgram_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = closureProgram_AST;
		return r;
	}
	
	public final DynalloyProgram  atomicPrograms(
		IDynalloyProgramParseContext ctx
	) throws RecognitionException, TokenStreamException {
		DynalloyProgram r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST atomicPrograms_AST = null;
		Token  aliasModuleId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId_AST = null;
		Token  actionId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST actionId_AST = null;
		Token  aliasModuleId2 = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId2_AST = null;
		Token  programId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST programId_AST = null;
		Token  variableId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST variableId_AST = null;
		
			  r = null; 
			  List<AlloyExpression> p; 
			  PredicateFormula pred;  
			  DynalloyProgram other = null; 
			  AlloyExpression expr=null; 
			  TestPredicateLabel label = null; 
			
		
		switch ( LA(1)) {
		case LITERAL_skip:
		{
			skipProgram();
			astFactory.addASTChild(currentAST, returnAST);
			r = new Skip(positionFromAST());
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LBRACKET:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp63_AST = null;
			tmp63_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp63_AST);
			match(LBRACKET);
			pred=predicateFormula(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp64_AST = null;
			tmp64_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp64_AST);
			match(RBRACKET);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp65_AST = null;
			tmp65_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp65_AST);
			match(QUESTION);
			r = new TestPredicate(pred, positionFromAST());
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_call:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp66_AST = null;
			tmp66_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp66_AST);
			match(LITERAL_call);
			{
			if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
				aliasModuleId2 = LT(1);
				aliasModuleId2_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(aliasModuleId2);
				astFactory.addASTChild(currentAST, aliasModuleId2_AST);
				match(IDENT);
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp67_AST = null;
				tmp67_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp67_AST);
				match(SLASH);
			}
			else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			programId = LT(1);
			programId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(programId);
			astFactory.addASTChild(currentAST, programId_AST);
			match(IDENT);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp68_AST = null;
			tmp68_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp68_AST);
			match(LBRACKET);
			p=actualParams(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp69_AST = null;
			tmp69_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp69_AST);
			match(RBRACKET);
			r = new InvokeProgram(aliasModuleId2==null ? null : aliasModuleId2.getText(), programId.getText(), p, positionFromAST());
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LPAREN:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp70_AST = null;
			tmp70_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp70_AST);
			match(LPAREN);
			r=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp71_AST = null;
			tmp71_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp71_AST);
			match(RPAREN);
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_assume:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp72_AST = null;
			tmp72_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp72_AST);
			match(LITERAL_assume);
			pred=predicateFormula(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case LITERAL_lblneg:
			case LITERAL_lblpos:
			{
				label=testPredLabel();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EOF:
			case RPAREN:
			case PLUS:
			case SEMICOLON:
			case STAR:
			case RBRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				          TestPredicate t = new TestPredicate(pred, positionFromAST()); 
				          if (label!=null) {
				            t.setLabel(label);
				          }
				          r = t;
				
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_while:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp73_AST = null;
			tmp73_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp73_AST);
			match(LITERAL_while);
			pred=predicateFormula(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp74_AST = null;
			tmp74_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp74_AST);
			match(LITERAL_do);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp75_AST = null;
			tmp75_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp75_AST);
			match(LBRACE);
			r=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp76_AST = null;
			tmp76_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp76_AST);
			match(RBRACE);
			
				       Position while_pos = whileTokenPosition();
				       TestPredicate t1 = new TestPredicate(pred, while_pos);
				       TestPredicate t2 = new TestPredicate(pred, false, while_pos);
					   //r = new Composition(new Closure(new Composition(t1,r, positionFromAST()), positionFromAST()), t2, positionFromAST()); 
				       
					   r = new WhileProgram(pred, r, positionFromAST()); 
					
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_repeat:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp77_AST = null;
			tmp77_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp77_AST);
			match(LITERAL_repeat);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp78_AST = null;
			tmp78_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp78_AST);
			match(LBRACE);
			r=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp79_AST = null;
			tmp79_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp79_AST);
			match(RBRACE);
			r = new Closure(r, positionFromAST());
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_if:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp80_AST = null;
			tmp80_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp80_AST);
			match(LITERAL_if);
			pred=predicateFormula(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case LITERAL_lblneg:
			case LITERAL_lblpos:
			{
				label=testPredLabel();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case LBRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp81_AST = null;
			tmp81_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp81_AST);
			match(LBRACE);
			r=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp82_AST = null;
			tmp82_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp82_AST);
			match(RBRACE);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp83_AST = null;
			tmp83_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp83_AST);
			match(ELSE);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp84_AST = null;
			tmp84_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp84_AST);
			match(LBRACE);
			other=choiceProgram(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp85_AST = null;
			tmp85_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp85_AST);
			match(RBRACE);
			
				       	 Position ifPos = ifTokenPosition();
				       	 TestPredicate t1 = new TestPredicate(pred, ifPos);
			
			if (label!=null) {
			t1.setLabel(label);
			}
			
				       	 r = new Composition(t1,r, positionFromAST());
				       	  
				       	 TestPredicate t2 = new TestPredicate(pred, false, ifPos);
			
			if (label!=null) {
			TestPredicateLabel label_false;
			label_false = new TestPredicateLabel(!label.isLblpos(), label.getLabelId()); 
			t2.setLabel(label_false);
			}
			
				       	 other = new Composition(t2,other, positionFromAST());
				       	 
				       	 r = new Choice(r,other, positionFromAST());
				
			atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		default:
			if ((LA(1)==IDENT) && (LA(2)==SLASH||LA(2)==LBRACKET)) {
				{
				if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
					aliasModuleId = LT(1);
					aliasModuleId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(aliasModuleId);
					astFactory.addASTChild(currentAST, aliasModuleId_AST);
					match(IDENT);
					ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp86_AST = null;
					tmp86_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp86_AST);
					match(SLASH);
				}
				else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				actionId = LT(1);
				actionId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(actionId);
				astFactory.addASTChild(currentAST, actionId_AST);
				match(IDENT);
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp87_AST = null;
				tmp87_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp87_AST);
				match(LBRACKET);
				p=actualParams(ctx);
				astFactory.addASTChild(currentAST, returnAST);
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp88_AST = null;
				tmp88_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp88_AST);
				match(RBRACKET);
				r = new InvokeAction(aliasModuleId==null ? null : aliasModuleId.getText(), actionId.getText(), p, positionFromAST());
				atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			}
			else if ((LA(1)==IDENT) && (LA(2)==ASSIGNMENT)) {
				variableId = LT(1);
				variableId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(variableId);
				astFactory.addASTChild(currentAST, variableId_AST);
				match(IDENT);
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp89_AST = null;
				tmp89_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp89_AST);
				match(ASSIGNMENT);
				expr=termExpression(ctx);
				astFactory.addASTChild(currentAST, returnAST);
				r = new Assigment(ExprVariable.buildExprVariable(variableId.getText()),expr,positionFromAST());
				atomicPrograms_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = atomicPrograms_AST;
		return r;
	}
	
	public final TestPredicateLabel  testPredLabel() throws RecognitionException, TokenStreamException {
		TestPredicateLabel r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST testPredLabel_AST = null;
		Token  labelId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST labelId_AST = null;
		
		r = null;
		boolean isLblpos = false;
		
		
		{
		switch ( LA(1)) {
		case LITERAL_lblneg:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp90_AST = null;
			tmp90_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp90_AST);
			match(LITERAL_lblneg);
			isLblpos = false;
			break;
		}
		case LITERAL_lblpos:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp91_AST = null;
			tmp91_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp91_AST);
			match(LITERAL_lblpos);
			isLblpos = true;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		labelId = LT(1);
		labelId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(labelId);
		astFactory.addASTChild(currentAST, labelId_AST);
		match(IDENT);
		
		r = new TestPredicateLabel(isLblpos, labelId.getText());
		
		testPredLabel_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = testPredLabel_AST;
		return r;
	}
	
	public final void skipProgram() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST skipProgram_AST = null;
		
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp92_AST = null;
		tmp92_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp92_AST);
		match(LITERAL_skip);
		skipProgram_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = skipProgram_AST;
	}
	
	public final List<AlloyExpression>  actualParams(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		List<AlloyExpression> r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST actualParams_AST = null;
		r = new LinkedList<AlloyExpression>(); AlloyExpression p;
		
		{
		switch ( LA(1)) {
		case IDENT:
		case LPAREN:
		case NUMBER:
		case BACKSLASH_PRE:
		{
			p=termExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
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
		_loop57:
		do {
			if ((LA(1)==COMMA)) {
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp93_AST = null;
				tmp93_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp93_AST);
				match(COMMA);
				p=termExpression(ctx);
				astFactory.addASTChild(currentAST, returnAST);
				r.add(p);
			}
			else {
				break _loop57;
			}
			
		} while (true);
		}
		actualParams_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = actualParams_AST;
		return r;
	}
	
	public final AlloyExpression  termExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST termExpression_AST = null;
		r = null; AlloyExpression other = null;
		
		r=joinExpression(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==PLUS) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3))) && (_tokenSet_2.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp94_AST = null;
			tmp94_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp94_AST);
			match(PLUS);
			other=termExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new ExprUnion(r, other);
		}
		else if ((LA(1)==EOF) && (_tokenSet_3.member(LA(2))) && (_tokenSet_4.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp95_AST = null;
			tmp95_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp95_AST);
			match(Token.EOF_TYPE);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		termExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = termExpression_AST;
		return r;
	}
	
	public final void variablesDeclarationSingleType(
		FormalParametersDeclaration r
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST variablesDeclarationSingleType_AST = null;
		List<String> vars = null; String t = null;
		
		vars=variables();
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp96_AST = null;
		tmp96_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp96_AST);
		match(COLON);
		t=typeName();
		astFactory.addASTChild(currentAST, returnAST);
		ExpressionParser.extendAlloyTyping(r, vars, t);
		variablesDeclarationSingleType_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = variablesDeclarationSingleType_AST;
	}
	
	public final List<String>  variables() throws RecognitionException, TokenStreamException {
		List<String> r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST variables_AST = null;
		Token  id = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST id_AST = null;
		Token  id2 = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST id2_AST = null;
		r = new LinkedList<String>();
		
		id = LT(1);
		id_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(id);
		astFactory.addASTChild(currentAST, id_AST);
		match(IDENT);
		r.add(id.getText());
		{
		_loop47:
		do {
			if ((LA(1)==COMMA)) {
				ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp97_AST = null;
				tmp97_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp97_AST);
				match(COMMA);
				id2 = LT(1);
				id2_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(id2);
				astFactory.addASTChild(currentAST, id2_AST);
				match(IDENT);
				r.add(id2.getText());
			}
			else {
				break _loop47;
			}
			
		} while (true);
		}
		variables_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = variables_AST;
		return r;
	}
	
	public final String  multiplicity() throws RecognitionException, TokenStreamException {
		String r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST multiplicity_AST = null;
		r = null;
		
		switch ( LA(1)) {
		case LITERAL_lone:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp98_AST = null;
			tmp98_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp98_AST);
			match(LITERAL_lone);
			r = "lone";
			multiplicity_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_one:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp99_AST = null;
			tmp99_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp99_AST);
			match(LITERAL_one);
			r = "one";
			multiplicity_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_some:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp100_AST = null;
			tmp100_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp100_AST);
			match(LITERAL_some);
			r = "some";
			multiplicity_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_seq:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp101_AST = null;
			tmp101_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp101_AST);
			match(LITERAL_seq);
			r = "seq";
			multiplicity_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LITERAL_set:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp102_AST = null;
			tmp102_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp102_AST);
			match(LITERAL_set);
			r = "set";
			multiplicity_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = multiplicity_AST;
		return r;
	}
	
	public final AlloyExpression  joinExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST joinExpression_AST = null;
		r = null; AlloyExpression other = null;
		
		r=productExpression(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==DOT) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3))) && (_tokenSet_2.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp103_AST = null;
			tmp103_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp103_AST);
			match(DOT);
			other=joinExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new ExprJoin(r, other);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		joinExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = joinExpression_AST;
		return r;
	}
	
	public final AlloyExpression  productExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST productExpression_AST = null;
		r = null; AlloyExpression other = null;
		
		r=overrideExpression(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==RARROW) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3))) && (_tokenSet_2.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp104_AST = null;
			tmp104_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp104_AST);
			match(RARROW);
			other=productExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new ExprProduct(r, other);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		productExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = productExpression_AST;
		return r;
	}
	
	public final AlloyExpression  overrideExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST overrideExpression_AST = null;
		r = null; AlloyExpression other = null;
		
		r=exprIntersectionExpression(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==PLUSPLUS) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3))) && (_tokenSet_2.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp105_AST = null;
			tmp105_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp105_AST);
			match(PLUSPLUS);
			other=overrideExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new ExprOverride(r, other);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		overrideExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = overrideExpression_AST;
		return r;
	}
	
	public final AlloyExpression  exprIntersectionExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST exprIntersectionExpression_AST = null;
		r = null; AlloyExpression other = null;
		
		r=atomicExpression(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		{
		if ((LA(1)==AMP) && (_tokenSet_0.member(LA(2))) && (_tokenSet_1.member(LA(3))) && (_tokenSet_2.member(LA(4)))) {
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp106_AST = null;
			tmp106_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp106_AST);
			match(AMP);
			other=exprIntersectionExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			r = new ExprIntersection(r, other);
		}
		else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))) && (_tokenSet_5.member(LA(4)))) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		exprIntersectionExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = exprIntersectionExpression_AST;
		return r;
	}
	
	public final AlloyExpression  atomicExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST atomicExpression_AST = null;
		Token  id = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST id_AST = null;
		Token  number = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST number_AST = null;
		Token  id2 = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST id2_AST = null;
		r = null;
		
		switch ( LA(1)) {
		case NUMBER:
		{
			number = LT(1);
			number_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(number);
			astFactory.addASTChild(currentAST, number_AST);
			match(NUMBER);
			r = ExpressionParser.buildTerm(ctx, number, null);
			atomicExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case LPAREN:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp107_AST = null;
			tmp107_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp107_AST);
			match(LPAREN);
			r=termExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp108_AST = null;
			tmp108_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp108_AST);
			match(RPAREN);
			atomicExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		case BACKSLASH_PRE:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp109_AST = null;
			tmp109_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp109_AST);
			match(BACKSLASH_PRE);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp110_AST = null;
			tmp110_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp110_AST);
			match(LBRACKET);
			id2 = LT(1);
			id2_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(id2);
			astFactory.addASTChild(currentAST, id2_AST);
			match(IDENT);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp111_AST = null;
			tmp111_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp111_AST);
			match(RBRACKET);
			r = ExpressionParser.buildPreTerm(ctx, id2);
			atomicExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			break;
		}
		default:
			if ((LA(1)==IDENT) && (_tokenSet_3.member(LA(2)))) {
				id = LT(1);
				id_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(id);
				astFactory.addASTChild(currentAST, id_AST);
				match(IDENT);
				r = ExpressionParser.buildTerm(ctx, id, null);
				atomicExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			}
			else if ((LA(1)==IDENT) && (LA(2)==SLASH||LA(2)==LBRACKET)) {
				r=functionOrIfExpression(ctx);
				astFactory.addASTChild(currentAST, returnAST);
				atomicExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
			}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = atomicExpression_AST;
		return r;
	}
	
	public final AlloyExpression  functionOrIfExpression(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		AlloyExpression r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST functionOrIfExpression_AST = null;
		Token  aliasModuleId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId_AST = null;
		Token  functionId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST functionId_AST = null;
		r = null; List<AlloyExpression> args = null; AlloyExpression thenPart = null; AlloyExpression elsePart = null; boolean isIfExpression = false;
		
		{
		if ((LA(1)==IDENT) && (LA(2)==SLASH)) {
			aliasModuleId = LT(1);
			aliasModuleId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(aliasModuleId);
			astFactory.addASTChild(currentAST, aliasModuleId_AST);
			match(IDENT);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp112_AST = null;
			tmp112_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp112_AST);
			match(SLASH);
		}
		else if ((LA(1)==IDENT) && (LA(2)==LBRACKET)) {
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		}
		functionId = LT(1);
		functionId_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(functionId);
		astFactory.addASTChild(currentAST, functionId_AST);
		match(IDENT);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp113_AST = null;
		tmp113_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp113_AST);
		match(LBRACKET);
		args=actualParams(ctx);
		astFactory.addASTChild(currentAST, returnAST);
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp114_AST = null;
		tmp114_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp114_AST);
		match(RBRACKET);
		{
		switch ( LA(1)) {
		case IF_EXPR:
		{
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp115_AST = null;
			tmp115_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp115_AST);
			match(IF_EXPR);
			thenPart=termExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
			ar.uba.dc.rfm.dynalloy.parser.DynalloyAST tmp116_AST = null;
			tmp116_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp116_AST);
			match(ELSE);
			elsePart=termExpression(ctx);
			astFactory.addASTChild(currentAST, returnAST);
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
		case SEMICOLON:
		case STAR:
		case RBRACE:
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
			
		functionOrIfExpression_AST = (ar.uba.dc.rfm.dynalloy.parser.DynalloyAST)currentAST.root;
		returnAST = functionOrIfExpression_AST;
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
		"MODULE_ID",
		"\"abstract\"",
		"\"sig\"",
		"\"extends\"",
		"\"in\"",
		"\"module\"",
		"\"open\"",
		"\"as\"",
		"\"exactly\"",
		"\"action\"",
		"\"act\"",
		"\"pre\"",
		"\"post\"",
		"\"program\"",
		"\"prog\"",
		"\"var\"",
		"\"assertCorrectness\"",
		"\"lblneg\"",
		"\"lblpos\"",
		"QUESTION",
		"\"call\"",
		"ASSIGNMENT",
		"\"assume\"",
		"\"while\"",
		"\"do\"",
		"\"repeat\"",
		"\"if\"",
		"\"skip\""
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 12583104L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 5016324050L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -377739411565115438L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 5003544338L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { -377739411565180974L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { -16325541200197678L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	
	}
