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

/**
 * Runs the Alloy Analyzer on an Alloy file: parses and type-checks it, picks a command, translates
 * it to Kodkod and solves it with the SAT solver that the system property {@code dynalloy.solver}
 * chooses ({@link #SOLVER_PROPERTY}; docs/SOLVERS.md). The last stage of the pipeline: TACO's
 * {@code AlloyStage} creates one per analysis and calls {@link #analyzeCommand} on
 * {@code output/output.als} without a command, so the first one runs; in dynalloy4,
 * {@code DynAlloyAnalyzer} uses it for the console and the visualizer. Before each analysis it
 * makes the native solvers loadable ({@link AlloyJNILibraryPath}; docs/NATIVE-LIBRARIES.md). See
 * docs/ARCHITECTURE.md ("Analysis").
 *
 * <p>Static state, kept between analyses in one JVM and reset by nothing:
 * <ul>
 * <li>{@code answer} (the {@code A4Solution} of the last analysis) and {@code log4Jreporter} (its
 *     {@link Reporter}): replaced by each analysis, read by nothing outside this class;</li>
 * <li>{@code a4reporter} and {@code solvingTime}: never assigned, so always null and -1. Unclear:
 *     whether they are meant to be filled (for example, for TACO to read the solving time) or are
 *     left over;</li>
 * <li>whether MiniSat 2.2 can be used, and the solver that {@code auto} chose: decided once, at
 *     the first analysis that needs them;</li>
 * <li>in alloy4.jar, the static switches of {@code TranslateAlloyToKodkod} that
 *     {@link #analyzeCommand} sets at each analysis.</li>
 * </ul>
 * So analyses must not run concurrently (docs/KNOWN-LIMITATIONS.md, "Translation"). The
 * {@code A4Reporter} given to the constructor, {@link #setReporter} and {@link #setVerbosity} are
 * ignored: the progress always goes to {@code System.out} (docs/KNOWN-LIMITATIONS.md, "Solver and
 * analyzer").
 */
public class AlloyAnalyzer {

	/** Static; never assigned, so always null, and read by nothing (see the class comment). */
	public static A4Reporter a4reporter = null;

	/**
	 * Static: the {@link Reporter} of the last {@link #analyzeCommand}, replaced by each one; read
	 * by nothing outside this class.
	 */
	public static Reporter log4Jreporter = null;

	/**
	 * Static; never assigned, so always -1, and read by nothing (see the class comment). The
	 * reporter prints the solving time.
	 */
	public static long solvingTime = -1;

	/**
	 * Static: Alloy's solution of the last {@link #analyzeCommand} that reached the solver,
	 * replaced by each one; read by nothing outside this class, which returns it in the
	 * {@link AlloyAnalysisResult}.
	 */
	public static A4Solution answer = null;

	private String alloyFilename;
	
	

	/**
	 * Creates an analyzer for one Alloy file.
	 *
	 * @param alloyFilename the path of the Alloy file
	 * @param a4Reporter ignored: each analysis reports through a new {@link Reporter}
	 */
	public AlloyAnalyzer(String alloyFilename, A4Reporter a4Reporter) {
		verbosity = false;
		this.alloyFilename = alloyFilename;
	}

	private boolean verbosity;

	/**
	 * Sets a flag that nothing reads.
	 *
	 * @param b ignored
	 */
	public void setVerbosity(boolean b) {
		verbosity = b;
	}

	/**
	 * Analyses one command of the Alloy file. In order: chooses the solver
	 * ({@link #build_A4Options()}), makes the native libraries loadable
	 * ({@link AlloyJNILibraryPath#setupJNILibraryPath()}), sets alloy4.jar's static switches
	 * {@code TranslateAlloyToKodkod.boundsHackEnabled} to false and
	 * {@code removeAlloySyntaxChecksHackEnabled} to true, parses and type-checks the file, picks
	 * the command, translates it to Kodkod and solves it. For a {@code check}, satisfiable means a
	 * counterexample.
	 *
	 * <p>Side effects: the static {@code answer} and {@code log4Jreporter}; the extracted
	 * libraries and {@code java.library.path} (see {@code setupJNILibraryPath}); the progress,
	 * printed on {@code System.out} by a new {@link Reporter}.
	 *
	 * @param commandToRun the command to run, matched by its text against the commands of the file
	 *        (the last match wins), or null for the first command; TACO passes null
	 * @param alloyOptions ignored: the options come from {@link #build_A4Options()}
	 * @return the result (satisfiable or not, the Alloy module, the command and the solution), or
	 *         null if Alloy returned no solution
	 * @throws AlloyAnalysisException if the file has no commands ("No commands in alloy model!"),
	 *         if {@code commandToRun} is not one of them ("Command not found: ..."), or with the
	 *         message of Alloy's error if the file does not parse or type-check, or translating or
	 *         solving fails
	 * @throws VizException never: it is only declared
	 * @throws IllegalStateException with {@code dynalloy.solver=minisat}, if MiniSat 2.2 cannot be
	 *         used
	 * @throws IllegalArgumentException if {@code dynalloy.solver} has an unknown value
	 */
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
	 * (MiniSat220JNI), which alloy4.jar ships for macOS (x86_64 and arm64) and
	 * for x86, amd64 and arm64 Linux (see README.md); "auto" uses it when its
	 * library for this platform is in alloy4.jar and loads, and SAT4J otherwise.
	 */
	public static final String SOLVER_PROPERTY = "dynalloy.solver";

	/** Why MiniSat 2.2 cannot be used in this JVM, or null if it can (see miniSat220Problem()). */
	private static String miniSat220Problem = null;
	private static boolean miniSat220Checked = false;

	/** The solver "auto" chose in this JVM; null until the first analysis. */
	private static A4Options.SatSolver autoSolver = null;

	/**
	 * Returns new Alloy options whose SAT solver is chosen by the system property
	 * {@code dynalloy.solver}, read at every call: {@code sat4j}, {@code minisat} (MiniSat 2.2
	 * through JNI) or {@code auto} (the default), in any case. The other options are Alloy's
	 * defaults. {@link #analyzeCommand} calls it at every analysis; TACO's {@code AlloyStage} also
	 * calls it, for the argument that {@code analyzeCommand} ignores. See docs/SOLVERS.md.
	 *
	 * <p>Side effects, once per JVM: with {@code minisat} or {@code auto}, the first call checks
	 * whether MiniSat 2.2 can be used, which extracts and loads the native libraries
	 * ({@link AlloyJNILibraryPath}); the first call with {@code auto} fixes its choice for the
	 * rest of the JVM's life and, if it is SAT4J, says why on {@code System.err}.
	 *
	 * @return the options
	 * @throws IllegalStateException with {@code minisat}, if MiniSat 2.2 cannot be used
	 * @throws IllegalArgumentException if the property has another value
	 */
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

	/**
	 * Asks the solver for the next solution of a previous analysis ({@code A4Solution.next()}),
	 * with the same Alloy module and command. The visualizer uses it, through
	 * {@code DynAlloyAnalyzer}.
	 *
	 * @param previousSolution a result of {@link #analyzeCommand} or of this method
	 * @return the next result; an unsatisfiable one when there are no more solutions
	 * @throws Err if Alloy fails
	 */
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

	/**
	 * Does nothing (docs/KNOWN-LIMITATIONS.md, "Solver and analyzer").
	 *
	 * @param reporter2 ignored
	 */
	public void setReporter(PrintStreamA4Reporter reporter2) {

	}

}
