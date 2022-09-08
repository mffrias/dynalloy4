package ar.uba.dc.rfm.dynalloy.ast.programs;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;

public final class Assigment extends DynalloyProgram {

	@Override
	public String toString() {
		return left.toString() + ":=" + right.toString();
	}

	public Assigment(ExprVariable left, AlloyExpression right, Position position) {
		super();
		this.left = left;
		this.right = right;
		setPosition(position);
	}
	public Assigment(ExprVariable left, AlloyExpression right) {
		this(left,right,null);
	}

	public ExprVariable getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

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
		Assigment other = (Assigment) obj;
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

	private final ExprVariable left;
	private final AlloyExpression right;

	@Override
	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

}
