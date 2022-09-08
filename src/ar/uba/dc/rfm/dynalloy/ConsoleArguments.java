package ar.uba.dc.rfm.dynalloy;

import java.util.HashSet;
import java.util.Set;

class ConsoleArguments {

	static String UNROLL = "--unroll";

	static String STRIC_UNROLL = "--strictUnroll";

	static String REMOVE_QUANTIFIERS = "--removeQuantifiers";

	static String RUN_ALLOY_ANALYZER = "--runAlloyAnalyzer";

	static String BUILD_DYNALLOY_TRACE = "--buildDynAlloyTrace";

	static String REMOVE_EXIT_WHILE_GUARD = "--removeExitWhileGuard";
	
	static String INPUT = "--input";

	static String OUTPUT = "--output";

	static String ASSERTION = "--assertion";

	private final Integer unroll;

	private final Boolean removeQuantifiers;

	private final Boolean run_alloy_analyzer;

	private final Boolean build_dynalloy_trace;

	private final Boolean remove_exit_while_guard;

	private final String assertionToCheck;

	private final String input;

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

	private final String output;

	private final Boolean strictUnrolling;

	private Integer parseOptionalInteger(String[] args, String fieldId) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(fieldId)) {
				int fieldValue = Integer.parseInt(args[i + 1]);
				return fieldValue;
			}
		}
		return null;
	}

	private Boolean parseOptionalBoolean(String[] args, String fieldId) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(fieldId)) {
				Boolean fieldValue = Boolean.parseBoolean(args[i + 1]);
				return fieldValue;
			}
		}
		return null;
	}

	private String parseOptionalString(String[] args, String fieldId) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(fieldId)) {
				String fieldValue = args[i + 1];
				return fieldValue;
			}
		}
		return null;
	}

	private String parseMandatoryString(String[] args, String fieldId) throws ArgumentException {
		String fieldValue = parseOptionalString(args, fieldId);
		if (fieldValue == null)
			throw new ArgumentException(fieldId + " is mandatory argument");
		return fieldValue;
	}

	public ConsoleArguments(String[] args) throws ArgumentException {
		super();

		checkArguments(args);

		unroll = parseOptionalInteger(args, UNROLL);

		removeQuantifiers = parseOptionalBoolean(args, REMOVE_QUANTIFIERS);

		assertionToCheck = parseOptionalString(args, ASSERTION);

		strictUnrolling = parseOptionalBoolean(args, STRIC_UNROLL);

		run_alloy_analyzer = parseOptionalBoolean(args, RUN_ALLOY_ANALYZER);

		build_dynalloy_trace = parseOptionalBoolean(args, BUILD_DYNALLOY_TRACE);

		remove_exit_while_guard = parseOptionalBoolean(args, REMOVE_EXIT_WHILE_GUARD);

		output = parseOptionalString(args, OUTPUT);

		input = parseMandatoryString(args, INPUT);

	}

	private void checkArguments(String[] args) throws ArgumentException {
		Set<String> fieldIds = new HashSet<String>();
		fieldIds.add(UNROLL);
		fieldIds.add(REMOVE_QUANTIFIERS);
		fieldIds.add(RUN_ALLOY_ANALYZER);
		fieldIds.add(REMOVE_EXIT_WHILE_GUARD);
		fieldIds.add(ASSERTION);
		fieldIds.add(STRIC_UNROLL);
		fieldIds.add(INPUT);
		fieldIds.add(OUTPUT);

		Set<String> knownArguments = new HashSet<String>();
		for (int i = 0; i < args.length; i = i + 2) {
			if (!fieldIds.contains(args[i])) {
				throw new ArgumentException("Unknown argument :" + args[i]);
			}

			if (knownArguments.contains(args[i]))
				throw new ArgumentException(args[i] + " argument is repeated");

			knownArguments.add(args[i]);
		}
		if ((args.length % 2) != 0) {
			if (!fieldIds.contains(args[args.length - 1]))
				throw new ArgumentException("Unknown argument :" + args[args.length - 1]);
		}
	}

	public Integer getUnroll() {
		return unroll;
	}

	public Boolean getRemoveQuantifiers() {
		return removeQuantifiers;
	}

	public Boolean getRunAlloyAnalyzer() {
		return run_alloy_analyzer;
	}

	public Boolean getBuildDynAlloyTrace() {
		return build_dynalloy_trace;
	}

	public String getAssertionToCheck() {
		return assertionToCheck;
	}

	public Boolean getStrictUnrolling() {
		return strictUnrolling;
	}

	public Boolean getRemoveExitWhileGuard() {
		return remove_exit_while_guard;
	}
	
	public static String printHelp() {
		DynAlloyOptions defaultOptions = DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS;

		StringBuffer sb = new StringBuffer();
		sb.append("usage: dynalloy [--input <filename>] [--output <filename>] [--unroll <integer>] [--assertion <assertionName>] [--strictUnroll <boolean>] [--removeQuantifiers <boolean>] [--applySimplifications <boolean>] \n");

		sb.append("Dynalloy mandatory arguments:\n");
		sb.append("--input <filename>                 set name of input dynalloy model filename\n");
		sb.append("--output <filename>                set name of oupt alloy model filename\n");

		sb.append("Dynalloy optional arguments:\n");
		sb.append("--assertion <assertionName>        set the partial correctness assertion to check\n");
		sb.append("--unroll <integer>                 set number of unrollings, default is " + defaultOptions.getLoopUnroll() + "\n");
		sb.append("--strictUnroll <boolean>           strict unroll translation, default is " + defaultOptions.getStrictUnrolling() + "\n");
		sb.append("--removeQuantifiers <boolean>      remove assertion quantifiers, default is " + defaultOptions.getRemoveQuantifiers() + "\n");
		sb.append("--runAlloyAnalyzer <boolean>   run Alloy Analyzer to find counterexample/instance. Default value is " + defaultOptions.getRunAlloyAnalyzer()
				+ "\n");
		sb.append("--buildDynAlloyTrace <boolean>   create a DynAlloy trace if a counterexample is found. Default value is "
				+ defaultOptions.getBuildDynAlloyTrace() + "\n");
		sb.append("--removeExitWhileGuard <boolean> remove assume not while condition when unrolling, default is "
				+ defaultOptions.getRemoveExitWhileGuard() + "\n");

		return sb.toString();
	}

}