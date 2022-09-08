package ar.uba.dc.rfm.alloy.ast.expressions;

public final class ExprUnary extends AlloyExpression {

	public enum Operator {
		CARDINALITY
	}

	private final AlloyExpression expression;

	private final Operator operator;

	public ExprUnary(Operator operator, AlloyExpression expression) {
		this.expression = expression;
		this.operator = operator;
	}

	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	public AlloyExpression getExpression() {
		return expression;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
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
		ExprUnary other = (ExprUnary) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (operator.equals(Operator.CARDINALITY)) {
			String toString = String.format("#(%s)", expression.toString());
			return toString;
		} else
			throw new IllegalStateException("Unknown operator " + operator);

	}

}
