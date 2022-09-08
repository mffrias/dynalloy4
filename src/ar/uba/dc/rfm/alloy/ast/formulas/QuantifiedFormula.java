package ar.uba.dc.rfm.alloy.ast.formulas;

import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public final class QuantifiedFormula extends AlloyFormula {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
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
		QuantifiedFormula other = (QuantifiedFormula) obj;
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
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
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

	public enum Operator {
		FOR_ALL, EXISTS, ONE, LONE, NONE
	}

	public QuantifiedFormula(Operator operator, List<String> names,
			List<AlloyExpression> sets, AlloyFormula formula) {
		super();
		this.operator = operator;
		this.names = names;
		this.sets = sets;
		this.formula = formula;
	}

	private final List<String> names;

	private final List<AlloyExpression> sets;
	
	public List<AlloyExpression> getSets(){
		return sets;
	}

	private final Operator operator;

	private final AlloyFormula formula;

	@Override
	public Object accept(IFormulaVisitor visitor) {
//		if (!(visitor instanceof IJDynAlloyFormulaVisitor))
//			throw new IllegalArgumentException(
//					this.getClass().getName()
//							+ " is not supposed to be called using this kind of visitor: "
//							+ visitor.getClass().getName());
//		else {
//			IJDynAlloyFormulaVisitor dynjalloyFormulaVisitor = (IJDynAlloyFormulaVisitor) visitor;
//			return dynjalloyFormulaVisitor.visit(this);
//		}
		return visitor.visit(this);
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public boolean isForAll() {
		return operator == Operator.FOR_ALL;
	}

	public boolean isExists() {
		return operator == Operator.EXISTS;
	}

	public boolean isSome() {
		return operator == Operator.EXISTS;
	}

	public boolean isNone() {
		return operator == Operator.NONE;
	}

	public boolean isOne() {
		return operator == Operator.ONE;
	}

	public boolean isLone() {
		return operator == Operator.LONE;
	}

	public Operator getOperator() {
		return operator;
	}

	public AlloyExpression getSetOf(String name) {
		int indexOfName = names.indexOf(name);
		return sets.get(indexOfName);
	}

}
