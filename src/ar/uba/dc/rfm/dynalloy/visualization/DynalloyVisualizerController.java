package ar.uba.dc.rfm.dynalloy.visualization;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.DynAlloyAnalyzer;
import ar.uba.dc.rfm.dynalloy.DynAlloyCompiler;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

public final class DynalloyVisualizerController {

	private final PrintStreamA4Reporter reporter;
	private DynAlloyAnalyzer dynAlloyAnalyzer;

	public void setReporterPrintStream(PrintStream printStream) {
		dynAlloyAnalyzer.getReporter().setPrintStream(printStream);
	}

	public DynalloyVisualizerController() {
		reporter = new PrintStreamA4Reporter();
		reporter.setVerbosity(true);
		dynAlloyAnalyzer = new DynAlloyAnalyzer();
	}

	// XXX ADDED BY DANIEL
	public DynAlloyAnalyzer getDynAlloyAnalyzer() {
		return dynAlloyAnalyzer;
	}

	public List<AlloyCommand> getAvailableCommands(File dalsFile)
			throws VizException {
		DynAlloyCompiler compiler = new DynAlloyCompiler();
		DynAlloyOptions opts = DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS;

		try {
			compiler.compile(dalsFile.getAbsolutePath(),
					dalsFileToAlsPath(dalsFile), opts, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
					new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);
		} catch (RecognitionException e) {
			throw new DynalloyVisualizerException(e);
		} catch (TokenStreamException e) {
			throw new DynalloyVisualizerException(e);
		} catch (IOException e) {
			throw new DynalloyVisualizerException(e);
		} catch (AssertionNotFound e) {
			throw new DynalloyVisualizerException(e);
		}
		String alloyFilename = dalsFileToAlsPath(dalsFile);
		return getAlloyCommands(alloyFilename);
	}

	public DynAlloySolution translateAndExecute(File dalsFile,
			AlloyCommand command, DynAlloyOptions options)
			throws VizException {
		DynAlloySolution solution = dynAlloyAnalyzer.analyzeCommand(dalsFile,
				command, options);
		return solution;
	}

	private List<AlloyCommand> getAlloyCommands(String alloyFilename)
			throws VizException {
		try {
			List<AlloyCommand> commands = new ArrayList<AlloyCommand>();
			Module world = CompUtil.parseEverything_fromFile(reporter, null,
					alloyFilename, 1);
			for (Command command : world.getAllCommands()) {
				commands
						.add(new AlloyCommand(command.toString(), command.label));
			}

			return commands;
		} catch (Err e) {
			throw new VizException(e);
		}
	}

	public DynAlloySolution nextSolution(DynAlloySolution previous)
			throws VizException {
		DynAlloySolution nextSolution = dynAlloyAnalyzer.nextSolution(previous);
		return nextSolution;
	}

	private static String dalsFileToAlsPath(File dalsFile) {
		String dalsPath = dalsFile.getAbsolutePath();
		String alsPath = dalsPath.substring(0, dalsPath.length() - 5) + ".als";

		return alsPath;
	}

}
