package ar.uba.dc.rfm.dynalloy.ast.programs;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;

public final class WhileProgram extends DynalloyProgram {

	private final PredicateFormula condition;
	
	private final DynalloyProgram body;
	
	private final Position position;
	@Override
	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

	public WhileProgram(PredicateFormula condition, DynalloyProgram body, Position position) {
		this.condition = condition;
		this.body = body;
		this.position = position;
	}

	public PredicateFormula getCondition() {
		return condition;
	}

	public DynalloyProgram getBody() {
		return body;
	}

	public Position getPosition() {
		return position;
	}

}
