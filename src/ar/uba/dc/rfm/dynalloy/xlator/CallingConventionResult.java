package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

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
