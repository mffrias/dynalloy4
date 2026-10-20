package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

/**
 * What {@link CallingConvention#instantiate(List, String)} returns: the arguments of one call
 * of an action's or program's predicate, and the typing of the local variables the call
 * introduced, by indexed variable ({@code l2_temp_1}). {@link SpecContext#invokeProgram} turns
 * it into an {@link AlloyFormulaWithLocals}.
 */
public class CallingConventionResult {

	public CallingConventionResult(List<AlloyExpression> parameters,
			AlloyTyping locals) {
		super();
		this.parameters = parameters;
		this.locals = locals;
	}

	private final List<AlloyExpression> parameters;
	
	private final AlloyTyping locals;

	public List<AlloyExpression> getParameters() {
		return parameters;
	}

	public AlloyTyping getLocals() {
		return locals;
	}
	
	
}
