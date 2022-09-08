package ar.uba.dc.rfm.alloy.ast.expressions;

import java.util.List;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class ExprComprehension extends AlloyExpression {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + ((sets == null) ? 0 : sets.hashCode());
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
		ExprComprehension other = (ExprComprehension) obj;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		if (sets == null) {
			if (other.sets != null)
				return false;
		} else if (!sets.equals(other.sets))
			return false;
		return true;
	}

	public List<String> getNames() {
		return names;
	}

	public List<AlloyExpression> getSets() {
		return sets;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public ExprComprehension(List<String> names, List<AlloyExpression> sets,
			AlloyFormula formula) {
		super();
		this.names = names;
		this.sets = sets;
		this.formula = formula;
	}

	private final List<String> names;
	private final List<AlloyExpression> sets;
	private final AlloyFormula formula;

	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	public AlloyExpression getSetOf(String name) {
		int indexOf = this.names.indexOf(name);
		return this.sets.get(indexOf);
	}

}
