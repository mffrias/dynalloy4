package ar.uba.dc.rfm.dynalloy.parser;

import java.util.Stack;

import antlr.CommonAST;
import antlr.Token;

public class DynalloyAST extends CommonAST {

	private static final long serialVersionUID = 3975971489305895837L;

	private int line;
	private int column;
	private static Stack<DynalloyAST> ifTokens = new Stack<DynalloyAST>();
	private static Stack<DynalloyAST> whileTokens = new Stack<DynalloyAST>();

	@Override
	public void initialize(Token tok) {
		super.initialize(tok);
		this.line = tok.getLine();
		this.column = tok.getColumn();
		if (tok.getType() == DynAlloyANTLRParserTokenTypes.LITERAL_if) {
			DynalloyAST.ifTokens.push(this);
		} else if (tok.getType() == DynAlloyANTLRParserTokenTypes.LITERAL_while) {
			DynalloyAST.whileTokens.push(this);
		}
	}
	
	@Override
	public int getLine() {
		return this.line;
	}
	
	@Override
	public int getColumn() {
		return this.column;
	}
	
	public static DynalloyAST pollIfToken() {
		return DynalloyAST.ifTokens.pop();
	}

	public static DynalloyAST pollWhileToken() {
		return DynalloyAST.whileTokens.pop();
	}

}
