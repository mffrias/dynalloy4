package ar.uba.dc.rfm.alloy.ast.expressions;

public class ExprIntersection extends AlloyExpression {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		ExprIntersection other = (ExprIntersection) obj;
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

	public AlloyExpression getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

	public ExprIntersection(AlloyExpression left, AlloyExpression right) {
		super();
		this.left = left;
		this.right = right;
	}

	private final AlloyExpression left;
	
	private final AlloyExpression right;
	
	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

}
