package ar.uba.dc.rfm.alloy.ast.expressions;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class ExprIfCondition extends AlloyExpression {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExprIfCondition other = (ExprIfCondition) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	public AlloyFormula getCondition() {
		return condition;
	}

	public AlloyExpression getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

	public ExprIfCondition(AlloyFormula condition, AlloyExpression left,
			AlloyExpression right) {
		super();
		this.condition = condition;
		this.left = left;
		this.right = right;
	}

	private final AlloyFormula condition;

	private final AlloyExpression left;

	private final AlloyExpression right;

	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}
	
	
	@Override
	public String toString(){
		return "IF (" + this.condition.toString() + ") THEN (" + this.left.toString() + ") ELSE (" + this.right.toString() + ")";
	}

}
