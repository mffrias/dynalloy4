package ar.uba.dc.rfm.dynalloy.xlator;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class AlloyFormulaWithLocals {

	public AlloyFormulaWithLocals(AlloyFormula formula, AlloyTyping locals) {
		super();
		this.formula = formula;
		this.locals = locals;
	}

	private final AlloyFormula formula;
	private final AlloyTyping locals;

	public AlloyFormula getFormula() {
		return formula;
	}

	public AlloyTyping getLocals() {
		return locals;
	}

}
