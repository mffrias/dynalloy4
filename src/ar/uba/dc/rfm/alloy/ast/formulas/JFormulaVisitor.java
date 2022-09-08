package ar.uba.dc.rfm.alloy.ast.formulas;

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;

public class JFormulaVisitor extends FormulaVisitor implements
		IJDynAlloyFormulaVisitor {

	@Override
	public Object visit(IfFormula ifFormula) {
		Vector<Object> result = new Vector<Object>();
		Object cond = ifFormula.getCondition().accept(this);
		Object left = ifFormula.getLeft().accept(this);
		Object right = ifFormula.getRight().accept(this);
		result.add(cond);
		result.add(left);
		result.add(right);
		return result;
	}

	@Override
	public Object visit(IffFormula iffFormula) {
		Vector<Object> result = new Vector<Object>();
		Object left = iffFormula.getLeft().accept(this);
		Object right = iffFormula.getRight().accept(this);
		result.add(left);
		result.add(right);
		return result;
	}

	public JFormulaVisitor(ExpressionVisitor visitor) {
		super(visitor);
		visitor.setFormulaVisitor(this);
	}

	@Override
	public Object visit(QuantifiedFormula n) {
		Vector<Object> setResults = new Vector<Object>();
		for (String name : n.getNames()) {
			AlloyExpression setOf = n.getSetOf(name);
			Object r = setOf.accept(this.getDfsExprVisitor());
			setResults.add(r);
		}
		Object formulaResult = n.getFormula().accept(this);

		Vector<Object> result = new Vector<Object>();
		result.add(setResults);
		result.add(formulaResult);
		return result;
	}

	public Object visit(PredicateCallAlloyFormula predicateCallAlloyFormula) {
		Vector<Object> result = new Vector<Object>();
		for (AlloyExpression alloyExpression : predicateCallAlloyFormula
				.getArguments()) {
			Object r = alloyExpression.accept(getDfsExprVisitor());
			result.add(r);
		}
		return result;
	}

}
