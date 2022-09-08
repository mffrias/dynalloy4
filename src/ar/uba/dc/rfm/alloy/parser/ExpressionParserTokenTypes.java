// $ANTLR 2.7.6 (2005-12-22): "expression.g" -> "ExpressionLexer.java"$

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

public interface ExpressionParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int COMMA = 4;
	int COLON = 5;
	int IDENT = 6;
	int LPAREN = 7;
	int RPAREN = 8;
	int RARROW = 9;
	int PLUS = 10;
	int LITERAL_lone = 11;
	int LITERAL_one = 12;
	int LITERAL_some = 13;
	int LITERAL_seq = 14;
	int LITERAL_set = 15;
	int SLASH = 16;
	int LBRACKET = 17;
	int RBRACKET = 18;
	int DOT = 19;
	int PLUSPLUS = 20;
	int AMP = 21;
	int NUMBER = 22;
	int BACKSLASH_PRE = 23;
	int IF_EXPR = 24;
	int ELSE = 25;
	int VAL = 26;
	int SEMICOLON = 27;
	int EQUALS = 28;
	int STAR = 29;
	int MINUS = 30;
	int LBRACE = 31;
	int RBRACE = 32;
	int COMMENT = 33;
	int COMMENT_SLASH_SLASH = 34;
	int COMMENT_ML = 35;
	int WS = 36;
	int MODULE_ID = 37;
}
