package ar.uba.dc.rfm.dynalloy.analyzer;

import java.util.Arrays;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;


public final class Reporter extends A4Reporter {

    public void parseAndTypeCheck(String filename) {
        reportTitle("Starting Alloy Analyzer via command line interface");
        reportDatum("Input spec file", filename);
        reportTitle("Parsing and typechecking");
    }

    public void alloy2kodkod(Command command) {
        reportDatum("Command type", command.check ? "check" : "run");
        reportDatum("Command label", command.label);
        if(command.expects == 0 || command.expects == 1)
            reportDatum("Expected outcome", command.expects == 1 ? "SAT" : "UNSAT");
        reportTitle("Translating Alloy to Kodkod");
    }

    public void resultXML(final String filename) {
        reportDatum("XML solution file", filename);
    }

    public void enumSolutions(boolean maxSolsEnabled, int maxSols, boolean maxTimeEnabled, int maxSecs, boolean outFileEnabled, String outFile) {
        reportTitle("Enumerating solutions");
        if(outFileEnabled)
            reportDatum("Output filename", outFile);
        if(maxTimeEnabled)
            reportDatum("Timeout", maxSecs * 1000);
        if(maxSolsEnabled)
            reportDatum("Target quota", maxSols);
    }

    public void enumDone(int solsFound, boolean moreSolsLeft, boolean maxSolsReached, boolean maxTimeReached) {
        reportDatum("Solutions found", solsFound);
        reportDatum("Solution count is", moreSolsLeft ? "partial" : "exhaustive");
        reportDatum("Stopping reason", maxTimeReached ? "timeout" : maxSolsReached ? "quota met" : "all found");
    }

    public void analysisFinished() {
        reportTitle("Analysis finished");
    }

    public void translationFinished() {
        reportTitle("Translation finished");
    }

    @Override
    public void translate(String solver, int bitw, int maxseq, int skold, int symm) {
        reportDatum("Solver", solver);
        reportDatum("Bit width", bitw);
        reportDatum("Max sequence", maxseq);
        reportDatum("Skolem depth", skold);
        reportDatum("Symmbreaking", symm);

        if(solver.equals("kodkod"))
            reportTitle("Translating Kodkod to Java");
        else
            reportTitle("Translating Kodkod to CNF");
    }

    @Override
    public void resultCNF(final String filename) {
        reportDatum("Output file", filename);
    }

    @Override
    public void solve(int primaryVars, int totalVars, int clauses) {
        reportDatum("Primary vars", primaryVars);
        reportDatum("Total vars", totalVars);
        reportDatum("Clauses", clauses);
        reportTitle("Solving");
    }

    @Override
    public void resultSAT(Object command, long solvingTime, Object solution) {
        reportDatum("Outcome", "SAT: A failure has been detected.");
        reportDatum("Solving time", solvingTime);

    }

    @Override
    public void resultUNSAT(Object command, long solvingTime, Object solution) {
        reportDatum("Outcome", "UNSAT: No failures were detected within the given scopes.");
        reportDatum("Solving time", solvingTime);

    }

    @Override
    public void minimizing (Object command, int before) {
        reportDatum("Unsat core size", before);
        reportTitle("Minimizing unsat core");
    }

    @Override
    public void minimized (Object command, int before, int after) {
           reportDatum("Unsat core size", after);
    }

    @Override
    public void warning(final ErrorWarning e) {
        ++warnings;
        System.err.println("Warning #" + warnings);
        System.err.println(e.msg.trim());
    }


    private int warnings = 0;



    private long t_zero = System.currentTimeMillis();

    private long time()
      { return System.currentTimeMillis() - t_zero; }

    private void reportTitle(String phaseName)
      { System.out.println();
          String s = String.format("%08d  %s", time(), phaseName);
        System.out.println(s);
        System.out.println(); }

    private void reportDatum(String name, int value)

      { String s = String.format("    * %-23s : %d", name, value);
        System.out.println(s); }

    private void reportDatum(String name, long value)
      { String s = String.format("    * %-23s : %d", name, value);
        System.out.println(s); }

    private void reportDatum(String name, String value)
      { String s = String.format("    * %-23s : %s", name, value);
        System.out.println(s); }


    public void new_BQ_witness_found(String bq_found) {
    	String s = String.format("     * %s", bq_found);
    	System.out.println(s);
    }
    
}

