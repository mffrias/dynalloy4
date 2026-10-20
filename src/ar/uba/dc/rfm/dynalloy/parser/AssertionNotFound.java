package ar.uba.dc.rfm.dynalloy.parser;

/**
 * Declared by {@code DynAlloyCompiler.compile}, {@code DynAlloyTranslator.translateDynAlloyAST},
 * {@code DynalloyParser.parse} and other methods, and caught by TACO's
 * {@code DynalloyToAlloyManager}, but thrown by nothing (nothing has thrown it since the first
 * commit). A module without an assertion makes the translation fail with a
 * {@code NullPointerException} instead. Unclear: whether a missing assertion is meant to throw it.
 */
public class AssertionNotFound extends Exception {

	public AssertionNotFound(String message) {
		super(message);
	}

}
