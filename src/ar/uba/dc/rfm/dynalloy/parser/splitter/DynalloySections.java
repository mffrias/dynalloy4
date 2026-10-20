package ar.uba.dc.rfm.dynalloy.parser.splitter;

/**
 * The kinds of the sections that {@link DocumentSplitter} cuts a {@code .dals} file into, as
 * strings, and what {@code DynalloyParser} does with each: {@code ALLOY} (Alloy text: facts,
 * functions, assertions, commands, comments) and {@code PRED_DECL} ({@code pred}) are copied to
 * the Alloy output; {@code ALLOY_SIGNATURE} ({@code sig}, {@code abstract} or {@code one}),
 * {@code ALLOY_MODULE} and {@code ALLOY_OPEN} are parsed and copied; {@code DYNALLOY_ACTION},
 * {@code DYNALLOY_PROGRAM} and {@code DYNALLOY_ASSERTION} are parsed into the DynAlloy module.
 * {@code DocumentSplitter} says where each ends.
 */
public interface DynalloySections {
	static String ALLOY = "alloy";
	static String ALLOY_SIGNATURE = "sig";
	static String DYNALLOY_ACTION = "action";
	static String DYNALLOY_PROGRAM = "program";
	static String DYNALLOY_ASSERTION = "assertCorrectness";
	static String ALLOY_MODULE = "module";
	static String ALLOY_OPEN = "open";
	static String PRED_DECL = "pred";
}
