package ar.uba.dc.rfm.alloy.ast.formulas;


public interface IJDynAlloyFormulaVisitor extends IFormulaVisitor {

	Object visit(QuantifiedFormula n);

	Object visit(IffFormula iffFormula);

	Object visit(IfFormula ifFormula);
	
	Object visit(PredicateCallAlloyFormula predicateCallAlloyFormula);	

}
