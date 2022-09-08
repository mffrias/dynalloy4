package ar.uba.dc.rfm.dynalloy.trace;

import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;

class IncarnationMutator extends ExpressionMutator {

	private static final int INITIAL_INSTANT = 0;

	public IncarnationMutator(DynalloyExecutionTrace pointOfEvaluation) {
		super();
		this.pointOfEvaluation = pointOfEvaluation;
	}

	private final DynalloyExecutionTrace pointOfEvaluation;

	private boolean undef = false;

	@Override
	public Object visit(ExprVariable n) {
		NameSpace nameSpace = pointOfEvaluation.getNameSpace();

		String varName;
		if (n.getVariable().isPreStateVar()) {
			varName = n.getVariable().getVariableId().getString();
		} else
			varName = n.getVariable().toString();

		if (nameSpace.containsName(varName)) {
			int entryInstant = pointOfEvaluation.getEntryInstant(varName);
			String str;
			if (n.getVariable().isPreStateVar()) {
				str = nameSpace.getIncarnation(varName, INITIAL_INSTANT);
			} else
				str = nameSpace.getIncarnation(varName, entryInstant);

			return ExprVariable.buildExprVariable(str);
		} else {
			undef = true;
			return super.visit(n);
		}
	}

	public boolean undef() {
		return undef;
	}

}
