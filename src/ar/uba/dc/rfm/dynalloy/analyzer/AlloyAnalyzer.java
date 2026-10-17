/*
 * TACO: Translation of Annotated COde
 * Copyright (c) 2010 Universidad de Buenos Aires
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

package ar.uba.dc.rfm.dynalloy.analyzer;

import java.util.HashSet;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.PrintStreamA4Reporter;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.satlab.SATSolver;

public class AlloyAnalyzer {

	public static A4Reporter a4reporter = null;

	public static Reporter log4Jreporter = null;

	public static long solvingTime = -1;

	public static A4Solution answer = null;

	private String alloyFilename;
	
	

	public AlloyAnalyzer(String alloyFilename, A4Reporter a4Reporter) {
		verbosity = false;
		this.alloyFilename = alloyFilename;
	}

	private boolean verbosity;

	public void setVerbosity(boolean b) {
		verbosity = b;
	}

	public AlloyAnalysisResult analyzeCommand(AlloyCommand commandToRun, A4Options alloyOptions)
			throws VizException, AlloyAnalysisException {

		A4Options alloy4Options = build_A4Options();

		AlloyJNILibraryPath alloyJNILibraryPath = new AlloyJNILibraryPath();
		alloyJNILibraryPath.setupJNILibraryPath();

		log4Jreporter = new Reporter();
		Reporter log = log4Jreporter;

		log.parseAndTypeCheck(alloyFilename);
		Module world;
		Command command = null;
		try {
//mffrias-mfrias-06/10/2012-set default values of optimizations.		
			TranslateAlloyToKodkod.boundsHackEnabled = false;
			TranslateAlloyToKodkod.removeAlloySyntaxChecksHackEnabled = true;			
			world = CompUtil.parseEverything_fromFile(log, null, alloyFilename);

			if (world.getAllCommands().isEmpty()) {
				throw new AlloyAnalysisException("No commands in alloy model!");
			}

			if (commandToRun == null) {
				command = world.getAllCommands().get(0);
			} else {
				for (Command alloy_command : world.getAllCommands()) {
					if (commandToRun.toString().equals(alloy_command.toString())) {
						command = alloy_command;
					}
				}
				if (command == null) {
					throw new AlloyAnalysisException(
						"Command not found: " + commandToRun.toString());
				}
			}
			
			log.alloy2kodkod(command);
			answer = TranslateAlloyToKodkod.execute_command(
				log, world.getAllReachableSigs(), command, alloy4Options);
			
		} catch (Err e) {
			throw new AlloyAnalysisException(e.getMessage());
		}

		if (answer == null) {
			log.translationFinished();
			return null;
		}

		log.analysisFinished();

		AlloyAnalysisResult result = new AlloyAnalysisResult(
			answer.satisfiable(), world, command, answer);
		
		return result;
	}

	/**
	 * System property that selects the SAT solver: "auto" (the default),
	 * "minisat" or "sat4j". "minisat" is MiniSat 2.2 through JNI
	 * (MiniSat220JNI), which alloy4.jar ships for macOS (x86_64 and arm64, see
	 * README.md) and for x86 and amd64 Linux; "auto" uses it when its library
	 * for this platform is in alloy4.jar and loads, and SAT4J otherwise.
	 */
	public static final String SOLVER_PROPERTY = "dynalloy.solver";

	/** Why MiniSat 2.2 cannot be used in this JVM, or null if it can (see miniSat220Problem()). */
	private static String miniSat220Problem = null;
	private static boolean miniSat220Checked = false;

	/** The solver "auto" chose in this JVM; null until the first analysis. */
	private static A4Options.SatSolver autoSolver = null;

	public static A4Options build_A4Options() {

		A4Options alloyOptions = new A4Options();

		String solver = System.getProperty(SOLVER_PROPERTY, "auto");
		if (solver.equalsIgnoreCase("sat4j")) {
			alloyOptions.solver = A4Options.SatSolver.SAT4J;
		} else if (solver.equalsIgnoreCase("minisat")) {
			String problem = miniSat220Problem();
			if (problem != null) {
				throw new IllegalStateException(SOLVER_PROPERTY + "=minisat, but " + problem
						+ " (" + SOLVER_PROPERTY + "=sat4j or auto would solve with SAT4J)");
			}
			alloyOptions.solver = A4Options.SatSolver.MiniSat220JNI;
		} else if (solver.equalsIgnoreCase("auto")) {
			alloyOptions.solver = autoSolver();
		} else {
			throw new IllegalArgumentException("Unknown SAT solver " + SOLVER_PROPERTY + "=" + solver
					+ " (use auto, minisat or sat4j)");
		}
		return alloyOptions;
	}

	/** MiniSat 2.2 if it can be used here, SAT4J otherwise, saying so once. */
	private static synchronized A4Options.SatSolver autoSolver() {
		if (autoSolver == null) {
			String problem = miniSat220Problem();
			if (problem == null) {
				autoSolver = A4Options.SatSolver.MiniSat220JNI;
			} else {
				autoSolver = A4Options.SatSolver.SAT4J;
				System.err.println("DynAlloy: solving with SAT4J, because " + problem + ".");
			}
		}
		return autoSolver;
	}

	/**
	 * Null if MiniSat 2.2 can be used in this JVM, or why not: its library for
	 * this platform is not in alloy4.jar, or it does not load. Checked once,
	 * after making the extracted libraries loadable, by creating and freeing a
	 * MiniSat 2.2 solver.
	 */
	private static synchronized String miniSat220Problem() {
		if (!miniSat220Checked) {
			miniSat220Checked = true;
			String library = AlloyJNILibraryPath.platformFolder() + "/" + AlloyJNILibraryPath.miniSat220FileName();
			if (Util.class.getClassLoader().getResource(library) == null) {
				miniSat220Problem = "alloy4.jar has no MiniSat 2.2 for this platform (" + library + ")";
			} else {
				try {
					new AlloyJNILibraryPath().setupJNILibraryPath();
					SATSolver miniSat = SATFactory.MiniSat220.instance();
					miniSat.free();
				} catch (Throwable e) {
					miniSat220Problem = "MiniSat 2.2 (" + library + ") does not load: " + e;
				}
			}
		}
		return miniSat220Problem;
	}

	public AlloyAnalysisResult nextSolution(AlloyAnalysisResult previousSolution) throws Err {
		A4Solution previousA4Solution = previousSolution.getAlloy_solution();
		A4Solution nextA4Solution = previousA4Solution.next();

		AlloyAnalysisResult nextAlloyAnalysisResult = new AlloyAnalysisResult(
			nextA4Solution.satisfiable(),
			previousSolution.getWorld(),
			previousSolution.getCommand(),
			nextA4Solution
		);

		return nextAlloyAnalysisResult;
	}

	public void setReporter(PrintStreamA4Reporter reporter2) {

	}

}
