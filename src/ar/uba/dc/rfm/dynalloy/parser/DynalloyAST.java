package ar.uba.dc.rfm.dynalloy.parser;

import java.util.Stack;

import antlr.CommonAST;
import antlr.Token;

/**
 * The ANTLR AST node class of the DynAlloy grammar ({@link DynalloyParser} installs it in each
 * parser's factory): a {@code CommonAST} that keeps its token's line and column, from which the
 * grammar makes the positions of the program constructs.
 *
 * <p>Static state: two stacks, of the nodes made for {@code if} tokens and for {@code while}
 * tokens. {@link #initialize(Token)} pushes them, and the grammar pops one when it finishes an
 * {@code if} or a {@code while} ({@link #pollIfToken()}, {@link #pollWhileToken()}); nested
 * constructs finish in the reverse order they start, so each pops its own. The stacks are never
 * cleared: a parse that fails inside an {@code if} or a {@code while} leaves its node there for
 * the rest of the JVM's life, below the ones that later constructs push and pop. Parses must not
 * run concurrently.
 */
public class DynalloyAST extends CommonAST {

	private static final long serialVersionUID = 3975971489305895837L;

	private int line;
	private int column;
	private static Stack<DynalloyAST> ifTokens = new Stack<DynalloyAST>();
	private static Stack<DynalloyAST> whileTokens = new Stack<DynalloyAST>();

	/**
	 * Initializes the node from its token, as {@code CommonAST} does, and keeps the token's line
	 * and column. For an {@code if} or a {@code while} token, also pushes the node on the static
	 * stack that {@link #pollIfToken()} or {@link #pollWhileToken()} pops.
	 *
	 * @param tok the token
	 */
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
	
	/**
	 * Removes and returns the latest node made for an {@code if} token, from the static stack. The
	 * grammar calls it when it has parsed a whole {@code if ... else ...}, for the position of the
	 * two tests it generates.
	 *
	 * @return the node
	 * @throws java.util.EmptyStackException if there is none
	 */
	public static DynalloyAST pollIfToken() {
		return DynalloyAST.ifTokens.pop();
	}

	/**
	 * Removes and returns the latest node made for a {@code while} token, from the static stack.
	 * The grammar calls it when it has parsed a whole {@code while} loop.
	 *
	 * @return the node
	 * @throws java.util.EmptyStackException if there is none
	 */
	public static DynalloyAST pollWhileToken() {
		return DynalloyAST.whileTokens.pop();
	}

}
