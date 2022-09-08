package ar.uba.dc.rfm.alloy.ast.expressions;

public final class ExprIntLiteral extends AlloyExpression {

	private int intLiteral;

	public ExprIntLiteral(int literal) {
		super();
		if (literal < 0)
			throw new IllegalArgumentException("Invalid literal value: "
					+ literal + ". Only positive literal are allowd");
		this.intLiteral = literal;
	}

	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	public int getIntLiteral() {
		return intLiteral;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + intLiteral;
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
		final ExprIntLiteral other = (ExprIntLiteral) obj;
		if (intLiteral != other.intLiteral)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new Integer(intLiteral).toString();
	}

}
