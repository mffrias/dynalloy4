/*
 * Dynalloy Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.dynalloy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerController;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;

public class DynAlloyConsole {

	/**
	 * @param args
	 * @throws IOException
	 * @throws TokenStreamException
	 * @throws RecognitionException
	 * @throws AssertionNotFound
	 * @throws VizException
	 */
	public static void main(String[] args) throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound, VizException {
		run(args);
	}

	public static DynalloyVisualizerController run(String[] args)
			throws RecognitionException, TokenStreamException, IOException,
			AssertionNotFound, VizException {

		ConsoleArguments consoleArguments;
		try {
			DynAlloyOptions defaultOptions = DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS;
			consoleArguments = new ConsoleArguments(args);

			// mandatory fields
			String inputFilename = consoleArguments.getInput();
			String outputFilename = null;
			if (consoleArguments.getOutput() != null) {
				outputFilename = consoleArguments.getOutput();
			}

			// optional fields
			int unroll;
			if (consoleArguments.getUnroll() != null)
				unroll = consoleArguments.getUnroll();
			else
				unroll = defaultOptions.getLoopUnroll();

			String assertionToCheck;
			if (consoleArguments.getAssertionToCheck() != null)
				assertionToCheck = consoleArguments.getAssertionToCheck();
			else
				assertionToCheck = null;

			boolean strictUnrolling;
			if (consoleArguments.getStrictUnrolling() != null)
				strictUnrolling = consoleArguments.getStrictUnrolling();
			else
				strictUnrolling = defaultOptions.getStrictUnrolling();

			boolean removeQuantifiers;
			if (consoleArguments.getRemoveQuantifiers() != null)
				removeQuantifiers = consoleArguments.getRemoveQuantifiers();
			else
				removeQuantifiers = defaultOptions.getRemoveQuantifiers();

			boolean runAlloyAnalyzer;
			if (consoleArguments.getRunAlloyAnalyzer() != null)
				runAlloyAnalyzer = consoleArguments.getRunAlloyAnalyzer();
			else
				runAlloyAnalyzer = defaultOptions.getRunAlloyAnalyzer();

			boolean buildDynAlloyTrace;
			if (consoleArguments.getBuildDynAlloyTrace() != null)
				buildDynAlloyTrace = consoleArguments.getBuildDynAlloyTrace();
			else
				buildDynAlloyTrace = defaultOptions.getBuildDynAlloyTrace();

			boolean removeExitWhileGuard;
			if (consoleArguments.getRemoveExitWhileGuard() != null) {
				removeExitWhileGuard = consoleArguments
						.getRemoveExitWhileGuard();
			} else {
				removeExitWhileGuard = defaultOptions.getRemoveExitWhileGuard();
			}
			DynAlloyOptions dynalloyOptions = new DynAlloyOptions();

			dynalloyOptions.setOutputFilename(outputFilename);
			dynalloyOptions.setAssertionToCheck(assertionToCheck);
			dynalloyOptions.setLoopUnroll(unroll);
			dynalloyOptions.setStrictUnroll(strictUnrolling);
			dynalloyOptions.setRemoveQuantifier(removeQuantifiers);
			dynalloyOptions.setRunAlloyAnalyzer(runAlloyAnalyzer);
			dynalloyOptions.setBuildDynAlloyTrace(buildDynAlloyTrace);
			dynalloyOptions.setRemoveExitWhileGuard(removeExitWhileGuard);

			DynAlloyAnalyzer analyzer = new DynAlloyAnalyzer();
			File dynalloyFile = new File(inputFilename);

			if (dynalloyOptions.getRunAlloyAnalyzer() == false) {
				analyzer.compile(dynalloyFile, dynalloyOptions);
			} else {

				List<AlloyCommand> commands = analyzer
						.getAvailableCommands(dynalloyFile);

				if (commands.isEmpty()) {
					// do nothing
				} else {
					AlloyCommand command = commands.get(0);
					DynAlloySolution solution = analyzer.analyzeCommand(
							dynalloyFile, command, dynalloyOptions);
					if (solution == null) {
						System.out
								.println("DynAlloy Analyzer verification not executed.");
					} else {
						System.out.println("Result: "
								+ (solution.isUNSAT() ? "UNSAT" : "SAT"));
					}
				}
			}

			return null;

		} catch (ArgumentException e) {
			System.out.println(e.getMessage());
			System.out.println(ConsoleArguments.printHelp());
		}

		return null;

	}

}
