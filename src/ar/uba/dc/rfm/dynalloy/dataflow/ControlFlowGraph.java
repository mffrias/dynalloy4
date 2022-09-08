package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class ControlFlowGraph {
	private List<CfgNode> nodes;
	private Set<CfgNode> heads;
	private Set<CfgNode> tails;
	
	/**
	 * Constructs a simple intra-procedural CFG
	 */
	public ControlFlowGraph(DynalloyProgram program) {
		this(createDummyProgramDeclaration(program), null);
	}
	
	/**
	 * Constructs the CFG of the specified program, using the SpecContext to resolve
	 * any InvokeProgram statements, and merging the programs' CFGs using cloning
	 * (i.e.: adding the CFG of the called program to the one of the caller) 
	 */
	public ControlFlowGraph(ProgramDeclaration p, SpecContext ctx) {
		nodes = new LinkedList<CfgNode>();
		
		CfgBuilder builder = new CfgBuilder(ctx, p);
		CfgBuilder.CfgBuilderNode builderNode = (CfgBuilder.CfgBuilderNode)p.getBody().accept(builder);
		
		addNodesAndSuccessors(builderNode.getEntryPoints());
		
		buildHeadsAndTails();
	}

	public List<CfgNode> getNodes() {
		return nodes;
	}
	
	public Set<CfgNode> getHeads() {
		return heads;
	}
	
	public Set<CfgNode> getTails() {
		return tails;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			CfgNode node = nodes.get(i);
			if (node.getSuccessors().isEmpty()) {
				sb.append(String.format("{%d} %s\n", i, node.getProgram()));
			}
			else {
				for (CfgNode successor : node.getSuccessors()) {
					int successorIndex = nodes.indexOf(successor);
					sb.append(String.format("{%d} %s -> {%d} %s\n", i, node.getProgram(), successorIndex, successor.getProgram()));
				}
			}
		}
		return sb.toString();
	}
	
	private void addNodesAndSuccessors(Collection<CfgNode> nodesToAdd) {
		Queue<CfgNode> successors = new LinkedList<CfgNode>();
		for (CfgNode node : nodesToAdd) {
			if (!nodes.contains(node)) {
				nodes.add(node);
				successors.addAll(node.getSuccessors());
			}
		}
		
		if (!successors.isEmpty())
			addNodesAndSuccessors(successors);
	}
	
	private void buildHeadsAndTails() {
		heads = new LinkedHashSet<CfgNode>();
		tails = new LinkedHashSet<CfgNode>();
		for (CfgNode node : getNodes()) {
			if (node.getPredecessors().isEmpty())
				heads.add(node);
			if (node.getSuccessors().isEmpty())
				tails.add(node);
		}	
	}
	
	private static ProgramDeclaration createDummyProgramDeclaration(DynalloyProgram program) {
		return new ProgramDeclaration("", new LinkedList<VariableId>(), new LinkedList<VariableId>(), program, new AlloyTyping(), new ArrayList<AlloyFormula>(), new AlloyTyping());
	}
}
