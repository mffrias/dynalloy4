package ar.uba.dc.rfm.dynalloy.analyzer;


/**
 * The analysis of an Alloy file failed: {@link AlloyAnalyzer#analyzeCommand} throws it when the
 * file has no commands or not the one asked for, and with the message of Alloy's error when the
 * file does not parse or type-check, or translating or solving fails. Only the message is kept.
 * TACO's {@code AlloyStage} turns it into a {@code TacoException}, {@code DynAlloyAnalyzer} into a
 * {@code VizException}.
 */
public class AlloyAnalysisException extends Exception {

	public AlloyAnalysisException(String message) {
		super(message);
	}

}
