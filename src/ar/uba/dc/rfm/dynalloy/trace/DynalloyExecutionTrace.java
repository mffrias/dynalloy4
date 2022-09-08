package ar.uba.dc.rfm.dynalloy.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.util.DynalloyPrinter;

/**
 * Represents a state in a Dynalloy execution trace.
 * An execution trace is represented as a tree, with nodes representing
 * program calls and leafs representing atomic operations.
 * @author pbendersky
 *
 */
public abstract class DynalloyExecutionTrace {

	private DynalloyProgram program;

	private DynalloyExecutionTrace parent;

	private boolean value;

	private boolean visible;

	private Map<AlloyExpression, Object> variableValues = new HashMap<AlloyExpression, Object>();

	private NameSpace nameSpace;

	public DynalloyExecutionTrace() {
		super();
	}

	public DynalloyExecutionTrace(DynalloyProgram program) {
		this();
		setProgram(program);
	}

	/**
	 * Returns the children nodes of this execution trace.
	 * @return
	 */
	public abstract List<DynalloyExecutionTrace> getChildren();

	/**
	 * Adds a child node to this execution trace instance.
	 * @param trace
	 */
	public abstract void addChild(DynalloyExecutionTrace trace);

	/**
	 * Appends a string representation of this node to a StringBuilder.
	 * @param sb StringBuilder where we'll be appending the values.
	 * @param level Indentation level of this node, to allow for hierarchichal
	 * display.
	 */
	public abstract void toStringAppend(StringBuilder sb, int level);

	/**
	 * Adds a parameter mapping between the caller and the calee. This is used
	 * to convert formal parameters to actual parameters and be able to evaluate
	 * them.
	 * @param callerName The name the caller is using for the parameter.
	 * @param calleeName The name the calee is using for the paramter.
	 */
	public abstract void addParameterMap(AlloyExpression callerExpression, AlloyExpression calleeExpression);

	/**
	 * Returns the name of a parameter in the callers naming convention.
	 * @param calleeName Callee name of the parameter we want to translate.
	 * @return Callers name of the parameter translated using the mapping
	 * present on this node.
	 */
	public abstract AlloyExpression getParameterMap(AlloyExpression calleeExpression);

	/**
	 * Adds a new variable (at DynAlloy level) to this execution context.
	 * @param variable
	 */
	public void addVariable(AlloyExpression variable) {
		// Intentionally blank. Override as needed.
	}

	/**
	 * Returns a list of the available variables (at DynAlloy level) in this context
	 * @return List of variables (parameters and locals) available in this context.
	 */
	public List<AlloyExpression> getVariables() {
		// Intentionally blank. Override as needed.
		return Collections.emptyList();
	}

	/**
	 * Adds a new variable value.
	 * @param expression AlloyExpression representing the DynAlloy variable we are assigning to.
	 * @param value Value as returned by the Alloy evaluator
	 */
	public void addVariableValue(AlloyExpression expression, Object value) {
		this.variableValues.put(expression, value);
	}

	/**
	 * Returns the map with the alloy variables stored in this trace.
	 * The keys of the map are variables in DynAlloy convention (without alloy suffixes)
	 * @return Map with the variable values
	 */
	public Map<AlloyExpression, Object> getVariableValues() {
		return this.variableValues;
	}

	public Map<AlloyExpression, Object> getTraceValues() {
		Map<AlloyExpression, Object> accumulatedValues = new HashMap<AlloyExpression, Object>();
		DynalloyExecutionTrace ancestor = getValidAncestor();
		addAccumulatedValues(accumulatedValues, ancestor);
		for (DynalloyExecutionTrace trace : ancestor.getChildren()) {
			if (trace == this) {
				break;
			}
			addAccumulatedValues(accumulatedValues, trace);
		}
		return accumulatedValues;
	}

	private void addAccumulatedValues(
			Map<AlloyExpression, Object> accumulatedValues,
			DynalloyExecutionTrace trace) {
		for (AlloyExpression a : trace.getVariables()) {
			if (!accumulatedValues.containsKey(a)) {
				accumulatedValues.put(a, null);
			}
		}
		for (Map.Entry<AlloyExpression, Object> entry : trace
				.getVariableValues().entrySet()) {
			accumulatedValues.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Returns a trace string representing this node.
	 * @return
	 */
	public String toTraceString() {
		StringBuilder sb = new StringBuilder();
		toStringAppend(sb, 0);
		return sb.toString();
	}

	/**
	 * Sets the DynalloyProgram for this node.
	 * @param program DynalloyProgram this node represents
	 */
	public void setProgram(DynalloyProgram program) {
		this.program = program;
	}

	/**
	 * Returns the DynalloyProgram this node represents.
	 * @return DynalloyProgram this node represents.
	 */
	public DynalloyProgram getProgram() {
		return program;
	}

	/**
	 * Sets the parent node.
	 * @param parent Parent node.
	 */
	public void setParent(DynalloyExecutionTrace parent) {
		this.parent = parent;
	}

	/**
	 * Returns this node's parent.
	 * @return Node's parent.
	 */
	public DynalloyExecutionTrace getParent() {
		return parent;
	}

	/**
	 * Returns the closest ancestor node that is visible and has a true value
	 */
	public DynalloyExecutionTrace getValidAncestor() {
		DynalloyExecutionTrace ret = getParent();
		while (!(ret.isVisible() && ret.isValue())) {
			ret = ret.getParent();
		}
		return ret;
	}

	@Override
	public String toString() {
		if (getProgram() != null) {
			DynalloyPrinter printer = new DynalloyPrinter();
			printer.setGrammar(DynalloyPrinter.DynalloyGrammar.C_LIKE);
			String string = (String) getProgram().accept(printer);
			return string;
		} else {
			return super.toString();
		}
	}

	/**
	 * Sets the truth value of this node. The truth value is generated when
	 * evaluating the counter example generated by Alloy.
	 * @param value Truth value to set.
	 */
	public void setValue(boolean value) {
		this.value = value;
	}

	/**
	 * Returns the truth value of this node.
	 * @return Truth value of this node.
	 */
	public boolean isValue() {
		return value;
	}

	/**
	 * Sets the visibility of this node. Nodes that are not visible are created
	 * for convenience purposes when tracing the program and removed before
	 * showing the trace to the user.
	 * @param visible Value indicating if this node it to be shown to the user.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns true if this node is to be shown to the user, false otherwise.
	 * @return Value indicating if the node should be visible to the user.
	 */
	public boolean isVisible() {
		return visible;
	}

	public void setNameSpace(NameSpace nameSpace) {
		this.nameSpace = nameSpace;
	}

	public NameSpace getNameSpace() {
		return nameSpace;

	}

	private final Map<String, TreeSet<Integer>> usesOf = new HashMap<String, TreeSet<Integer>>();

	public void addUseOf(String name, int index) {
		if (!usesOf.containsKey(name))
			usesOf.put(name, new TreeSet<Integer>());
		
		TreeSet<Integer> instants = usesOf.get(name);
		instants.add(index);
		
	}

	public String getAlloyIncarnation(String varName) {
		return null;
	}

	public boolean usesName(String name) {
		return usesOf.containsKey(name);
	}

	public Integer firstUseOf(String name) {
		return usesOf.get(name).first();
	}

	public Integer lastUseOf(String name) {
		return usesOf.get(name).last();
	}

	private final Map<String,Integer> entryInstants = new HashMap<String,Integer>();
	
	public void setEntryInstant(String name, int entryInstant) {
		entryInstants.put(name, entryInstant);
	}
	
	public int getEntryInstant(String name) {
		return entryInstants.get(name);
	}

}
