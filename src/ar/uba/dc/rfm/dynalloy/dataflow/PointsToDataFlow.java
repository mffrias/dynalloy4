package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.dataflow.CfgVarCollector.GlobalVariable;
import ar.uba.dc.rfm.dynalloy.dataflow.PointsToGraph.Edge;
import ar.uba.dc.rfm.dynalloy.dataflow.PointsToGraph.Node;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class PointsToDataFlow extends ForwardFlowAnalysis<PointsToGraph> {

	private DynAlloyAlloyMapping mapping;
	private PointsToFlowSet initialFlow;
	
	public PointsToDataFlow(ControlFlowGraph cfg) {
		this(cfg, null);
	}
	
	public PointsToDataFlow(ControlFlowGraph cfg, DynAlloyAlloyMapping mapping) {
		super(cfg);
		this.mapping = mapping;
		
		initialFlow = new PointsToFlowSet();
		PointsToGraph ptg = (PointsToGraph)initialFlow.iterator().next();

		//Initialize all the params (that aren't fields)
		Set<GlobalVariable> allVars = new CfgVarCollector(graph, mapping).getCfgVariables();
	
		for (GlobalVariable globalVar : allVars) {
			if (globalVar.getType().contains("->"))
				continue;
			
			if (globalVar.isParameter() && globalVar.getVar().getVariable().getIndex() == 0) {	
				Node n = ptg.new Node(globalVar.getVar().toString());
				ptg.addParamNode(n);
				ptg.addLabel(globalVar.getVar().toString(), n);
			}
		}
	}

	private Map<ExprVariable, AlloyExpression> currentLocalToGlobalVarMapping;

	@Override
	protected void flowThrough(FlowSet<PointsToGraph> in, CfgNode p, FlowSet<PointsToGraph> out) {
		currentLocalToGlobalVarMapping = p.getLocalToGlobalVarMapping();
		in.copy(out);
		
		PointsToGraph ptg = (PointsToGraph)out.iterator().next();
		DynalloyProgram program = p.getProgram();
		if (program instanceof Assigment) {
			Assigment assignment = (Assigment)program;
			ExprVariable assignedVar = assignment.getLeft();
			AlloyExpression expr = assignment.getRight();
			String type = p.getParentProgramDeclaration().getParameterTyping().get(assignedVar.getVariable());
			
			//We are only interested in references (this takes care of Int and boolean
			//fields and vars alike)
			if (type.contains("Int") || type.contains("boolean"))
				return;
			
			//If there is a mapping, we have to calculate the bounds for the expression
			//contained in it (i.e., the alloy expression), because we have to handle
			//the incarnations correctly working in the alloy world
			if (mapping != null) {
				EqualsFormula equals = (EqualsFormula)mapping.getAlloy(assignment);
				expr = equals.getRight();
				assignedVar = (ExprVariable)equals.getLeft();
				//Since we have to calculate bounds for the global variables, we translate the local names
				//of variables in methods to their global counterparts
				if (currentLocalToGlobalVarMapping.containsKey(assignedVar)) {
					//Since we are assigning to it, the expression for this variable must be an lvalue.
					assignedVar = (ExprVariable)currentLocalToGlobalVarMapping.get(assignedVar);
				}
			}
			
			//Assignment of the form: f := g ++ (Exp1 -> Exp2)
			//We have to modify edges here
			if (expr instanceof ExprOverride) {
				ExprOverride override = (ExprOverride)expr;
				//Consistency check: make sure the 'overriden' is a field
				if (!(override.getLeft() instanceof ExprVariable))
					throw new IllegalStateException("Can't override something different than a field");
				
				//TODO: Separar strong update de weak update
				
				//Copy the old edges from g into f, if there are any
				ExprVariable oldField = (ExprVariable)override.getLeft();
				Set<Edge> oldEdges = ptg.getFieldEdges(oldField);
				if (oldEdges != null) {
					for (Edge oldEdge : oldEdges) {
						ptg.createAndAddEdge(assignedVar, oldEdge.getFrom(), oldEdge.getTo());
					}
				}
				
				//Add the new f edges (Expr1 -> Expr2)
				ExprProduct overriderProduct = (ExprProduct)override.getRight();
				Set<Node> leftNodes = getNodes(overriderProduct.getLeft(), ptg);
				Set<Node> rightNodes = getNodes(overriderProduct.getRight(), ptg);
				ptg.createAndAddEdges(assignedVar, leftNodes, rightNodes);
				
				//TODO: explicar
				((PointsToFlowSet)out).fieldIncarnationUpdated(assignedVar.getVariable().getVariableId().getString(), assignedVar.getVariable().getIndex());
			}
			//Assignment of the form: v := v2 | constant | intLiteral | function | expr.field
			//We have to modify nodes here
			else if (expr instanceof ExprVariable || expr instanceof ExprConstant || expr instanceof ExprIntLiteral
					|| expr instanceof ExprFunction || expr instanceof ExprJoin) {
				Set<Node> reachedNodes = getNodes(expr, ptg);
				ptg.redefineLabel(assignedVar, (Set<Node>)((HashSet<Node>)reachedNodes).clone()); //We need to make a copy here
				
				//TODO: explicar
				((PointsToFlowSet)out).varIncarnationUpdated(assignedVar.getVariable().getVariableId().getString(), assignedVar.getVariable().getIndex());
			}
			else {
				throw new IllegalStateException(String.format("Unknown expression: %s", expr));
			}
		}
		else if (program instanceof InvokeAction) {
			InvokeAction ia = (InvokeAction)program;
			if (ia.getActionId().equals("getUnusedObject") || ia.getActionId().equals("havocVariable2")) {
				//Both getUnusedObject and havocVariable2 have the same semantics:
				//the variable (which is the first parameter), now points to a new node.
				InvokeAction action = (InvokeAction)program;
				ExprVariable var = (ExprVariable)action.getActualParameters().iterator().next();
				
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
					((PointsToFlowSet)out).varIncarnationUpdated(var.getVariable().getVariableId().getString(), var.getVariable().getIndex());
				}
				
				//We create a new node and point the variable to that
				Node newLocalNode = ptg.new Node(String.format("new@%s", var));
				ptg.addParamNode(newLocalNode);
				ptg.redefineLabel(var, newLocalNode);
			}
			else {
				throw new UnsupportedOperationException("InvokeAction not supported.");
			}
		}
		
//		PointsToGraph inPtg = in.iterator().next();
//		PointsToGraph outPtg = in.iterator().next();
//        System.out.println(String.format("IN: Nodes: %d, Edges: %d.  OUT: Nodes: %d, Edges: %d", inPtg.getLocalNodes().size() + inPtg.getParamNodes().size(), inPtg.getEdgeCount(), outPtg.getLocalNodes().size() + outPtg.getParamNodes().size(), outPtg.getEdgeCount()));
	}
	
	/**
	 * Returns the nodes reached by the expression.
	 * Also, it adds the corresponding nodes and edges if a "parameter node" is referenced through
	 * a field not pointing yet to anything.
	 */
	private Set<Node> getNodes(AlloyExpression expr, PointsToGraph ptg) {
		//Base case: expr = Var
		if (expr instanceof ExprVariable) {
			ExprVariable var = (ExprVariable)expr;
			//Again, since everything is calculated only for the global variables, if this is a local
			//one, we need the nodes for the global expression that is the "actual parameter"
			if (currentLocalToGlobalVarMapping.containsKey(var)) {
				AlloyExpression globalExp = currentLocalToGlobalVarMapping.get(var);
				if ((globalExp instanceof ExprVariable))
					var = (ExprVariable)globalExp;
				else
					return getNodes(currentLocalToGlobalVarMapping.get(var), ptg);
			}
			return ptg.getNodesReachedByLabel(var);
		}
		//Base case: expr = Constant
		if (expr instanceof ExprConstant) {
			String id = ((ExprConstant)expr).getConstantId();
			
			if (ptg.getNodesReachedByLabel(id) == null || ptg.getNodesReachedByLabel(id).size() == 0) {
				Node constantNode = ptg.new Node(id);
				ptg.addLocalNode(constantNode);
				ptg.addConstantLabel(id, constantNode);
			}
			return ptg.getNodesReachedByLabel(id);
		}
		//Base case: expr = Function
		if (expr instanceof ExprFunction) {
			throw new IllegalStateException("Funciones no...");
		}
		
		ExprJoin join = (ExprJoin)expr;
		if (!(join.getRight() instanceof ExprVariable))
			throw new UnsupportedOperationException("Oops, esto esta pensado para tener las var a la derecha, si no es el caso, cambiarlo...");
		
		//Recursive case: expr = expr2.field
		Set<Node> nodes = getNodes(join.getLeft(), ptg);
		ExprVariable field = (ExprVariable)join.getRight();
		Set<Node> reachedNodes = new HashSet<Node>();
		for (Node n : nodes) {
			if (ptg.getNodesReachedFromNodeByField(n, field).size() == 0) {
				//If a parameter node is 'loaded' through a field not pointing yet to anything, we have
				//to add another parameter node and connect it through that field.
				if (ptg.isParamNode(n)) {
					Node newParamNode = ptg.new Node(String.format("%s.%s", n, field));
					ptg.addParamNode(newParamNode);
					ptg.createAndAddEdge(field, n, newParamNode);
				}
				//TODO: Esto no vale porque alguien que apuntaba a null era dereferenciado y pinchaba...
				//else
					//throw new IllegalStateException(String.format("Local node '%s' does not point through '%s' to anything yet", n, field));
			}
			reachedNodes.addAll(ptg.getNodesReachedFromNodeByField(n, field));
		}
		
		return reachedNodes;
	}

	@Override
	protected FlowSet<PointsToGraph> newInitialFlow() {
		return new PointsToFlowSet();
	}

	@Override
	protected FlowSet<PointsToGraph> entryInitialFlow() {
		//Make sure to return a copy, in case it gets modified
		FlowSet<PointsToGraph> newInitialFlow = new PointsToFlowSet();
		PointsToGraph newInitialPTG = (PointsToGraph)newInitialFlow.iterator().next();
		newInitialPTG.merge((PointsToGraph)initialFlow.iterator().next());
		return newInitialFlow;
	}

	@Override
	protected void merge(FlowSet<PointsToGraph> in1, FlowSet<PointsToGraph> in2, FlowSet<PointsToGraph> out) {
		if (in1.equals(in2)) {
			in1.copy(out);
			return;
		}
		
		PointsToFlowSet flowset1 = (PointsToFlowSet)in1;
		PointsToFlowSet flowset2 = (PointsToFlowSet)in2;
		PointsToFlowSet flowsetOut = (PointsToFlowSet)out;
		PointsToGraph graph1 = flowset1.iterator().next();
		PointsToGraph graph2 = flowset2.iterator().next();
		PointsToGraph merged = flowsetOut.iterator().next();
		merged.merge(graph1);
		merged.merge(graph2);
		
		for (String varId : flowset1.getHighestVarIncarnationUpdatedTable().keySet()) {
			flowsetOut.varIncarnationUpdated(varId, flowset1.getHighestVarIncarnationUpdatedTable().get(varId));
		}
		for (String varId : flowset2.getHighestVarIncarnationUpdatedTable().keySet()) {
			flowsetOut.varIncarnationUpdated(varId, flowset2.getHighestVarIncarnationUpdatedTable().get(varId));
		}
		for (String fieldId : flowset1.getHighestFieldIncarnationUpdatedTable().keySet()) {
			flowsetOut.fieldIncarnationUpdated(fieldId, flowset1.getHighestFieldIncarnationUpdatedTable().get(fieldId));
		}
		for (String fieldId : flowset2.getHighestFieldIncarnationUpdatedTable().keySet()) {
			flowsetOut.fieldIncarnationUpdated(fieldId, flowset2.getHighestFieldIncarnationUpdatedTable().get(fieldId));
		}
		
		for (String varId : flowset1.getHighestVarIncarnationUpdatedTable().keySet()) {
			if (flowset2.getHighestVarIncarnationUpdatedTable().containsKey(varId)) {
				int incarnationIn1 = flowset1.getHighestVarIncarnationUpdatedTable().get(varId);
				int incarnationIn2 = flowset2.getHighestVarIncarnationUpdatedTable().get(varId);
				Set<Node> nodesIn1 = graph1.getNodesReachedByLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn1)));
				Set<Node> nodesIn2 = graph2.getNodesReachedByLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn2)));
//				Set<Node> nodesMerged = new TreeSet<PointsToGraph.Node>();
//				nodesMerged.addAll(nodesIn1);
//				nodesMerged.addAll(nodesIn2);
				if (incarnationIn1 >= incarnationIn2) {
					for (Node node : nodesIn2) {
						merged.addLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn1)), node);
					}
					//merged.redefineLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn1)), nodesMerged);
				}
				else {
					for (Node node : nodesIn1) {
						merged.addLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn2)), node);
					}
					//merged.redefineLabel(new ExprVariable(new AlloyVariable(varId, incarnationIn2)), nodesMerged);
				}
			}
		}
		
		for (String fieldId : flowset1.getHighestFieldIncarnationUpdatedTable().keySet()) {
			if (flowset2.getHighestFieldIncarnationUpdatedTable().containsKey(fieldId)) {
				int incarnationIn1 = flowset1.getHighestFieldIncarnationUpdatedTable().get(fieldId);
				int incarnationIn2 = flowset2.getHighestFieldIncarnationUpdatedTable().get(fieldId);
				ExprVariable varIn1 = new ExprVariable(new AlloyVariable(fieldId, incarnationIn1));
				ExprVariable varIn2 = new ExprVariable(new AlloyVariable(fieldId, incarnationIn2));
				Set<Edge> edgesIn1 = graph1.getFieldEdges(varIn1);
				Set<Edge> edgesIn2 = graph2.getFieldEdges(varIn2);
				if (incarnationIn1 >= incarnationIn2) {
					for (Edge edge : edgesIn2) {
						merged.addEdge(merged.new Edge(varIn1.toString(), edge.getFrom(), edge.getTo()));
					}
				}
				else {
					for (Edge edge : edgesIn1) {
						merged.addEdge(merged.new Edge(varIn2.toString(), edge.getFrom(), edge.getTo()));
					}
				}
			}
		}
	}

	@Override
	protected void copy(FlowSet<PointsToGraph> source, FlowSet<PointsToGraph> dest) {
		source.copy(dest);
	}
	
	public PointsToGraph getFinalPointsTo() {
		return getTailsMergedFinalFlow().iterator().next();
	}

}
