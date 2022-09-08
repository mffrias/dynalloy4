package ar.uba.dc.rfm.alloy.ast.formulas;

import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public interface IProgramCall {

	public abstract String getProgramId();

	public abstract List<AlloyExpression> getArguments();

	public abstract boolean isSuperCall();

	public abstract boolean isStatic();

}
