package ar.uba.dc.rfm.alloy.ast.formulas;

import static ar.uba.dc.rfm.alloy.util.AlloyPrinter.increaseIdentation;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.util.ExpressionPrinter;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;

public class JFormulaPrinter extends FormulaPrinter implements
		IJDynAlloyFormulaVisitor {

	public JFormulaPrinter() {
		this(new ExpressionPrinter());
	}

	public JFormulaPrinter(ExpressionVisitor expressionPrinter) {
		super(expressionPrinter);
		expressionPrinter.setFormulaVisitor(this);
	}

	@Override
	public Object visit(IfFormula ifFormula) {
		Object c = ifFormula.getCondition().accept(this);
		Object l = ifFormula.getLeft().accept(this);
		Object r = ifFormula.getRight().accept(this);

		String cond = (String) c;
		String left = (String) l;
		String right = (String) r;

		StringBuffer buff = new StringBuffer();
		buff.append("{\n");
		buff.append(cond);
		buff.append("} => {\n");
		buff.append(increaseIdentation(left) + "\n");
		buff.append("}else{\n");
		buff.append(increaseIdentation(right) + "\n");
		buff.append("}\n");

		return buff.toString();
	}

	@Override
	public Object visit(IffFormula iffFormula) {
		Object l = iffFormula.getLeft().accept(this);
		Object r = iffFormula.getRight().accept(this);

		String left = (String) l;
		String right = (String) r;

		StringBuffer buff = new StringBuffer();
		buff.append(left);
		buff.append("\n");

		buff.append("iff");
		buff.append("\n");

		buff.append(right);
		buff.append("\n");

		return buff.toString();
	}

	@Override
	public Object visit(QuantifiedFormula n) {

		List<String> decls = new LinkedList<String>();
		for (String name : n.getNames()) {
			AlloyExpression setOf = n.getSetOf(name);
			String setOfStr = (String) setOf.accept(this.getDfsExprVisitor());

			String varDeclStr = String.format("%s:%s", name, setOfStr);
			decls.add(varDeclStr);
		}

		String formulaStr = (String) n.getFormula().accept(this);
		StringBuffer buff = new StringBuffer();

		// operator
		String operString;
		if (n.isExists())
			operString = "some ";
		else if (n.isForAll())
			operString = "all ";
		else if (n.isLone())
			operString = "lone ";
		else if (n.isNone())
			operString = "no ";
		else if (n.isOne())
			operString = "one ";
		else
			throw new IllegalArgumentException("Operator not supported in "
					+ n.toString());
		buff.append(operString);

		// (name:set)+
		for (int i = 0; i < decls.size(); i++) {
			if (i > 0) {
				buff.append(",");
				buff.append("\n");
			}
			buff.append(decls.get(i));
		}

		buff.append(" | {\n");
		buff.append(increaseIdentation(formulaStr));
		buff.append("\n");
		buff.append("}");
		return buff.toString();
	}

	public Object visit(PredicateCallAlloyFormula predicateCallAlloyFormula) {
		StringBuffer buffer = new StringBuffer();
		for (AlloyExpression e : predicateCallAlloyFormula.getArguments()) {
			if (buffer.length() != 0)
				buffer.append(",");
			buffer.append(e.accept(this.getDfsExprVisitor()));
		}
		return "callSpec " + predicateCallAlloyFormula.getProgramId() + "[" + buffer.toString() + "]";
	}

}
