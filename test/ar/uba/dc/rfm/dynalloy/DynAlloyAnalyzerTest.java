package ar.uba.dc.rfm.dynalloy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.trace.TraceStep;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;

public class DynAlloyAnalyzerTest {

	@Test
	public void testAnalyzer() throws IOException, VizException {
		final String specString = buildSpecString();

		FileWriter writer = new FileWriter("temp.dals");
		writer.write(specString);
		writer.close();

		File dynalloy_file = new File("temp.dals");

		DynAlloyAnalyzer analyzer = new DynAlloyAnalyzer();
		analyzer.getAvailableCommands(dynalloy_file);

		List<AlloyCommand> alloyCommands = analyzer
				.getAvailableCommands(dynalloy_file);

		DynAlloyOptions options = new DynAlloyOptions();
		options.setStrictUnroll(false);
		options.setLoopUnroll(3);
		options.setRemoveQuantifier(false);
		options.setRunAlloyAnalyzer(true);
		options.alloyOptions.solver = A4Options.SatSolver.MiniSatJNI;

		if (!alloyCommands.isEmpty()) {
			AlloyCommand command = alloyCommands.get(0);
			DynAlloySolution dynalloySolution = analyzer.analyzeCommand(
					dynalloy_file, command, options);

			if (!dynalloySolution.isUNSAT()) {

				System.out.println("Problem is SAT");

				// If SAT, exists a trace

				List<TraceStep> trace = dynalloySolution.getTrace();

				int stepCount = 0;

				for (TraceStep step : trace) {

					// we walk every step from the trace

					DynalloyProgram program = step.getProgram();

					System.out.println(stepCount + ")" + program.toString());
					stepCount++;

					if (program instanceof InvokeAction) {

						// step is a atomic action invocation

						InvokeAction invokeAction = (InvokeAction) program;

						List<AlloyExpression> actualParameters = invokeAction
								.getActualParameters();

						invokeAction.getActionId();

						// we evaluate each actual parameter

						for (AlloyExpression actualParameter : actualParameters) {

							Object evalResult = step.evaluate(actualParameter);

							System.out.println(actualParameter.toString()
									+ ": " + evalResult.toString());

						}

					} else if (program instanceof TestPredicate) {

					} else if (program instanceof Skip) {

					}

				}

			} else {

				System.out.println("Problem is UNSAT");
			}

		}

	}

	private static String buildSpecString() {
		StringBuffer buff = new StringBuffer();
		buff.append("module myModule" + "\n");

		buff.append("one sig A {}" + "\n");
		buff.append("one sig B {}" + "\n");

		buff.append("one sig QF { x_0: A+B , x_1: A+B , x_2: A+B }" + "\n");

		buff.append("pred TruePred[] {}" + "\n");

		buff.append("pred equ[l,r:univ] {" + "\n");
		buff.append(" l=r" + "\n");
		buff.append("}" + "\n");

		buff.append("action setA[x: A+B] {" + "\n");
		buff.append("  pre  { TruePred[] }" + "\n");
		buff.append("  post { equ[x', A] }" + "\n");
		buff.append("}");

		buff.append("action setB[x: A+B] {" + "\n");
		buff.append("  pre  { TruePred[] }" + "\n");
		buff.append("  post { equ[x', B] }" + "\n");
		buff.append("}");

		buff.append("program myProgram[y: A+B] {" + "\n");
		buff.append("  assume equ[y, A]; " + "\n");
		buff.append("  setB[y]; " + "\n");
		buff.append("  setA[y]; " + "\n");
		buff.append("  setB[y]; " + "\n");
		buff.append("  setB[y]; " + "\n");
		buff.append("  setA[y]; " + "\n");
		buff.append("  skip" + "\n");
		buff.append("}" + "\n");

		buff.append("run myProgram for exactly 1 A, exactly 1 B");
		final String specString;
		specString = buff.toString();
		return specString;
	}

	@Test
	public void testNextResult() throws IOException, VizException {
		final String specString = buildSpecString();

		FileWriter writer = new FileWriter("temp.dals");
		writer.write(specString);
		writer.close();

		File dynalloy_file = new File("temp.dals");

		DynAlloyAnalyzer dynalloyAnalyzer = new DynAlloyAnalyzer();
		List<AlloyCommand> alloyCommands = dynalloyAnalyzer
				.getAvailableCommands(dynalloy_file);

		DynAlloyOptions options = new DynAlloyOptions();
		options.setStrictUnroll(false);
		options.setLoopUnroll(3);
		options.setRemoveQuantifier(false);
		options.setRunAlloyAnalyzer(true);

		options.alloyOptions.solver = A4Options.SatSolver.MiniSatJNI;

		if (!alloyCommands.isEmpty()) {
			AlloyCommand command = alloyCommands.get(0);
			DynAlloySolution solution = dynalloyAnalyzer.analyzeCommand(
					dynalloy_file, command, options);

			int number_of_solutions = 0;
			while (solution.isSAT()) {
				number_of_solutions++;
				solution = dynalloyAnalyzer.nextSolution(solution);
				assertNotNull(solution);
			}
			assertEquals(8, number_of_solutions);

		}
	}
}
