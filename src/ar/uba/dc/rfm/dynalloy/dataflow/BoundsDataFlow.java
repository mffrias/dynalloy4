package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.dataflow.CfgVarCollector.GlobalVariable;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class BoundsDataFlow extends ForwardFlowAnalysis<BoundedVariable> {

	private FlowSet<BoundedVariable> initialFlow;
	private DynAlloyAlloyMapping mapping;
	private Set<BoundedVariable> initialBounds;
	private BoundsFileParser parser;
	
	public BoundsDataFlow(ControlFlowGraph cfg) {
		this(cfg, new LinkedHashSet<BoundedVariable>());
	}
	
	public BoundsDataFlow(ControlFlowGraph cfg, Collection<BoundedVariable> initialBounds) {
		super(cfg);
		addBoundsToInitialFlow(initialBounds);
	}
	
	public BoundsDataFlow(ControlFlowGraph cfg, String boundsFilePath, String boundsSpecFilePath, ProgramDeclaration program, DynAlloyAlloyMapping mapping) throws IOException {
		super(cfg);
		this.mapping = mapping;
		parser = new BoundsFileParser(boundsFilePath, boundsSpecFilePath, program.getParameterTyping());
		
		initialBounds = parser.getBoundsFromFile();
		Set<GlobalVariable> allVars = new CfgVarCollector(graph, mapping).getCfgVariables();
		
		addBoundsToInitialFlow(initialBounds);
		propagateBoundsToOtherIncarnations(allVars);
		completeVariablesWithDefaultBounds(allVars, parser, program);
	}

	private void addBoundsToInitialFlow(Collection<BoundedVariable> initialBounds) {
		initialFlow = new BoundsFlowSet();
		if (initialBounds != null)
			for (BoundedVariable bv : initialBounds)
				initialFlow.add(bv);
	}
	
	/**
	 * Since the initial bounds calculated by the tool created by JPG gives us bounds
	 * only for the initial incarnations of alloy variables, we have to propagate
	 * those bounds to every other incarnation of those variables, because the analysis
	 * rests on the assumption that every variable is bounded initially, and those are
	 * the tightest bounds we can use.  
	 */
	private void propagateBoundsToOtherIncarnations(Set<GlobalVariable> allVars) {
		HashMap<VariableId, Set<Bound>> varToBounds = new HashMap<VariableId, Set<Bound>>();
		for (BoundedVariable bv : initialBounds) {
			varToBounds.put(bv.getVariable().getVariable().getVariableId(), bv.getBounds());
		}
		
		for (GlobalVariable globalVar : allVars) {
			if (!isVariableBounded(globalVar.getVar(), initialFlow)) {
				VariableId varId = globalVar.getVar().getVariable().getVariableId();
				//This means this variable is another incarnation of a variable for which we have
				//initial bounds information, so we add that to the initial flow
				if (varToBounds.containsKey(varId)) {
					BoundedVariable newBV = new BoundedVariable(globalVar.getVar(), varToBounds.get(varId));
					initialFlow.add(newBV);
				}
			}
		}		
	}
	
	/**
	 * This adds bounds to the initialFlow for every variable that hasn't been bounded yet.
	 * For local variables, we can use a tight bound which is the initialization value for that
	 * type. For parameters, we have to use all the possible values that the type accepts.  
	 */
	private void completeVariablesWithDefaultBounds(Set<GlobalVariable> allVars, BoundsFileParser boundsFileParser, ProgramDeclaration program) {
		for (GlobalVariable globalVar : allVars) {
			if (isVariableBounded(globalVar.getVar(), initialFlow))
				continue;
			
			String type = globalVar.getType();
			if (!globalVar.isParameter()) {	
				Bound defaultBoundForType = boundsFileParser.getDefaultBoundForTypeInitialization(type);
				Set<Bound> bounds = new LinkedHashSet<Bound>();
				bounds.add(defaultBoundForType);
				initialFlow.add(new BoundedVariable(globalVar.getVar(), bounds));
			}
			else {
				Set<Bound> defaultBoundsForType = boundsFileParser.getDefaultBoundsForType(type);
				initialFlow.add(new BoundedVariable(globalVar.getVar(), defaultBoundsForType));
			}
		}
	}
//	private void completeVariablesWithDefaultBounds(Set<GlobalVariable> allVars, BoundsFileParser boundsFileParser, ProgramDeclaration program) {
//		for (GlobalVariable globalVar : allVars) {
//			if (isVariableBounded(globalVar.getVar(), initialFlow))
//				continue;
//			
//			String type = globalVar.getType();
//			if (globalVar.isParameter() && globalVar.getVar().getVariable().getIndex() == 0) {	
//				Set<Bound> defaultBoundsForType = boundsFileParser.getDefaultBoundsForType(type);
//				initialFlow.add(new BoundedVariable(globalVar.getVar(), defaultBoundsForType));
//			}
//		}
//	}
	
	public Set<BoundedVariable> getFinalAlloyBounds() {		
		Set<BoundedVariable> finalBounds = new LinkedHashSet<BoundedVariable>();
		for (BoundedVariable bv : getTailsMergedFinalFlow()) {
			finalBounds.add(bv);
		}
		
		//Remove the initial bounds from the final bounds returned, so they are not added twice to
		//the .als file with the alloy bounds calculated by JPG
		for (BoundedVariable bv : initialBounds) {
			finalBounds.remove(bv);
		}
		
		return finalBounds;
	}

	@Override
	protected void copy(FlowSet<BoundedVariable> source, FlowSet<BoundedVariable> dest) {
		source.copy(dest);
	}

	@Override
	protected FlowSet<BoundedVariable> entryInitialFlow() {
		return initialFlow;
	}
	
	@Override
	protected void merge(FlowSet<BoundedVariable> in1, FlowSet<BoundedVariable> in2, FlowSet<BoundedVariable> out) {
		if (in1.equals(in2)) {
			in1.copy(out);
			return;
		}
		
		in1.copy(out);
		
		for (Object o2 : in2) {
			BoundedVariable bvIn2 = (BoundedVariable)o2;
			if (isVariableBounded(bvIn2.getVariable(), in1)) {
				BoundedVariable bvIn1 = getBounds(bvIn2.getVariable(), in1);
				out.remove(bvIn1);
				Set<Bound> mergedBounds = new LinkedHashSet<Bound>(bvIn2.getBounds());
				mergedBounds.addAll(bvIn1.getBounds());				
				BoundedVariable mergedBV = new BoundedVariable(bvIn2.getVariable(), mergedBounds);
				out.add(mergedBV);
			}
			else {
				out.add(bvIn2);
			}
		}
		
		//TODO: Explicar esto...
		if (mapping != null) {
			BoundsFlowSet boundsIn1 = (BoundsFlowSet)in1;
			BoundsFlowSet boundsIn2 = (BoundsFlowSet)in2;
			BoundsFlowSet boundsOut = (BoundsFlowSet)out;
			
			for (String varId : boundsIn2.getHighestIncarnationUpdatedTable().keySet()) {
				int incarnationIn2 = boundsIn2.getHighestIncarnationUpdatedTable().get(varId);
				
				boundsOut.incarnationUpdated(varId, incarnationIn2);
			
				if (boundsIn1.getHighestIncarnationUpdatedTable().containsKey(varId)) {
					int incarnationIn1 = boundsIn1.getHighestIncarnationUpdatedTable().get(varId);
					ExprVariable var1 = new ExprVariable(new AlloyVariable(varId, incarnationIn1));
					ExprVariable var2 = new ExprVariable(new AlloyVariable(varId, incarnationIn2));
					BoundedVariable bvIn1 = getBounds(var1, in1);
					BoundedVariable bvIn2 = getBounds(var2, in2);
					if (incarnationIn1 >= incarnationIn2) {
						BoundedVariable bvInOut = getBounds(var1, out);
						out.remove(bvInOut);
						Set<Bound> mergedBounds = new LinkedHashSet<Bound>(bvIn1.getBounds());
						mergedBounds.addAll(bvIn2.getBounds());
						BoundedVariable mergedBV = new BoundedVariable(var1, mergedBounds);
						out.add(mergedBV);
					}
					else {
						BoundedVariable bvInOut = getBounds(var2, out);
						out.remove(bvInOut);
						Set<Bound> mergedBounds = new LinkedHashSet<Bound>(bvIn1.getBounds());
						mergedBounds.addAll(bvIn2.getBounds());
						BoundedVariable mergedBV = new BoundedVariable(var2, mergedBounds);
						out.add(mergedBV);
					}
				}
			}
		}
	}

	@Override
	protected FlowSet<BoundedVariable> newInitialFlow() {
		return new BoundsFlowSet();
	}
	
	private Map<ExprVariable, AlloyExpression> currentLocalToGlobalVarMapping;
	
	@Override
	protected void flowThrough(FlowSet<BoundedVariable> in, CfgNode p, FlowSet<BoundedVariable> out) {
		currentLocalToGlobalVarMapping = p.getLocalToGlobalVarMapping();
		DynalloyProgram program = p.getProgram();
		if (program instanceof Assigment) {
			//out = in
			in.copy(out);
			
			Assigment assignment = (Assigment)program;
			ExprVariable assignedVar = assignment.getLeft();
			AlloyExpression expression = assignment.getRight();
			
			//If there is a mapping, we have to calculate the bounds for the expression
			//contained in it (i.e., the alloy expression), because we have to handle
			//the incarnations correctly working in the alloy world
			if (mapping != null) {
				EqualsFormula equals = (EqualsFormula)mapping.getAlloy(assignment);
				expression = equals.getRight();
				assignedVar = (ExprVariable)equals.getLeft();
				//Since we have to calculate bounds for the global variables, we translate the local names
				//of variables in methods to their global counterparts
				if (p.getLocalToGlobalVarMapping().containsKey(assignedVar)) {
					//Since we are assigning to it, the expression for this variable must be an lvalue.
					assignedVar = (ExprVariable)p.getLocalToGlobalVarMapping().get(assignedVar);
				}
				
				//TODO:Explicar esto
				((BoundsFlowSet)out).incarnationUpdated(assignedVar.getVariable().getVariableId().getString(), assignedVar.getVariable().getIndex());
			}
			
			//out = in - sigma[x]
			if (isVariableBounded(assignedVar, in)) {
				BoundedVariable bv = getBounds(assignedVar, in);
				out.remove(bv);
			}
			
			//out = in - sigma[x] U eval(exp)
			Set<Bound> bounds = evaluate(expression, in, false);
			BoundedVariable vb = new BoundedVariable(assignedVar, bounds);
			out.add(vb);
		}
		else if (program instanceof TestPredicate) {
			in.copy(out);
		}
		else if (program instanceof Skip) {
			//Agregar que chequee que si la encarnacion que esta en la condicion esta acotada y su conjunto de bounds
			//solo tiene al null, ahi si no devuelva nada...
			
//			if (p.getPredecessors().size() == 1) {
//				DynalloyProgram predProgram = p.getPredecessors().iterator().next().getProgram();
//				if (predProgram instanceof TestPredicate) {
//					TestPredicate predTest = (TestPredicate)predProgram;
//					if (predTest.getPredicateFormula().toString().contains("Condition0")) {
//						//If the parent of this Skip is a check 'throw == null', then we return no bounds
//						//to avoid ruining the tighter bounds that might be calculated in the other branch
//						if (mapping != null) {
//							PredicateFormula predicate = (PredicateFormula)mapping.getAlloy(predProgram);
//							if (predicate.getParameters().size() == 1) {
//								ExprVariable throwVar = (ExprVariable)predicate.getParameters().iterator().next();
//								if (p.getLocalToGlobalVarMapping().containsKey(throwVar)) {
//									throwVar = (ExprVariable)p.getLocalToGlobalVarMapping().get(throwVar);								
//								}
//								if (isVariableBounded(throwVar, in)) {
//									BoundedVariable vb = getBounds(throwVar, in);
//									if (vb.getBounds().size() == 1 && vb.getBounds().contains("null"))
//										return;
//								}
//								//This means is probably throw_0, which is not bounded
//								else
//									return;
//							}
//						}
//					}
//				}
//			}
			in.copy(out);
		}
		else if (program instanceof InvokeAction) {
			InvokeAction ia = (InvokeAction)program;
			if (ia.getActionId().equals("getUnusedObject") || ia.getActionId().equals("havocVariable2")) {
				//out = in
				in.copy(out);
				
				//Both getUnusedObject and havocVariable2 have the same bounds semantics:
				//the variable (which is the first parameter), now has all the possible bounds of its type.
				InvokeAction action = (InvokeAction)program;
				ExprVariable var = (ExprVariable)action.getActualParameters().iterator().next();
				
				//Before modifying the variable, we have to get it's type.
				String type = p.getParentProgramDeclaration().getParameterTyping().get(var.getVariable());
				
				//We use the mapping, since we are interested in the Alloy variable, not the dynalloy counterpart
				if (mapping != null) {
					PredicateFormula predicate = (PredicateFormula)mapping.getAlloy(action);
					var = (ExprVariable)predicate.getParameters().iterator().next();
					//Since we have to calculate bounds for the global variables, we translate the local names
					//of variables in methods to their global counterparts
					if (p.getLocalToGlobalVarMapping().containsKey(var)) {
						//Since we are assigning to it, the expression for this variable must be an lvalue.
						var = (ExprVariable)p.getLocalToGlobalVarMapping().get(var);
					}
					
					//TODO:Explicar esto
					((BoundsFlowSet)out).incarnationUpdated(var.getVariable().getVariableId().getString(), var.getVariable().getIndex());
				}
				
				//out = in - sigma[x]
				if (isVariableBounded(var, in)) {
					BoundedVariable bv = getBounds(var, in);
					out.remove(bv);
				}
				
				//out = in - sigma[x] U DefaultBoundsForVar	
				Set<Bound> bounds = parser.getDefaultBoundsForType(type);
				BoundedVariable vb = new BoundedVariable(var, bounds);
				out.add(vb);
			}
			else {
				throw new UnsupportedOperationException("InvokeAction not supported.");
			}
		}
		else if (program instanceof InvokeProgram) {
			throw new UnsupportedOperationException("Since cloning is used, there shouldn't be any InvokeProgram");
		}
		else {
			throw new UnsupportedOperationException(String.format("Invalid program: %s", program.toString()));
		}
	}
	
	private Set<Bound> evaluate(AlloyExpression exp, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		//ExprVariable = V
		if (exp instanceof ExprVariable) {
			ExprVariable var = (ExprVariable)exp;
			return evaluateVariable(var, in, expIsAlreadyGlobal);
		}
		//ExprJoin = E.f
		else if (exp instanceof ExprJoin) {
			ExprJoin join = (ExprJoin)exp;
			return evaluateJoin(join, in, expIsAlreadyGlobal);
		}
		//ExprProduct = E1 -> E2
		else if (exp instanceof ExprProduct) {
			ExprProduct product = (ExprProduct)exp;
			return evaluateProduct(product, in, expIsAlreadyGlobal);
		}
		//ExprOverride = E1 ++ E2
		else if (exp instanceof ExprOverride) {
			ExprOverride override = (ExprOverride)exp;
			return evaluateOverride(override, in, expIsAlreadyGlobal);
		}
		//ExprConstant = C
		else if (exp instanceof ExprConstant) {
			ExprConstant constant = (ExprConstant)exp;
			return evaluateConstant(constant);
		}
		//ExprConstant = N
		else if (exp instanceof ExprIntLiteral) {
			ExprIntLiteral intLiteral = (ExprIntLiteral)exp;
			return evaluateIntLiteral(intLiteral);
		}
		//ExprFunction = add[x, y] | sub[x, y] | negate[x]
		else if (exp instanceof ExprFunction) {
			ExprFunction function = (ExprFunction)exp;
			return evaluateFunction(function, in, expIsAlreadyGlobal);
		}
		//ExprIfCondition = Condition => Left else Right
		else if (exp instanceof ExprIfCondition) {
			ExprIfCondition ifCondition = (ExprIfCondition)exp;
			return evaluateIfCondition(ifCondition, in, expIsAlreadyGlobal);
		}
		else {
			throw new UnsupportedOperationException(String.format("AlloyExpression not supported: %s", exp.toString()));
		}
	}

	private Set<Bound> evaluateVariable(ExprVariable var, FlowSet<BoundedVariable> in, boolean varIsAlreadyGlobal) {
		//Again, since we have the bounds calculated only for the global variables, if this is a local
		//one, we need the bounds for the global expression that is the "actual parameter"
		if (!varIsAlreadyGlobal && currentLocalToGlobalVarMapping.containsKey(var)) {
			AlloyExpression globalExp = currentLocalToGlobalVarMapping.get(var);
			if ((globalExp instanceof ExprVariable))
				var = (ExprVariable)globalExp;
			else
				return evaluate(currentLocalToGlobalVarMapping.get(var), in, true);
		}
		
		if (isVariableBounded(var, in)) {
			Set<Bound> result = new LinkedHashSet<Bound>();
			BoundedVariable varBounds = getBounds(var, in);
			result.addAll(varBounds.getBounds());
			return result;
		}
		else {
			throw new IllegalStateException(String.format("Variable '%s' not bounded", var.toString()));
		}
	}
	
	private Set<Bound> evaluateJoin(ExprJoin join, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		Set<Bound> leftBounds = evaluate(join.getLeft(), in, expIsAlreadyGlobal);
		Set<Bound> rightBounds = evaluate(join.getRight(), in, expIsAlreadyGlobal);
		
		return Bound.compose(leftBounds, rightBounds);
	}
	
	private Set<Bound> evaluateProduct(ExprProduct product, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		Set<Bound> leftBounds = evaluate(product.getLeft(), in, expIsAlreadyGlobal);
		Set<Bound> rightBounds = evaluate(product.getRight(), in, expIsAlreadyGlobal);
		
		return Bound.cartesianProduct(leftBounds, rightBounds);
	}
	
	private Set<Bound> evaluateOverride(ExprOverride override, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) { 
		Set<Bound> leftBounds = evaluate(override.getLeft(), in, expIsAlreadyGlobal);
		Set<Bound> rightBounds = evaluate(override.getRight(), in, expIsAlreadyGlobal);
		
		return Bound.override(leftBounds, rightBounds);
	}
	
	private Set<Bound> evaluateConstant(ExprConstant constant) {
		Set<Bound> bound = new LinkedHashSet<Bound>();
		bound.add(Bound.buildBound(constant.getConstantId()));
		return bound;
	}
	
	private Set<Bound> evaluateIntLiteral(ExprIntLiteral intLiteral) {
		Set<Bound> bound = new LinkedHashSet<Bound>();
		bound.add(Bound.buildBound(intLiteral.getIntLiteral()));
		return bound;
	}
	
	private Set<Bound> evaluateFunction(ExprFunction function, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		String funcId = function.getFunctionId(); 
		if (funcId.equals("add") || funcId.equals("sub"))
			return evaluateBinaryFunction(function, in, expIsAlreadyGlobal);
		else if (funcId.equals("negate")) {
			return evaluateNegation(function, in, expIsAlreadyGlobal);
		}
		else
			throw new UnsupportedOperationException(String.format("Function '%s' not supported", funcId));
	}
	
	private Set<Bound> evaluateBinaryFunction(ExprFunction function, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		String funcId = function.getFunctionId(); 
		List<AlloyExpression> params = function.getParameters();
		if (params.size() != 2)
			throw new IllegalStateException(String.format("'%s' function must have 2 parameters", funcId));
		
		Set<Bound> firstParamBounds = evaluate(params.get(0), in, expIsAlreadyGlobal);
		Set<Bound> secondParamBounds = evaluate(params.get(1), in, expIsAlreadyGlobal);
		if (funcId.equals("add"))
			return Bound.add(firstParamBounds, secondParamBounds);
		else if (funcId.equals("sub"))
			return Bound.sub(firstParamBounds, secondParamBounds);
		else
			throw new UnsupportedOperationException(String.format("Function '%s' not supported", funcId));
	}
	
	private Set<Bound> evaluateNegation(ExprFunction function, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		List<AlloyExpression> params = function.getParameters();
		if (params.size() != 1)
			throw new IllegalStateException(String.format("'%s' function must have 1 parameter", function.getFunctionId()));
		
		Set<Bound> parameters = evaluate(params.get(0), in, expIsAlreadyGlobal);
		
		return Bound.negate(parameters);
	}
	
	private Set<Bound> evaluateIfCondition(ExprIfCondition ifCondition, FlowSet<BoundedVariable> in, boolean expIsAlreadyGlobal) {
		Set<Bound> bounds = new LinkedHashSet<Bound>();
		bounds.addAll(evaluate(ifCondition.getLeft(), in, expIsAlreadyGlobal));
		bounds.addAll(evaluate(ifCondition.getRight(), in, expIsAlreadyGlobal));
		return bounds;
	}
	
	private boolean isVariableBounded(ExprVariable var, FlowSet<BoundedVariable> fs) {
		//BoundsFlowSet implements this operation to answer in O(1)
		return ((BoundsFlowSet)fs).isVariableBounded(var); 
	}
	
	private BoundedVariable getBounds(ExprVariable var, FlowSet<BoundedVariable> fs) {
		//BoundsFlowSet implements this operation to answer in O(1)
		return ((BoundsFlowSet)fs).getBounds(var); 
	}
}
