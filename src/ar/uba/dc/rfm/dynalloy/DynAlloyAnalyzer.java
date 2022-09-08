package ar.uba.dc.rfm.dynalloy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
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

public final class DynAlloyAnalyzer {

	private PrintStreamA4Reporter reporter;
	private DynAlloyCompiler compiler;
	private DynAlloyOptions options;
	private String alsFilename;
	private DynAlloySolutionBuilder solutionBuilder;
	private AlloyAnalyzer analyzer;

	public void setReporterPrintStream(PrintStream printStream) {
		reporter.setPrintStream(printStream);
	}

	public DynAlloyAnalyzer() {
		reporter = new PrintStreamA4Reporter();
		reporter.setVerbosity(true);
	}

	public void setVerbosity(boolean verbosity) {

	}

	public List<AlloyCommand> getAvailableCommands(File dalsFile)
			throws VizException {
		DynAlloyCompiler controller = new DynAlloyCompiler();
		try {
			AlloyModule alloyAST = controller.compile(dalsFile.getAbsolutePath(),
					dalsFileToAlsPath(dalsFile),
					DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
					new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);

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

	public DynAlloySolution analyzeCommand(File dynAlloyFile,
			AlloyCommand command, DynAlloyOptions options) throws VizException {

		// output: .ALS File , .settings File
		compile(dynAlloyFile, options);

		DynAlloySolution dynalloyInstance = null;
		if (options.getRunAlloyAnalyzer() == true) {
			// output: XML File
			dynalloyInstance = execute(dynAlloyFile, command, options);
		}
		return dynalloyInstance;
	}

	void compile(File dalsFile, DynAlloyOptions options) throws VizException {

		compiler = new DynAlloyCompiler();

		try {

			String output_filename = null;
			if (options.getOutputFilename()==null)
				output_filename = dalsFileToAlsPath(dalsFile);
			else
				output_filename = options.getOutputFilename();

			AlloyModule alloyModule = compiler.compile(dalsFile.getAbsolutePath(),
					output_filename, options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
					new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);

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
