package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.*;

import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

public abstract class ForwardFlowAnalysisFromSoot {
	/** Maps graph nodes to OUT sets. */
    protected Map<CfgNode, FlowSet> unitToAfterFlow;
	/** Maps graph nodes to IN sets. */
    protected Map<CfgNode, FlowSet> unitToBeforeFlow;
	protected ControlFlowGraph graph;

	public ForwardFlowAnalysisFromSoot(ControlFlowGraph cfg) {
		graph = cfg;
		unitToAfterFlow = new HashMap<CfgNode, FlowSet>(graph.getNodes().size() * 2 + 1, 0.7f);
		unitToBeforeFlow = new HashMap<CfgNode, FlowSet>(graph.getNodes().size() * 2 + 1, 0.7f);
	}
	
	/** Given the merge of the <code>out</code> sets, compute the <code>in</code> set for <code>s</code> (or in to out, depending on direction).
    *
    * This function often causes confusion, because the same interface
    * is used for both forward and backward flow analyses. The first
    * parameter is always the argument to the flow function (i.e. it
    * is the "in" set in a forward analysis and the "out" set in a
    * backward analysis), and the third parameter is always the result
    * of the flow function (i.e. it is the "out" set in a forward
    * analysis and the "in" set in a backward analysis).
    * */
	protected abstract void flowThrough(FlowSet in, CfgNode p, FlowSet out);
	
	/** 
     * Returns the flow object corresponding to the initial values for
     * each graph node. 
     */
    protected abstract FlowSet newInitialFlow();

    /**
     * Returns the initial flow value for entry/exit graph nodes.
     */
    protected abstract FlowSet entryInitialFlow();
    
    /** Compute the merge of the <code>in1</code> and <code>in2</code> sets, putting the result into <code>out</code>. 
     * The behavior of this function depends on the implementation ( it may be necessary to check whether
     * <code>in1</code> and <code>in2</code> are equal or aliased ). 
     * Used by the doAnalysis method. */
    protected abstract void merge(FlowSet in1, FlowSet in2, FlowSet out);
    
    /**
     * Merges in1 and in2 into out, just before node succNode.
     * By default, this method just calls merge(A,A,A), ignoring
     * the node.
     */
    protected void merge(CfgNode succNode, FlowSet in1, FlowSet in2, FlowSet out) {
    	merge(in1,in2,out);
    }
    
    /** Creates a copy of the <code>source</code> flow object in <code>dest</code>. */
    protected abstract void copy(FlowSet source, FlowSet dest);
    
    /**
     * Merges in into inout, just before node succNode.
     */
    protected void mergeInto(CfgNode succNode, FlowSet inout, FlowSet in) {
    	FlowSet tmp = newInitialFlow();
        merge(succNode, inout, in, tmp);
        copy(tmp, inout);
    }
    
    /** Accessor function returning value of IN set for s. */
    public FlowSet getFlowBefore(CfgNode s)
    {
        return unitToBeforeFlow.get(s);
    }
    
    /** Accessor function returning value of OUT set for s. */
    public FlowSet getFlowAfter(CfgNode s)
    {
        return unitToAfterFlow.get(s);
    }

	protected void doAnalysis() {
		final Map<CfgNode, Integer> numbers = new HashMap<CfgNode, Integer>();
		// Timers.v().orderComputation = new soot.Timer();
		// Timers.v().orderComputation.start();
		List<CfgNode> orderedUnits = graph.getNodes();
		// Timers.v().orderComputation.end();
		int i = 1;
		for (Iterator<CfgNode> uIt = orderedUnits.iterator(); uIt
				.hasNext();) {
			final CfgNode u = uIt.next();
			numbers.put(u, new Integer(i));
			i++;
		}

		Collection<CfgNode> changedUnits = constructWorklist(numbers);

		Set<CfgNode> heads = graph.getHeads();
		int numNodes = graph.getNodes().size();
		int numComputations = 0;

		// Set initial values and nodes to visit.
		{
			Iterator<CfgNode> it = graph.getNodes().iterator();

			while (it.hasNext()) {
				CfgNode s = it.next();

				changedUnits.add(s);

				unitToBeforeFlow.put(s, newInitialFlow());
				unitToAfterFlow.put(s, newInitialFlow());
			}
		}

		// Feng Qian: March 07, 2002
		// Set initial values for entry points
		{
			Iterator<CfgNode> it = heads.iterator();

			while (it.hasNext()) {
				CfgNode s = it.next();
				// this is a forward flow analysis
				unitToBeforeFlow.put(s, entryInitialFlow());
			}
		}

		// Perform fixed point flow analysis
		{
			FlowSet previousAfterFlow = newInitialFlow();

			while (!changedUnits.isEmpty()) {
				FlowSet beforeFlow;
				FlowSet afterFlow;

				// get the first object
				CfgNode s = changedUnits.iterator().next();
				changedUnits.remove(s);
				boolean isHead = heads.contains(s);

				copy(unitToAfterFlow.get(s), previousAfterFlow);

				// Compute and store beforeFlow
				{
					Set<CfgNode> preds = s.getPredecessors();

					beforeFlow = unitToBeforeFlow.get(s);

					if (preds.size() == 1)
						copy(unitToAfterFlow.get(preds.iterator().next()), beforeFlow);
					else if (preds.size() != 0) {
						Iterator<CfgNode> predIt = preds.iterator();

						copy(unitToAfterFlow.get(predIt.next()), beforeFlow);

						while (predIt.hasNext()) {
							FlowSet otherBranchFlow = unitToAfterFlow
									.get(predIt.next());
							mergeInto(s, beforeFlow, otherBranchFlow);
						}
					}

					if (isHead && preds.size() != 0) {
						mergeInto(s, beforeFlow, entryInitialFlow());
					}
				}

				{
					// Compute afterFlow and store it.
					afterFlow = unitToAfterFlow.get(s);
					flowThrough(beforeFlow, s, afterFlow);
					numComputations++;
				}

				// Update queue appropriately
				if (!afterFlow.equals(previousAfterFlow)) {
					Iterator<CfgNode> succIt = s.getSuccessors().iterator();

					while (succIt.hasNext()) {
						CfgNode succ = succIt.next();

						changedUnits.add(succ);
					}
				}
			}
		}

		// G.v().out.println(graph.getBody().getMethod().getSignature() +
		// " numNodes: " + numNodes +
		// " numComputations: " + numComputations + " avg: " +
		// Main.truncatedOf((double) numComputations / numNodes, 2));

		// Timers.v().totalFlowNodes += numNodes;
		// Timers.v().totalFlowComputations += numComputations;
	}
	
	protected Collection<CfgNode> constructWorklist(final Map<CfgNode, Integer> numbers) {
		return new TreeSet<CfgNode>( new Comparator<CfgNode>() {
            public int compare(CfgNode o1, CfgNode o2) {
                Integer i1 = numbers.get(o1);
                Integer i2 = numbers.get(o2);
                return (i1.intValue() - i2.intValue());
            }
        } );
	}
}
