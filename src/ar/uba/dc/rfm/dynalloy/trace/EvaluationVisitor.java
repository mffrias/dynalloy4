package ar.uba.dc.rfm.dynalloy.trace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.PrefixedExpressionPrinter;
import ar.uba.dc.rfm.alloy.util.VarCollector;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class EvaluationVisitor extends FormulaVisitor {

	private SpecContext context;
	private AlloyEvaluator evaluator;
	private DynalloyExecutionTrace rootExecutionTrace = DynalloyExecutionTraceNode
			.createRootNode();
	private DynalloyExecutionTrace currentExecutionTrace = rootExecutionTrace;
	private final boolean evaluatingAssertion;
	private Stack<NotFormula> notFormulas = new Stack<NotFormula>();

	public EvaluationVisitor(SpecContext specContext,
			AlloyEvaluator alloyEvaluator, boolean evaluatingAssertion) {
		alloyEvaluator.setContext(specContext);
		this.setEvaluator(alloyEvaluator);
		this.setContext(specContext);
		this.evaluatingAssertion = evaluatingAssertion;
	}

	@Override
	public Object visit(AndFormula n) {
		DynalloyProgram dynalloy = getMapping().getDynAlloy(n);
		DynalloyExecutionTrace node = addExecutionTraceNode(dynalloy);
		currentExecutionTrace = node;

		AlloyFormula left = n.getLeft();
		AlloyFormula right = n.getRight();

		Object left_value = left.accept(this);
		Object right_value = right.accept(this);

		Boolean leftResult = (Boolean) left_value;
		Boolean rightResult = (Boolean) right_value;

		boolean retValue = leftResult && rightResult;

		currentExecutionTrace.setValue(retValue);
		currentExecutionTrace.setVisible(false);
		currentExecutionTrace = node.getParent();

		return retValue;
	}

	@Override
	public Object visit(EqualsFormula n) {
		boolean retValue;
		List<AlloyExpression> expressions = new LinkedList<AlloyExpression>();
		expressions.add(n.getLeft());
		expressions.add(n.getRight());

		Object o = evaluator.evaluate(n, currentExecutionTrace);
		if (o instanceof Boolean) {
			retValue = (Boolean) evaluator.evaluate(n, currentExecutionTrace);
		} else {
			retValue = true;
		}

		for (AlloyExpression expression : expressions) {
			Object paramResult = evaluator.evaluate(expression,
					currentExecutionTrace);
		}

		Map<String, TreeMap<Integer, String>> instantsOf;
		instantsOf = createInstantsOf(n);

		DynalloyExecutionTrace leaf = addExecutionTraceLeaf(n);

		if (leaf != null) {

			for (String name : instantsOf.keySet()) {
				TreeMap<Integer, String> map = instantsOf.get(name);
				for (Integer instant : map.keySet()) {
					leaf.addUseOf(name, instant);
					String alloyStr = map.get(instant);
					leaf.getNameSpace().addIncarnation(name, instant, alloyStr);
				}
			}

		}
		return retValue;
	}

	@Override
	public Object visit(OrFormula n) {
		DynalloyProgram dynalloy = getMapping().getDynAlloy(n);
		DynalloyExecutionTrace node = addExecutionTraceNode(dynalloy);

		currentExecutionTrace = node;

		AlloyFormula left = n.getLeft();
		AlloyFormula right = n.getRight();

		boolean retValue = (Boolean) left.accept(this);
		if (!retValue) {
			retValue = (Boolean) right.accept(this);
		}
		currentExecutionTrace.setValue(retValue);
		currentExecutionTrace.setVisible(false);
		currentExecutionTrace = node.getParent();

		return retValue;
	}

	@Override
	public Object visit(ImpliesFormula n) {
		throw new RuntimeException("visit not implemented. ImpliesFormula");
	}

	@Override
	public Object visit(NotFormula n) {
		DynalloyProgram dynalloy = getMapping().getDynAlloy(n);

		DynalloyExecutionTrace node = addExecutionTraceNode(dynalloy);

		currentExecutionTrace = node;

		notFormulas.push(n);
		boolean retValue = (Boolean) n.getFormula().accept(this);
		notFormulas.pop();

		if (n.getFormula().getClass().equals(PredicateFormula.class)) {
			if (!currentExecutionTrace.getChildren().isEmpty()) {
				currentExecutionTrace.getChildren().remove(0);
			}
		}

		currentExecutionTrace.setValue(!retValue);
		currentExecutionTrace.setVisible(true);
		currentExecutionTrace = node.getParent();

		return !retValue;
	}

	@Override
	public Object visit(PredicateFormula n) {
		DynalloyProgram dynalloy = getMapping().getDynAlloy(n);

		if (dynalloy instanceof InvokeProgram) {
			DynalloyExecutionTrace node = addExecutionTraceNode(dynalloy);

			node.setVisible(true);

			InvokeProgram invoke = (InvokeProgram) dynalloy;

			node.getVariables().addAll(invoke.getActualParameters());

			ProgramDeclaration programDecl = getContext().getProgram(
					invoke.getAliasModuleId(), invoke.getProgramId());

			NameSpace programNameSpace = new NameSpace(programDecl);

			List<ExprVariable> programParameters = getContext()
					.getProgramParameters(invoke.getAliasModuleId(),
							invoke.getProgramId());

			for (VariableId var : programDecl.getLocalVariables()) {
				node.addVariable(ExprVariable
						.buildExprVariable(var.getString()));
			}
			for (VariableId var : programDecl.getParameters()) {
				node.addVariable(ExprVariable
						.buildExprVariable(var.getString()));
			}

			// Add parameters mapping
			for (int i = 0, size = n.getParameters().size(); i < size; i++) {

				AlloyExpression actual_parameter = n.getParameters().get(i);
				ExprVariable formal_parameter = programParameters.get(i);

				node.addParameterMap(actual_parameter, formal_parameter);
			}

			// Add the initial values of the variables
			for (AlloyExpression var : node.getVariables()) {
				if (var instanceof ExprVariable) {
					ExprVariable v = (ExprVariable) var;
					ExprVariable alloyVar = null;
					int minIndex = Integer.MAX_VALUE;
					for (int i = 0; i < programParameters.size(); i++) {

						String formal_parameter_str = programParameters.get(i)
								.getVariable().getVariableId().getString();
						int formal_parameter_index = programParameters.get(i)
								.getVariable().getIndex();

						String expr_var_str = v.getVariable().getVariableId()
								.getString();

						if (formal_parameter_str.equals(expr_var_str)) {
							if (formal_parameter_index < minIndex) {

								minIndex = formal_parameter_index;
								alloyVar = programParameters.get(i);
							}
						}
					}
					if (alloyVar != null) {
						Object value = evaluator.evaluate(alloyVar, node);

						node.addVariableValue(var, value);

					}
				}
			}

			Map<String, TreeMap<Integer, String>> instantsOf;
			instantsOf = createInstantsOf(n);
			for (String name : instantsOf.keySet()) {
				TreeMap<Integer, String> map = instantsOf.get(name);
				for (Integer instant : map.keySet()) {
					node.addUseOf(name, instant);
					String alloyStr = map.get(instant);
					node.getNameSpace().addIncarnation(name, instant, alloyStr);
				}
			}

			AlloyFormula innerFormula = getMapping().getAlloy(
					programDecl.getBody());

			// Change the current scope, since we are calling a method
			currentExecutionTrace = node;

			nameSpaces.push(programNameSpace);
			Object retValue = innerFormula.accept(this);
			nameSpaces.pop();

			node.setValue((Boolean) retValue);
			// After the recursive call of the visitor, we switch back the scope
			currentExecutionTrace = node.getParent();

			return retValue;
		} else {

			for (AlloyExpression param : n.getParameters()) {
				Object paramResult = evaluator.evaluate(param,
						currentExecutionTrace);
			}

			Map<String, TreeMap<Integer, String>> instantsOf;
			instantsOf = createInstantsOf(n);

			Object evalResult = evaluator.evaluate(n, currentExecutionTrace);


			if ((evalResult == Boolean.TRUE && notFormulas.size() % 2 == 0)
					|| (evalResult == Boolean.FALSE && notFormulas.size() % 2 != 0)) {

				DynalloyExecutionTrace leaf;
				if (evalResult == Boolean.FALSE && notFormulas.size() % 2 != 0) {
					if (notFormulas.peek().getFormula().equals(n)) {
						leaf = addExecutionTraceLeaf(notFormulas.peek());
					} else
						leaf = null;
				} else
					leaf = addExecutionTraceLeaf(n);

				if (leaf != null) {
					for (String name : instantsOf.keySet()) {
						TreeMap<Integer, String> map = instantsOf.get(name);
						for (Integer instant : map.keySet()) {
							leaf.addUseOf(name, instant);
							String alloyStr = map.get(instant);
							leaf.getNameSpace().addIncarnation(name, instant,
									alloyStr);
						}
					}
				}
			}
			return evalResult;
		}
	}

	Map<String, TreeMap<Integer, String>> createInstantsOf(PredicateFormula n) {
		return createInstantsOf(n.getParameters());
	}

	Map<String, TreeMap<Integer, String>> createInstantsOf(EqualsFormula n) {
		List<AlloyExpression> expressions = new LinkedList<AlloyExpression>();
		expressions.add(n.getLeft());
		expressions.add(n.getRight());
		return createInstantsOf(expressions);
	}

	private Map<String, TreeMap<Integer, String>> createInstantsOf(
			List<AlloyExpression> actual_parameters) {
		
		Map<String, TreeMap<Integer, String>> instantsOf;
		instantsOf = new HashMap<String, TreeMap<Integer, String>>();
		
		for (AlloyExpression actual_parameter : actual_parameters) {

			VarCollector varCollector = new VarCollector();

			varCollector.setFormulaVisitor(this);
			this.setExpressionVisitor(varCollector);

			actual_parameter.accept(varCollector);

			NameSpace currentNameSpace = nameSpaces.peek();

			for (AlloyVariable v : varCollector.getVariables()) {
				String varName = v.getVariableId().getString();
				if (currentNameSpace.containsName(varName)) {

					if (!instantsOf.containsKey(varName)) {
						instantsOf.put(varName, new TreeMap<Integer, String>());
					}
					TreeMap<Integer, String> map = instantsOf.get(varName);

					ExprVariable exprVariable = ExprVariable
							.buildExprVariable(v);

					PrefixedExpressionPrinter prefixedPrinter = new PrefixedExpressionPrinter(
							currentExecutionTrace);
					String variablePrefix = this.evaluator.getVariablePrefix();
					prefixedPrinter.setVariablePrefix(variablePrefix);

					String incarnationStr = (String) exprVariable
							.accept(prefixedPrinter);

					map.put(v.getIndex(), incarnationStr);
				}
			}

		}
		return instantsOf;
	}

	public void setEvaluator(AlloyEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public AlloyEvaluator getEvaluator() {
		return evaluator;
	}

	public DynalloyExecutionTrace getRootExecutionTrace() {
		return rootExecutionTrace;
	}

	private DynalloyExecutionTrace addExecutionTraceLeaf(AlloyFormula formula) {
		DynalloyProgram dynAlloy = getMapping().getDynAlloy(formula);
		if (dynAlloy != null && dynAlloy.getPosition() != null) {
			DynalloyExecutionTrace leaf = new DynalloyExecutionTraceLeaf(
					dynAlloy);
			leaf.setVisible(true);
			leaf.setValue(true);
			currentExecutionTrace.addChild(leaf);

			if (formula instanceof EqualsFormula
					&& dynAlloy instanceof Assigment) {
				EqualsFormula eq = (EqualsFormula) formula;
				Assigment assignment = (Assigment) dynAlloy;
				Object value = evaluator.evaluate(eq.getRight(), leaf);
				leaf.addVariableValue(assignment.getLeft(), value);
			}

			leaf.setNameSpace(nameSpaces.peek());
			return leaf;
		}
		return null;
	}

	private DynalloyExecutionTrace addExecutionTraceNode(
			DynalloyProgram dynAlloy) {
		// if (dynAlloy != null) {
		DynalloyExecutionTrace innerNode = new DynalloyExecutionTraceNode(
				dynAlloy);
		currentExecutionTrace.addChild(innerNode);

		innerNode.setNameSpace(nameSpaces.peek());

		return innerNode;
		// }
		// return null;
	}

	public void setContext(SpecContext context) {
		this.context = context;
	}

	public SpecContext getContext() {
		return context;
	}

	public DynAlloyAlloyMapping getMapping() {
		return context.getMapping();
	}

	private final Stack<NameSpace> nameSpaces = new Stack<NameSpace>();

	public void setRootNameSpace(NameSpace rootNameSpace) {
		nameSpaces.clear();
		nameSpaces.push(rootNameSpace);
		rootExecutionTrace.setNameSpace(rootNameSpace);
	}

	public Object visitAlloyAssertion(AlloyFormula alloyPrecondition,
			AlloyFormula programFormula, AlloyFormula alloyPostcondition) {

		alloyPrecondition.accept(this);

		programFormula.accept(this);

		this.visitPostcondition((PredicateFormula) alloyPostcondition);

		return null;
	}

	public void visitPostcondition(PredicateFormula n) {
		DynalloyProgram dynalloy = getMapping().getDynAlloy(n);

		if (!(dynalloy instanceof TestPredicate))
			throw new IllegalStateException(
					"PredicateFormula is not a postcondition" + n.toString());

		for (AlloyExpression param : n.getParameters()) {
			Object paramResult = evaluator.evaluate(param,
					currentExecutionTrace);
		}

		Map<String, TreeMap<Integer, String>> instantsOf;
		instantsOf = createInstantsOf(n);

		Object evalResult = evaluator.evaluate(n, currentExecutionTrace);

		DynalloyExecutionTrace leaf = addExecutionTraceLeaf(n);

		for (String name : instantsOf.keySet()) {
			TreeMap<Integer, String> map = instantsOf.get(name);
			for (Integer instant : map.keySet()) {
				leaf.addUseOf(name, instant);
				String alloyStr = map.get(instant);
				leaf.getNameSpace().addIncarnation(name, instant, alloyStr);
			}
		}

	}

}
