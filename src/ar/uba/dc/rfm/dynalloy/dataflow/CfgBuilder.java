package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class CfgBuilder extends DfsProgramVisitor {
	
	public class CfgBuilderNode {
		private List<CfgNode> entryPoints;
		private List<CfgNode> exitPoints;
		
		public CfgBuilderNode() {
			this.entryPoints = new LinkedList<CfgNode>();
			this.exitPoints = new LinkedList<CfgNode>();
		}
		
		public CfgBuilderNode(DynalloyProgram p, ProgramDeclaration progDeclaration, HashMap<ExprVariable, AlloyExpression> localToGlobalVarMapping) {
			this();
			CfgNode node = new CfgNode(p, progDeclaration, localToGlobalVarMapping);
			this.entryPoints.add(node);
			this.exitPoints.add(node);
		}
		
		public CfgBuilderNode(List<CfgNode> entryPoints, List<CfgNode> exitPoints) {
			this.entryPoints = entryPoints;
			this.exitPoints = exitPoints;
		}
		
		public List<CfgNode> getEntryPoints() {
			return entryPoints;
		}
		
		public List<CfgNode> getExitPoints() {
			return exitPoints;
		}
	}
	
	private SpecContext specContext;
	private HashMap<ExprVariable, AlloyExpression> currentLevelParamToGlobalVarMapping;
	private ProgramDeclaration currentLevelProgramDeclaration;
	
	public CfgBuilder(SpecContext ctx, ProgramDeclaration progDeclaration) {
		this(ctx, progDeclaration, new LinkedHashMap<ExprVariable, AlloyExpression>());
	}
	
	private CfgBuilder(SpecContext ctx, ProgramDeclaration progDeclaration, HashMap<ExprVariable, AlloyExpression> mapping) {
		this.specContext = ctx;
		this.currentLevelProgramDeclaration = progDeclaration;
		this.currentLevelParamToGlobalVarMapping = mapping;
	}
	
	public HashMap<ExprVariable, AlloyExpression> getLocalToGlobalVarMapping() {
		return currentLevelParamToGlobalVarMapping;
	}

	@Override
	public Object visit(Choice c) {
		List<CfgBuilderNode> nodes = (List<CfgBuilderNode>)super.visit(c);
		
		// The result of a Choice is the union of all the entry points, and as an exit point, we add a Skip
		//that is needed in bounds calculation, to make deterministic merges.
		//(while calculating the max updated incarnation)
		CfgBuilderNode result = new CfgBuilderNode();
		CfgNode exitSkip = new CfgNode(new Skip(), currentLevelProgramDeclaration, currentLevelParamToGlobalVarMapping);
		result.exitPoints.add(exitSkip);
		for (CfgBuilderNode cfgBuilderNode : nodes) {
			result.entryPoints.addAll(cfgBuilderNode.entryPoints);
			connectSuccessorsAndPredecessors(cfgBuilderNode.exitPoints, Arrays.asList(exitSkip));
		}

		return result;
	}

	@Override
	public Object visit(Closure c) {
		throw new UnsupportedOperationException("Closure not supported");
	}

	@Override
	public Object visit(Skip s) {
		return new CfgBuilderNode(s, currentLevelProgramDeclaration, currentLevelParamToGlobalVarMapping);
	}

	@Override
	public Object visit(Composition c) {
		List<CfgBuilderNode> childs = (List<CfgBuilderNode>)super.visit(c);
		
		//In a Composition, we have to connect the exit points of a node with the entry points of
		//its successors, and the result has the entry points of the first node, and the exit
		//of the last one
		Iterator<CfgBuilderNode> iterator = childs.iterator();
		CfgBuilderNode current;
		CfgBuilderNode next;
		if (iterator.hasNext()) {
			current = iterator.next();
			while(iterator.hasNext()) {
				next = iterator.next();
				connectSuccessorsAndPredecessors(current.exitPoints, next.entryPoints);
				current = next;
			}
		}
		
		CfgBuilderNode result = new CfgBuilderNode(childs.get(0).entryPoints, childs.get(childs.size() - 1).exitPoints);
		return result;
	}
	
	@Override
	public Object visit(TestPredicate t) {
		return new CfgBuilderNode(t, currentLevelProgramDeclaration, currentLevelParamToGlobalVarMapping);
	}

	@Override
	public Object visit(InvokeAction u) {
		return new CfgBuilderNode(u, currentLevelProgramDeclaration, currentLevelParamToGlobalVarMapping);
	}

	@Override
	/**
	 * We use cloning to solve the inter-procedural calls and build the CFG.
	 */
	public Object visit(InvokeProgram n) {
		if (specContext == null)
			throw new IllegalStateException("The program to analyse invokes other programs, so a SpecContext must be specified");
		
		//We build the CFG of the InvokeProgram recursively, calculating the new mapping
		//(Only if there is a mapping, otherwise we work entirely on the dynalloy side, leaving the mapping empty)
		HashMap<ExprVariable, AlloyExpression> newMapping = new LinkedHashMap<ExprVariable, AlloyExpression>();
		if (specContext.getMapping() != null) {
			PredicateFormula predicateCall = (PredicateFormula)specContext.getMapping().getAlloy(n);
			List<AlloyExpression> actualParams = predicateCall.getParameters();
			List<ExprVariable> formalParams = specContext.getProgramParameters(n.getAliasModuleId(), n.getProgramId());
			newMapping = getNextLevelParamToGlobalVarMapping(formalParams, actualParams);
		}
		
		ProgramDeclaration calledProgram = specContext.getProgram(n.getAliasModuleId(), n.getProgramId(), n.getActualParameters().size());
		Object cfgBuilderNode = calledProgram.getBody().accept(new CfgBuilder(specContext, calledProgram, newMapping));
		
		return cfgBuilderNode;
	}

	private HashMap<ExprVariable, AlloyExpression> getNextLevelParamToGlobalVarMapping(List<ExprVariable> formalParams, List<AlloyExpression> actualParams) {
		if (formalParams.size() != actualParams.size())
			throw new IllegalStateException("Predicate formal params count differs from actual params count");
		
		HashMap<ExprVariable, AlloyExpression> newMapping = new LinkedHashMap<ExprVariable, AlloyExpression>();
		for (int i = 0; i < formalParams.size(); i++ ) {
			ExprVariable nameInProgram = formalParams.get(i);
			AlloyExpression actualExpression = actualParams.get(i);
			
			//The actual expression might be (or have inside it) a local from the previous level, so
			//we get the global one
			actualExpression = replaceVarForGlobal(actualExpression);
			
			newMapping.put(nameInProgram, actualExpression);
		}
		
		return newMapping;
	}

	@Override
	public Object visit(Assigment assigment) {
		return new CfgBuilderNode(assigment, currentLevelProgramDeclaration, currentLevelParamToGlobalVarMapping);
	}
	
	private void connectSuccessorsAndPredecessors(List<CfgNode> predecessors, List<CfgNode> successors) {
		for (CfgNode pred : predecessors) {
			for (CfgNode suc : successors) {
				pred.getSuccessors().add(suc);
				suc.getPredecessors().add(pred);
			}
		}
	}
	
	private AlloyExpression replaceVarForGlobal(AlloyExpression exp) {
		if (exp instanceof ExprVariable) {
			if (currentLevelParamToGlobalVarMapping.containsKey(exp))
				return currentLevelParamToGlobalVarMapping.get(exp); 
			else
				return exp;
		}
		else if (exp instanceof ExprIntLiteral || exp instanceof ExprConstant)
			return exp;
		else if (exp instanceof ExprJoin) {
			ExprJoin join = (ExprJoin)exp;
			return new ExprJoin(replaceVarForGlobal(join.getLeft()), replaceVarForGlobal(join.getRight()));
		}
		else if (exp instanceof ExprFunction) {
			ExprFunction func = (ExprFunction)exp;
			List<AlloyExpression> newParams = new LinkedList<AlloyExpression>();
			for (AlloyExpression param : func.getParameters()) {
				newParams.add(replaceVarForGlobal(param));
			}
			return new ExprFunction(null, func.getFunctionId(), newParams);
		}
		else
			throw new IllegalStateException(String.format("Expression not supported as parameter: %s", exp));
	}

}
