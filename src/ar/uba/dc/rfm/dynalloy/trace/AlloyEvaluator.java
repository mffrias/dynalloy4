package ar.uba.dc.rfm.dynalloy.trace;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;
import ar.uba.dc.rfm.alloy.util.PrefixedExpressionPrinter;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyEvaluator {

	private A4Solution answer;
	private Module world;
	private SpecContext context;

	public AlloyEvaluator(Module world, A4Solution answer) {
		super();
		setWorld(world);
		setAnswer(answer);

		// add skolems
		for (ExprVar a : answer.getAllSkolems()) {
			world.addGlobal(a.label, a);
		}

		variablePrefix = null;
	}

	public A4Solution getAnswer() {
		return answer;
	}

	public void setAnswer(A4Solution answer) {
		this.answer = answer;
	}

	public Module getWorld() {
		return world;
	}

	public void setWorld(Module world) {
		this.world = world;
	}

	public Object evaluate(AlloyFormula expression,
			DynalloyExecutionTrace currentExecutionTrace) {
		PrefixedExpressionPrinter prefixedPrinter = buildPrefixedPrinter(currentExecutionTrace);
		FormulaPrinter fp = new FormulaPrinter(prefixedPrinter);
		String alloyString = (String) expression.accept(fp);
		return evaluate(alloyString);
	}

	public Object evaluate(AlloyExpression expression,
			DynalloyExecutionTrace currentExecutionTrace) {
		PrefixedExpressionPrinter prefixedPrinter = buildPrefixedPrinter(currentExecutionTrace);
		String alloyString = (String) expression.accept(prefixedPrinter);
		return evaluate(alloyString);
	}

	private PrefixedExpressionPrinter buildPrefixedPrinter(
			DynalloyExecutionTrace currentExecutionTrace) {
		PrefixedExpressionPrinter prefixedPrinter = new PrefixedExpressionPrinter(
				currentExecutionTrace);
		FormulaPrinter formulaPrinter = new FormulaPrinter();

		formulaPrinter.setExpressionVisitor(prefixedPrinter);
		prefixedPrinter.setFormulaVisitor(formulaPrinter);
		
		prefixedPrinter.setVariablePrefix(variablePrefix);
		return prefixedPrinter;
	}

	Object evaluate(String alloyString) {

		//System.out.println("evaluator: " + alloyString);
		try {
			Expr expr = CompUtil.parseOneExpression_fromString(getWorld(),
					alloyString);

			Object eval = getAnswer().eval(expr);

			return eval;

		} catch (Exception e) {
			return "{can't evaluate}";
		}
	}

	public void setContext(SpecContext context) {
		this.context = context;
	}

	public SpecContext getContext() {
		return context;
	}

	private String variablePrefix;

	public String getVariablePrefix() {
		return variablePrefix;
	}

	public void setVariablePrefix(String variablePrefix) {
		this.variablePrefix = variablePrefix;

	}
	
}
