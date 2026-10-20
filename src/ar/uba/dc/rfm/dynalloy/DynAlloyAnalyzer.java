package ar.uba.dc.rfm.dynalloy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.analyzer.AlloyAnalysisException;
import ar.uba.dc.rfm.dynalloy.analyzer.AlloyAnalysisResult;
import ar.uba.dc.rfm.dynalloy.analyzer.AlloyAnalyzer;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyStringPlugin;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolutionBuilder;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerException;
import ar.uba.dc.rfm.dynalloy.visualization.PrintStreamA4Reporter;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;

/**
 * Compiles a DynAlloy file and analyses one of its commands, for the console
 * ({@code DynAlloyConsole}) and the visualizer ({@code DynalloyVisualizerController}). TACO does
 * not use it: it calls {@link DynAlloyCompiler} and {@code AlloyAnalyzer} itself. It compiles with
 * a new {@code DynAlloyCompiler}, without plugins and with empty maps, which fails on this branch
 * (docs/KNOWN-LIMITATIONS.md, "DynAlloy works only through TACO"); solves with
 * {@code AlloyAnalyzer}; and turns each Alloy solution into a DynAlloy trace
 * ({@code DynAlloySolutionBuilder}). See docs/ARCHITECTURE.md ("Analysis").
 *
 * <p>Files: it writes the Alloy file next to the DynAlloy file (in {@code analyzeCommand} and
 * {@code compile}, to {@code options.getOutputFilename()} instead, when that is set), and a
 * {@code .settings} file with the options next to the DynAlloy file. Their paths replace the last
 * five characters of the DynAlloy file's path, so its name must end in {@code .dals}.
 *
 * <p>No static state, but {@code AlloyAnalyzer} has some. Each instance keeps the compiler,
 * options, analyzer and solution builder of its last analysis, for {@link #nextSolution}.
 */
public final class DynAlloyAnalyzer {

	private PrintStreamA4Reporter reporter;
	private DynAlloyCompiler compiler;
	private DynAlloyOptions options;
	private String alsFilename;
	private DynAlloySolutionBuilder solutionBuilder;
	private AlloyAnalyzer analyzer;

	/**
	 * Sets where this instance's reporter prints. The reporter is used only when
	 * {@link #getAvailableCommands} parses the Alloy file: {@code AlloyAnalyzer} ignores it, and
	 * an analysis reports on {@code System.out} (docs/KNOWN-LIMITATIONS.md, "Solver and
	 * analyzer"). Nothing calls this method; the visualizer sets the stream through
	 * {@code getReporter()}.
	 *
	 * @param printStream the stream
	 */
	public void setReporterPrintStream(PrintStream printStream) {
		reporter.setPrintStream(printStream);
	}

	public DynAlloyAnalyzer() {
		reporter = new PrintStreamA4Reporter();
		reporter.setVerbosity(true);
	}

	/**
	 * Does nothing (docs/KNOWN-LIMITATIONS.md, "Solver and analyzer"). Each analysis sets the
	 * reporter's verbosity from {@code DynAlloyOptions.getVerbosity()}, which is always false.
	 *
	 * @param verbosity ignored
	 */
	public void setVerbosity(boolean verbosity) {

	}

	/**
	 * Translates a DynAlloy file with {@code DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS} into the
	 * Alloy file next to it, and returns the commands of that Alloy file. The console calls it
	 * before {@link #analyzeCommand}, and runs the first command.
	 *
	 * <p>After the translation it prints the module and writes the file a second time, with a
	 * header built from the options of this instance's last analysis; before any analysis those
	 * are null, and it throws a {@code NullPointerException}.
	 *
	 * @param dalsFile the DynAlloy file, whose name ends in {@code .dals}
	 * @return the commands, in the order of the Alloy file
	 * @throws VizException if the Alloy file does not parse or type-check
	 * @throws DynalloyVisualizerException (unchecked) if the DynAlloy file cannot be read or
	 *         parsed, or the Alloy file cannot be written
	 */
	public List<AlloyCommand> getAvailableCommands(File dalsFile)
			throws VizException {
		DynAlloyCompiler controller = new DynAlloyCompiler();
		try {
			AlloyModule alloyAST = controller.compile(dalsFile.getAbsolutePath(),
					dalsFileToAlsPath(dalsFile),
					DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
					new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false, null);

			// Print Alloy AST
			String optionsHeader = controller.buildOptionsHeader(options);
			AlloyPrinter printer = new AlloyPrinter();
			String alloyStr = (String) alloyAST.accept(printer);
			String alloyStrWithHeader = optionsHeader + "\n" + alloyStr;

			// Apply AlloyString plugins
			for (AlloyStringPlugin string_plugin: controller.getAlloyStringPlugins()) {
				alloyStrWithHeader = string_plugin.transform(alloyStrWithHeader);
			}

			// System.out.println(alloyStrWithHeader);
			// Write Alloy file
			controller.writeFile(dalsFileToAlsPath(dalsFile), alloyStrWithHeader);

		} catch (RecognitionException e) {
			throw new DynalloyVisualizerException(e);
		} catch (TokenStreamException e) {
			throw new DynalloyVisualizerException(e);
		} catch (IOException e) {
			throw new DynalloyVisualizerException(e);
		} catch (AssertionNotFound e) {
			throw new DynalloyVisualizerException(e);
		}
		return getAlloyCommands(dalsFile);
	}

	/**
	 * Translates a DynAlloy file (as {@code compile} does) and, if
	 * {@code options.getRunAlloyAnalyzer()}, runs one command of the Alloy file with
	 * {@code AlloyAnalyzer} and returns its first solution as a DynAlloy trace. The console and
	 * the visualizer call it.
	 *
	 * <p>Side effects: writes the Alloy and {@code .settings} files; keeps the compiler, the
	 * options, the analyzer and the solution builder for {@link #nextSolution}; sets the
	 * reporter's verbosity to false. The analysis has the side effects of
	 * {@code AlloyAnalyzer.analyzeCommand} (static state, native libraries; docs/SOLVERS.md).
	 *
	 * @param dynAlloyFile the DynAlloy file, whose name ends in {@code .dals}
	 * @param command the command to run, matched by its text, or null for the first one
	 * @param options the options; {@code alloyOptions} is passed on and ignored
	 * @return the first solution, or null when {@code options.getRunAlloyAnalyzer()} is false
	 * @throws VizException if the Alloy file does not parse or type-check, has no commands or not
	 *         this one, or the analysis fails
	 * @throws DynalloyVisualizerException (unchecked) if the DynAlloy file cannot be read or
	 *         parsed, or a file cannot be written
	 */
	public DynAlloySolution analyzeCommand(File dynAlloyFile,
			AlloyCommand command, DynAlloyOptions options) throws VizException {

		// output: .ALS File , .settings File
		compile(dynAlloyFile, options, null);

		DynAlloySolution dynalloyInstance = null;
		if (options.getRunAlloyAnalyzer() == true) {
			// output: XML File
			dynalloyInstance = execute(dynAlloyFile, command, options);
		}
		return dynalloyInstance;
	}

	/**
	 * Translates a DynAlloy file with a new {@link DynAlloyCompiler}, without plugins, with empty
	 * maps and not for Stryker, into {@code options.getOutputFilename()} or, when that is null,
	 * the Alloy file next to it; then prints the module and writes the same text to that file a
	 * second time, and writes the options ({@code DynAlloyOptions.toString()}) to the
	 * {@code .settings} file next to the DynAlloy file. Replaces this instance's compiler. The
	 * console calls it when it is not to run the analyzer, and {@link #analyzeCommand} before
	 * analysing.
	 *
	 * @param dalsFile the DynAlloy file, whose name ends in {@code .dals}
	 * @param options the options of the translation
	 * @param inputToFix passed to the compiler, which only tests it for null
	 * @throws DynalloyVisualizerException (unchecked) if the DynAlloy file cannot be read or
	 *         parsed, or a file cannot be written
	 */
	void compile(File dalsFile, DynAlloyOptions options, Object inputToFix) throws VizException {

		compiler = new DynAlloyCompiler();

		try {

			String output_filename = null;
			if (options.getOutputFilename()==null)
				output_filename = dalsFileToAlsPath(dalsFile);
			else
				output_filename = options.getOutputFilename();

			AlloyModule alloyModule = compiler.compile(dalsFile.getAbsolutePath(),
					output_filename, options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
					new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false, inputToFix);

			// Print Alloy AST
			String optionsHeader = compiler.buildOptionsHeader(options);
			AlloyPrinter printer = new AlloyPrinter();
			String alloyStr = (String) alloyModule.accept(printer);
			String alloyStrWithHeader = optionsHeader + "\n" + alloyStr;

			// Apply AlloyString plugins
			for (AlloyStringPlugin string_plugin: compiler.getAlloyStringPlugins()) {
				alloyStrWithHeader = string_plugin.transform(alloyStrWithHeader);
			}

			//			System.out.println(alloyStrWithHeader);
			// Write Alloy file
			compiler.writeFile(output_filename, alloyStrWithHeader);

		} catch (RecognitionException e) {
			throw new DynalloyVisualizerException(e);
		} catch (TokenStreamException e) {
			throw new DynalloyVisualizerException(e);
		} catch (IOException e) {
			throw new DynalloyVisualizerException(e);
		} catch (AssertionNotFound e) {
			throw new DynalloyVisualizerException(e);
		}

		writeSettings(dalsFile, options);

	}

	private static void writeSettings(File dalsFile, DynAlloyOptions opts) {

		StringBuffer buff = new StringBuffer();
		buff.append("dalsFile=" + dalsFile.getAbsolutePath() + ",\n");
		buff.append(opts.toString());

		String settingsPath = dalsFileToSettingsPath(dalsFile);
		FileWriter writer;
		try {
			writer = new FileWriter(settingsPath);
			writer.write(buff.toString());
			writer.close();
		} catch (IOException e) {
			throw new DynalloyVisualizerException(e);
		}

	}

	private List<AlloyCommand> getAlloyCommands(File dals) throws VizException {
		try {
			List<AlloyCommand> commands = new ArrayList<AlloyCommand>();
			Module world = CompUtil.parseEverything_fromFile(reporter, null,
					dalsFileToAlsPath(dals), 1);
			for (Command command : world.getAllCommands()) {
				commands.add(new AlloyCommand(command.toString(), command.label));
			}

			return commands;
		} catch (Err e) {
			throw new VizException(e);
		}
	}

	/**
	 * Returns the next solution of the last analysis, as a DynAlloy trace. Assumes that
	 * {@link #analyzeCommand} ran on this instance with {@code getRunAlloyAnalyzer()} set
	 * (otherwise a {@code NullPointerException}), and that {@code previous} is one of its
	 * solutions. The visualizer calls it.
	 *
	 * @param previous a solution returned by {@code analyzeCommand} or by this method
	 * @return the next solution; an unsatisfiable one when there are no more
	 * @throws VizException if Alloy fails, or the trace cannot be built
	 */
	public DynAlloySolution nextSolution(DynAlloySolution previous)
			throws VizException {

		AlloyAnalysisResult alloyAnalysisResult = previous
				.getAlloyAnalysisResult();
		try {

			AlloyAnalysisResult next = analyzer
					.nextSolution(alloyAnalysisResult);

			boolean options_skolemize = options.getRemoveQuantifiers();

			return solutionBuilder.buildSolution(next, options_skolemize);

		} catch (Err e) {
			throw new VizException(e);
		} catch (IOException e) {
			throw new VizException(e);
		}

	}

	private DynAlloySolution execute(File dals, AlloyCommand command,
			DynAlloyOptions options) throws VizException {

		this.reporter.setVerbosity(options.getVerbosity());

		try {

			if (options.getOutputFilename() == null)
				alsFilename = dalsFileToAlsPath(dals);
			else
				alsFilename = options.getOutputFilename();

			this.options = options;

			analyzer = new AlloyAnalyzer(alsFilename, this.reporter);

			AlloyAnalysisResult ans = analyzer.analyzeCommand(command,
					options.alloyOptions);

			boolean options_skolemize = options.getRemoveQuantifiers();

			solutionBuilder = new DynAlloySolutionBuilder(
					compiler.getSpecContext(), ans.getWorld(), ans.getCommand());

			DynAlloySolution firstSolution = solutionBuilder.buildSolution(ans,
					options_skolemize);

			return firstSolution;

		} catch (Err e) {
			throw new VizException(e);
		} catch (IOException e) {
			throw new VizException(e);
		} catch (AlloyAnalysisException e) {
			throw new VizException(e);
		}
	}

	private static String dalsFileToAlsPath(File dalsFile) {
		String dalsPath = dalsFile.getAbsolutePath();
		String alsPath = dalsPath.substring(0, dalsPath.length() - 5) + ".als";

		return alsPath;
	}

	private static String dalsFileToXmlPath(File dalsFile) {
		String dalsPath = dalsFile.getAbsolutePath();
		String xmlPath = dalsPath.substring(0, dalsPath.length() - 5) + ".xml";

		return xmlPath;
	}

	private static String dalsFileToSettingsPath(File dalsFile) {
		String dalsPath = dalsFile.getAbsolutePath();
		String settingsPath = dalsPath.substring(0, dalsPath.length() - 5)
				+ ".settings";

		return settingsPath;
	}

	public PrintStreamA4Reporter getReporter() {
		return reporter;
	}

}
