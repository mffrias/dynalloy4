package ar.uba.dc.rfm.dynalloy.visualization;

import java.io.PrintStream;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class PrintStreamA4Reporter extends A4Reporter {

	private PrintStream printStream;
	private long lastTime;

	public PrintStreamA4Reporter() {
		this(System.out);
	}

	public PrintStreamA4Reporter(PrintStream out) {
		super();
		this.printStream = out;
	}

	public PrintStream getPrintStream() {
		return printStream;
	}

	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	@Override
	public void parse(String msg) {
		if (verbosity) {
			printStream.print(msg);
		}
	}

	@Override
	public void warning(ErrorWarning msg) {
		if (verbosity) {
			printStream.print("Relevance Warning:\n" + (msg.toString().trim()));
		}
	}

	@Override
	public void scope(String msg) {
		if (verbosity) {
			printStream.print("Scope: " + msg);
		}
	}

	@Override
	public void translate(String solver, int bitwidth, int maxseq,
			int skolemDepth, int symmetry) {
		lastTime = System.currentTimeMillis();
		if (verbosity) {
			printStream
					.print(String
							.format(
									"Solver=%s Bitwidth=%d MaxSeq=%d SkolemDepth=%d Symmetry=%d\n",
									solver, bitwidth, maxseq, skolemDepth,
									symmetry));
		}
	}

	@Override
	public void solve(int primaryVars, int totalVars, int clauses) {
		if (verbosity) {
			printStream.format("%d vars. %d primary vars. %d clauses. %dms\n",
					totalVars, primaryVars, clauses, System.currentTimeMillis()
							- lastTime);
		}
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void resultSAT(Object command, long solvingTime, Object solution) {
		if (!(solution instanceof A4Solution) || !(command instanceof Command))
			return;
		A4Solution sol = (A4Solution) solution;
		Command cmd = (Command) command;

		if (sol.satisfiable()) {
			if (verbosity) {
				printStream.format("%s found. %s is %s. %dms\n\n",
						cmd.check ? "Counterexample" : "Instance", cmd.label,
						cmd.check ? "invalid" : "consistent", System
								.currentTimeMillis()
								- lastTime);
			}
		} else {
			if (verbosity) {
				printStream.format("No counterexample found.\n\n");
			}
		}
	}

	@Override
	public void bound(String msg) {
		if (verbosity)
			printStream.print("bound: " + msg);
	}

	private boolean verbosity;

	public void setVerbosity(boolean b) {
		verbosity = b;
	}
}
