package ar.uba.dc.rfm.dynalloy.xlator;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * The translation of a piece of a DynAlloy program: its Alloy formula, over indexed variables
 * ({@code x_0}, {@code x_1}, ...), and the local variables that the program calls in it
 * introduced, with their types. {@link ProgramTranslator} returns one from every visit;
 * for a program call it comes from {@link SpecContext#invokeProgram}, and its locals are the
 * called program's local variables, renamed with the call's prefix and indexed
 * ({@code l0_temp_1}). Every other construct has no locals of its own; a sequence or a choice
 * merges those of its parts. {@link DynalloyXlatorVisitor} makes the locals of a program body
 * parameters of the program's predicate, and those of the assertion's program quantified
 * variables of the assertion.
 */
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
