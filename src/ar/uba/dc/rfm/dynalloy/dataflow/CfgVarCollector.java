package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.VarCollector;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class CfgVarCollector {
	
	public class GlobalVariable {
		private final ExprVariable var;
		private final String type;
		private final boolean isParameter;
		
		public GlobalVariable(ExprVariable var, String type, boolean isParameter) {
			this.var = var;
			this.type = type;
			this.isParameter = isParameter;
		}
		
		public ExprVariable getVar() {
			return var;
		}
		
		public String getType() {
			return type;
		}
		
		public boolean isParameter() {
			return isParameter;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((var == null) ? 0 : var.hashCode());
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
			GlobalVariable other = (GlobalVariable) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		private CfgVarCollector getOuterType() {
			return CfgVarCollector.this;
		}
	}
	
	private ControlFlowGraph graph;
	private DynAlloyAlloyMapping mapping;
	private Set<CfgNode> visitedNodes;
	
	public CfgVarCollector(ControlFlowGraph cfg, DynAlloyAlloyMapping mapping) {
		this.graph = cfg;
		this.mapping = mapping;
		visitedNodes = new HashSet<CfgNode>();
	}
	
	public Set<GlobalVariable> getCfgVariables() {
		Set<GlobalVariable> allVars = getVariablesFromNodesAndChilds(graph.getHeads());
		//Some incarnation zero locals might not be present, so we add them since we want to bound them too.
		//allVars.addAll(getMainProgramLocalsIncarnationZero());
		visitedNodes.clear();
		return allVars;
	}
	
	public Collection<GlobalVariable> getMainProgramLocalsIncarnationZero() {
		Set<GlobalVariable> globalVars = new LinkedHashSet<GlobalVariable>();
		
		CfgNode head = graph.getHeads().iterator().next();
		for (VariableId varId : head.getParentProgramDeclaration().getLocalVariables()) {
			ExprVariable var = new ExprVariable(new AlloyVariable(varId, 0));
			String type = head.getParentProgramDeclaration().getParameterTyping().get(new AlloyVariable(varId));
			globalVars.add(new GlobalVariable(var, type, false));
		}
		
		return globalVars;
	}
	
	private Set<GlobalVariable> getVariablesFromNodesAndChilds(Collection<CfgNode> nodes) {
		Set<GlobalVariable> allVars = new LinkedHashSet<GlobalVariable>();
		for (CfgNode node : nodes) {
			if (!visitedNodes.contains(node)) {
				visitedNodes.add(node);
				for (GlobalVariable globalVariable : getVariables(node)) {
					addGlobalOverridingStateIfItIsLocal(globalVariable, allVars);
				}
				for (GlobalVariable globalVariable : getVariablesFromNodesAndChilds(node.getSuccessors())) {
					addGlobalOverridingStateIfItIsLocal(globalVariable, allVars);
				}
			}
		}
		return allVars;
	}
	
	private void addGlobalOverridingStateIfItIsLocal(GlobalVariable globalVariable, Set<GlobalVariable> allVars) {
		//Depending on the context, a global variable can be a local in one method,
		//and after that be a parameter in another. But if it is a local somewhere,
		//we know it's definitely a local, so we override it if there was previous
		//information saying something else.
		if (allVars.contains(globalVariable)) {
			if (!globalVariable.isParameter()) {
				allVars.remove(globalVariable);
				allVars.add(globalVariable);
			}
		}
		else
			allVars.add(globalVariable);
	}
	
	private Set<GlobalVariable> getVariables(CfgNode n) {
		Set<GlobalVariable> allVars = new LinkedHashSet<GlobalVariable>();
		DynalloyProgram p = n.getProgram();
		if (p instanceof Assigment) {
			Assigment a = (Assigment)p;
			ExprVariable assignedVar = a.getLeft();
			AlloyExpression expression = a.getRight();
			
			//We use the mapping, since we are interested in the Alloy variables, not the dynalloy counterparts
			if (mapping != null) {
				EqualsFormula equals = (EqualsFormula)mapping.getAlloy(a);
				assignedVar = (ExprVariable)equals.getLeft();
				expression = equals.getRight();
			}
			
			addVariableIfItIsAGlobal(assignedVar, allVars, n);
			
			VarCollector collector = new VarCollector();
			collector.setFormulaVisitor(new FormulaVisitor(collector));
			expression.accept(collector);
			for (AlloyVariable alloyVar : collector.getVariables()) {
				ExprVariable var = new ExprVariable(alloyVar);
				
				addVariableIfItIsAGlobal(var, allVars, n);
			}
			
			return allVars;
		}
		else if (p instanceof TestPredicate || p instanceof Skip) {
			return allVars;
		}
		else if (p instanceof InvokeAction) {
			InvokeAction action = (InvokeAction)p;
			if (action.getActionId().equals("getUnusedObject") || action.getActionId().equals("havocVariable2")) {
				//This is the target var of the instantiation or havoc
				ExprVariable var = (ExprVariable)action.getActualParameters().iterator().next();
				//We use the mapping, since we are interested in the Alloy variables, not the dynalloy counterparts
				if (mapping != null) {
					PredicateFormula predicate = (PredicateFormula)mapping.getAlloy(action);
					var = (ExprVariable)predicate.getParameters().iterator().next();
				}
				addVariableIfItIsAGlobal(var, allVars, n);
				return allVars;
			}
			else
				throw new UnsupportedOperationException("InvokeAction not supported.");
		}
		else
			throw new UnsupportedOperationException("Operation not supported.");
	}	
	
	private void addVariableIfItIsAGlobal(ExprVariable var, Set<GlobalVariable> allVars, CfgNode n) {
		//Typing info is stored for variables without their incarnation
		AlloyVariable varWithoutEncarnation = new AlloyVariable(var.getVariable().getVariableId());
		String type = n.getParentProgramDeclaration().getParameterTyping().get(varWithoutEncarnation);
		boolean isParameter = n.getParentProgramDeclaration().getParameters().contains(var.getVariable().getVariableId());
		
		if (n.getLocalToGlobalVarMapping().containsKey(var)) {
			AlloyExpression expr = n.getLocalToGlobalVarMapping().get(var);
			//If it's a general expression, we don't care for it
			if (expr instanceof ExprVariable)
				allVars.add(new GlobalVariable((ExprVariable)expr, type, isParameter));
		}
		//This means it's already a global var (e.g., a parameter to the entry program).
		else 
			allVars.add(new GlobalVariable(var, type, isParameter));
	}
}
