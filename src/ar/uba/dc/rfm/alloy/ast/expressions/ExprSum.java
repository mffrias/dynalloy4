package ar.uba.dc.rfm.alloy.ast.expressions;

import java.util.List;

public class ExprSum extends AlloyExpression {

	public ExprSum(List<String> names, List<AlloyExpression> sets,
			AlloyExpression expression) {
		super();
		this.names = names;
		this.sets = sets;
		this.expression = expression;
	}

	private final List<String> names;
	
	private final List<AlloyExpression> sets;
	
	private final AlloyExpression expression;
	
	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	public List<String> getNames() {
		return names;
	}

	public AlloyExpression getSetOf(String name) {
		int indexOf = names.indexOf(name);
		return sets.get(indexOf);
	}

	public AlloyExpression getExpression() {
		return expression;
	}

}
