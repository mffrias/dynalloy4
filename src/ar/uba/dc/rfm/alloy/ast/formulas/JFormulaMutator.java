package ar.uba.dc.rfm.alloy.ast.formulas;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula.Operator;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;

public class JFormulaMutator extends FormulaMutator implements
		IJDynAlloyFormulaVisitor {

	@Override
	public Object visit(IfFormula ifFormula) {
		Object c = ifFormula.getCondition().accept(this);
		Object l = ifFormula.getLeft().accept(this);
		Object r = ifFormula.getRight().accept(this);

		AlloyFormula condition = (AlloyFormula) c;
		AlloyFormula left = (AlloyFormula) l;
		AlloyFormula right = (AlloyFormula) r;

		return new IfFormula(condition, left, right);
	}

	@Override
	public Object visit(IffFormula iffFormula) {
		Object l = iffFormula.getLeft().accept(this);
		Object r = iffFormula.getRight().accept(this);

		AlloyFormula left = (AlloyFormula) l;
		AlloyFormula right = (AlloyFormula) r;

		return new IffFormula(left, right);
	}

	protected Stack<Set<AlloyVariable>> boundVariables = new Stack<Set<AlloyVariable>>();

	public JFormulaMutator() {
		this(new ExpressionMutator());
	}

	public JFormulaMutator(ExpressionMutator exprMutator) {
		super(exprMutator);
		exprMutator.setFormulaVisitor(this);
	}

	public boolean isBoundedVariable(AlloyVariable v) {
		for (Set<AlloyVariable> boundContext : boundVariables) {
			if (boundContext.contains(v))
				return true;
		}
		return false;
	}

	@Override
	public Object visit(QuantifiedFormula n) {
		Set<AlloyVariable> currentContext = new HashSet<AlloyVariable>();
		boundVariables.push(currentContext);

		List<String> names = new LinkedList<String>();
		List<AlloyExpression> exprs = new LinkedList<AlloyExpression>();
		for (String name : n.getNames()) {
			AlloyVariable v = AlloyVariable.buildNonMutableAlloyVariable(name);
			boundVariables.peek().add(v);
			AlloyExpression setOf = n.getSetOf(name);
			AlloyExpression expr = (AlloyExpression) setOf.accept(this
					.getExpressionMutator());
			names.add(name);
			exprs.add(expr);
		}
		AlloyFormula formula = (AlloyFormula) n.getFormula().accept(this);
		Operator operator = n.getOperator();

		boundVariables.pop();
		return new QuantifiedFormula(operator, names, exprs, formula);
	}

	public Object visit(PredicateCallAlloyFormula predicateCallAlloyFormula) {
		List<AlloyExpression> args = new LinkedList<AlloyExpression>();
		for (AlloyExpression arg : predicateCallAlloyFormula.getArguments()) {
			args.add((AlloyExpression) arg.accept(this.getExpressionMutator()));
		}
		return new PredicateCallAlloyFormula(predicateCallAlloyFormula.isSuperCall(), predicateCallAlloyFormula.getProgramId(), args, predicateCallAlloyFormula.isStatic());
	}
}
