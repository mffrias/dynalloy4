package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ForwardFlowAnalysis<T> {
	/** Maps graph nodes to OUT sets. */
    protected Map<CfgNode, FlowSet<T>> unitToAfterFlow;
	/** Maps graph nodes to IN sets. */
    protected Map<CfgNode, FlowSet<T>> unitToBeforeFlow;
	protected ControlFlowGraph graph;
	
	public ForwardFlowAnalysis(ControlFlowGraph cfg) {
		graph = cfg;
		unitToAfterFlow = new HashMap<CfgNode, FlowSet<T>>(graph.getNodes().size() * 2 + 1, 0.7f);
		unitToBeforeFlow = new HashMap<CfgNode, FlowSet<T>>(graph.getNodes().size() * 2 + 1, 0.7f);
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
	protected abstract void flowThrough(FlowSet<T> in, CfgNode p, FlowSet<T> out);
	
	/** 
     * Returns the flow object corresponding to the initial values for
     * each graph node. 
     */
    protected abstract FlowSet<T> newInitialFlow();

    /**
     * Returns the initial flow value for entry/exit graph nodes.
     */
    protected abstract FlowSet<T> entryInitialFlow();
    
    /** Compute the merge of the <code>in1</code> and <code>in2</code> sets, putting the result into <code>out</code>. 
     * The behavior of this function depends on the implementation ( it may be necessary to check whether
     * <code>in1</code> and <code>in2</code> are equal or aliased ). 
     * Used by the doAnalysis method. */
    protected abstract void merge(FlowSet<T> in1, FlowSet<T> in2, FlowSet<T> out);
    
    /**
     * Merges in1 and in2 into out, just before node succNode.
     * By default, this method just calls merge(A,A,A), ignoring
     * the node.
     */
    protected void merge(CfgNode succNode, FlowSet<T> in1, FlowSet<T> in2, FlowSet<T> out) {
    	merge(in1,in2,out);
    }
    
    /** Creates a copy of the <code>source</code> flow object in <code>dest</code>. */
    protected abstract void copy(FlowSet<T> source, FlowSet<T> dest);
    
    /**
     * Merges in into inout, just before node succNode.
     */
    protected void mergeInto(CfgNode succNode, FlowSet<T> inout, FlowSet<T> in) {
    	FlowSet<T> tmp = newInitialFlow();
        merge(succNode, inout, in, tmp);
        copy(tmp, inout);
    }
    
    /** Accessor function returning value of IN set for s. */
//    public FlowSet getFlowBefore(CfgNode s)
//    {
//        return unitToBeforeFlow.get(s);
//    }
    
    /** Accessor function returning value of OUT set for s. */
//    public FlowSet<T> getFlowAfter(CfgNode s)
//    {
//        return unitToAfterFlow.get(s);
//    }
    
    public FlowSet<T> getTailsMergedFinalFlow() {
    	//If there is more than one tail, we merge the flow info from each one of them
		//to get a consistent final result
    	FlowSet<T> finalFlow = newInitialFlow();
    	for (CfgNode tail : graph.getTails()) {
    		mergeInto(tail, finalFlow, unitToAfterFlow.get(tail));
		}
    	return finalFlow;
    }

    public void performAnalysis() {
    	doAnalysis();
    }
    
	protected void doAnalysis() {
		//Since we don't have loops in this level of Dynalloy (loop unroll has been performed),
	    //we can use a simpler and more efficient algorithm here
	     
		AbstractQueue<CfgNode> changedUnitsQueue = new LinkedBlockingQueue<CfgNode>();		
		Set<CfgNode> processedNodes = new HashSet<CfgNode>();
		
		//Assign the initial flow for all the heads
		for (CfgNode head : graph.getHeads()) {
			unitToBeforeFlow.put(head, entryInitialFlow());
			changedUnitsQueue.add(head);
		}
		
		while (!changedUnitsQueue.isEmpty()) {
			CfgNode s = changedUnitsQueue.remove();
			
			//Check that all the predecessors of this node have been processed.
			//If not, remove it from the list, it will be processed later after
			//one of those unfinished predecessors adds it again.
			boolean ready = true;
			for (CfgNode pred : s.getPredecessors()) {
				if (!processedNodes.contains(pred)) {
					ready = false;
					break;
				}
			}
			if (!ready)
				continue;
			
			//Compute the AfterFlow for the node
			FlowSet<T> afterFlow = newInitialFlow();
			flowThrough(unitToBeforeFlow.get(s), s, afterFlow);
			if (graph.getTails().contains(s))
				unitToAfterFlow.put(s, afterFlow);
			processedNodes.add(s);
			//We don't need the beforeFlow anymore, so release that memory
			unitToBeforeFlow.remove(s);
			
			//Merge this AfterFlow into every successor BeforeFlow
			for (CfgNode succ : s.getSuccessors()) {
				FlowSet<T> succBeforeFlow;
				if (unitToBeforeFlow.containsKey(succ))
					succBeforeFlow = unitToBeforeFlow.get(succ);
				else {
					succBeforeFlow = newInitialFlow();
					unitToBeforeFlow.put(succ, succBeforeFlow);
				}
				mergeInto(s, succBeforeFlow, afterFlow);
				
				if (!changedUnitsQueue.contains(succ))
					changedUnitsQueue.add(succ);
			}
		}
		
		//Just a sanity check
		if (processedNodes.size() != graph.getNodes().size())
			throw new IllegalStateException("Oops, this can't be right...");
	}
}
