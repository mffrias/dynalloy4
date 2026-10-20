package ar.uba.dc.rfm.dynalloy;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The options of one DynAlloy-to-Alloy translation, and of the console's analysis. TACO's
 * {@code DynalloyToAlloyManager} creates one per translation with {@code new DynAlloyOptions()}
 * and sets every option except the output file name and {@code alloyOptions}; the console
 * ({@code DynAlloyConsole}) and the visualizer make their own. Who uses what:
 * <ul>
 * <li>{@code loopUnroll}, {@code strictUnrolling} and {@code remove_exit_while_guard}:
 *     {@link DynAlloyTranslator}, for the unrolling;</li>
 * <li>{@code removeQuantifiers}, the assertion id and the module under analysis:
 *     {@code DynalloyXlatorVisitor} ({@link DynAlloyAnalyzer} also reads
 *     {@code removeQuantifiers}, for the traces);</li>
 * <li>{@code programSpecs}: only TACO's scope inference;</li>
 * <li>{@code run_alloy_analyzer}, the output file name and {@code alloyOptions}: only
 *     {@link DynAlloyAnalyzer} and the visualizer; {@link DynAlloyCompiler#compile} ignores them,
 *     and {@code AlloyAnalyzer} ignores {@code alloyOptions} (docs/SOLVERS.md);</li>
 * <li>{@code build_dynalloy_trace}: only printed in the header of the Alloy file, as are
 *     {@code assertionId}, {@code loopUnroll}, {@code removeQuantifiers} and
 *     {@code strictUnrolling}.</li>
 * </ul>
 * A new instance has {@code loopUnroll} 0, every flag false, no assertion id, no module under
 * analysis, no output file name, an empty set of program specs, and {@code alloyOptions} with the
 * solver {@code MiniSatJNI}. {@link #DEFAULT_DYNALLOY_OPTIONS} differs only in {@code loopUnroll},
 * which is 3. See docs/ARCHITECTURE.md ("DynAlloyOptions").
 *
 * <p>Static state: {@link #DEFAULT_DYNALLOY_OPTIONS}, one mutable instance for the whole JVM;
 * nothing in dynalloy4 changes it.
 */
public class DynAlloyOptions {

	public DynAlloyOptions() {
		this.alloyOptions.solver = A4Options.SatSolver.MiniSatJNI;
	}

	/**
	 * The Alloy options for {@link DynAlloyAnalyzer}, which passes them to
	 * {@code AlloyAnalyzer.analyzeCommand}, which ignores them: the solver comes from
	 * {@code -Ddynalloy.solver} (docs/SOLVERS.md). The constructor sets the solver to
	 * {@code MiniSatJNI}; the visualizer's solver menu changes it.
	 */
	public A4Options alloyOptions = new A4Options();

	private int loopUnroll;

	private boolean strictUnrolling;

	private boolean removeQuantifiers;

	private String assertionId;

	private boolean build_dynalloy_trace;

	private boolean run_alloy_analyzer;
	
	private boolean remove_exit_while_guard;

	private String output_filename;
	
	private String module_under_analysis_name;

	private Set<AlloyFormula> programSpecs = new HashSet<>();

	/**
	 * Static: the defaults that the console, its help text and the visualizer start from: those of
	 * a new instance, with {@code loopUnroll} 3. One mutable instance for the whole JVM, so a
	 * setter called on it would change it for every later user; nothing in dynalloy4 does. TACO
	 * does not use it.
	 */
	public static final DynAlloyOptions DEFAULT_DYNALLOY_OPTIONS = build_default_dynalloy_options();

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("moduleUnderAnalysis= " + module_under_analysis_name);
		buff.append("assertionId= "
				+ (assertionId == null ? "null" : assertionId) + ",\n");
		buff.append("loopUnroll= " + loopUnroll + ",\n");
		buff.append("removeQuantifiers= "
				+ (removeQuantifiers ? "true" : "false") + ",\n");
		buff.append("strictUnrolling= " + (strictUnrolling ? "true" : "false")
				+ ",\n");
		return buff.toString();
	}

	private static DynAlloyOptions build_default_dynalloy_options() {
		String moduleUnderAnalysis = null;
		String assertionId = null;
		int loopUnroll = 3;
		boolean strictUnrolling = false;
		boolean removeQuantifiers = false;
		boolean run_alloy_analyzer = false;
		boolean build_dynalloy_trace = false;
		boolean remove_exit_while_guard = false;

		DynAlloyOptions defaultOptions = new DynAlloyOptions();
		defaultOptions.setModuleUnderAnalysis(moduleUnderAnalysis);
		defaultOptions.setAssertionToCheck(assertionId);
		defaultOptions.setLoopUnroll(loopUnroll);
		defaultOptions.setStrictUnroll(strictUnrolling);
		defaultOptions.setRemoveQuantifier(removeQuantifiers);
		defaultOptions.setRunAlloyAnalyzer(run_alloy_analyzer);
		defaultOptions.setBuildDynAlloyTrace(build_dynalloy_trace);
		defaultOptions.setRemoveExitWhileGuard(remove_exit_while_guard);

		return defaultOptions;
	}

	public void setRemoveExitWhileGuard(boolean remove_exit_while_guard) {
		this.remove_exit_while_guard = remove_exit_while_guard;
	}

	/**
	 * Sets the module under analysis. The translation uses its id as the key of the variables of
	 * the object invariants' arithmetic (the first map of {@link DynAlloyCompiler#compile}), as
	 * the type of {@code thiz} when only the invariant predicates mention it, and, with an input
	 * to fix, as the prefix of the variables whose index range it widens. TACO sets the id of its
	 * first source module, the class under analysis.
	 *
	 * @param modName the module id
	 */
	public void setModuleUnderAnalysis(String modName){
		this.module_under_analysis_name = modName;
	}

	public String getModuleUnderAnalysis(){
		return this.module_under_analysis_name;
	}
	
	public int getLoopUnroll() {
		return loopUnroll;
	}

	public boolean getStrictUnrolling() {
		return strictUnrolling;
	}

	public boolean getRemoveQuantifiers() {
		return removeQuantifiers;
	}

	public String getAssertionId() {
		return assertionId;
	}

	public boolean getBuildDynAlloyTrace() {
		return build_dynalloy_trace;
	}

	public boolean getRunAlloyAnalyzer() {
		return run_alloy_analyzer;
	}

	/**
	 * Returns false, always: there is no verbosity option. {@code DynAlloyAnalyzer} passes it to
	 * its reporter at each analysis.
	 *
	 * @return false
	 */
	public boolean getVerbosity() {
		return false;
	}

	public void setRemoveQuantifier(boolean remove_quantifiers) {
		this.removeQuantifiers = remove_quantifiers;
	}

	/**
	 * Sets formulas for TACO's scope inference: TACO passes the object invariants of the module
	 * under analysis and the {@code requires} and {@code ensures} of the method under analysis,
	 * and gives {@link #getProgramSpecs()} to its {@code DynAlloyProgramScopeInferencePlugin}.
	 * dynalloy4 does not read them.
	 *
	 * @param specFormulas the formulas; the set is kept, not copied
	 */
	public void setProgramSpecs(Set<AlloyFormula> specFormulas) {
		programSpecs = specFormulas;
	}

	public Set<AlloyFormula> getProgramSpecs(){
		return programSpecs;
	}

	public void setStrictUnroll(boolean strict_unrolling) {
		this.strictUnrolling = strict_unrolling;
	}

	public void setRunAlloyAnalyzer(boolean run_alloy_analyzer) {
		this.run_alloy_analyzer = run_alloy_analyzer;
	}

	public void setLoopUnroll(int unroll) {
		this.loopUnroll = unroll;
	}

	/**
	 * Sets the id of the assertion. The translation takes what follows its first six characters
	 * ({@code check_}) as the id of the program under analysis, the key of that program's typing
	 * in the contract typings of {@link DynAlloyCompiler#compile}; with {@code removeQuantifiers},
	 * the quantifiers are removed only from an assertion with this id. It does not choose the
	 * assertion: only one is translated (docs/KNOWN-LIMITATIONS.md, "Translation"). It is also
	 * printed in the header of the Alloy file.
	 *
	 * @param assertionToCheck {@code check_<program>} (TACO:
	 *        {@code check_<package>_<Class>_<method>}); a null id, or one shorter than six
	 *        characters, makes the translation fail
	 */
	public void setAssertionToCheck(String assertionToCheck) {
		this.assertionId = assertionToCheck;
	}

	public void setBuildDynAlloyTrace(boolean buildDynAlloyTrace) {
		this.build_dynalloy_trace = buildDynAlloyTrace;
	}

	public boolean getRemoveExitWhileGuard() {
		return remove_exit_while_guard;
	}
	
	public void setOutputFilename(String _filename) {
		this.output_filename = _filename;
	}
	
	public String getOutputFilename() {
		return this.output_filename;
	}

}
