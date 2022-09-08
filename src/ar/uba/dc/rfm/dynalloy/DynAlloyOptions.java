package ar.uba.dc.rfm.dynalloy;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;

public class DynAlloyOptions {

	public DynAlloyOptions() {
		this.alloyOptions.solver = A4Options.SatSolver.MiniSatJNI;
	}

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

	public boolean getVerbosity() {
		return false;
	}

	public void setRemoveQuantifier(boolean remove_quantifiers) {
		this.removeQuantifiers = remove_quantifiers;

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
