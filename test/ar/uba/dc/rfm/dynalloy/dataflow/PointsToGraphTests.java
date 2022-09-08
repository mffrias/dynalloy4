package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.dataflow.PointsToGraph.Node;

public class PointsToGraphTests {

	@Test
	public void PointsToGraphTest() {
		PointsToGraph ptg1 = new PointsToGraph();
		PointsToGraph ptg2 = new PointsToGraph();
		PointsToGraph merged = new PointsToGraph();
		
		Node n1 = merged.new Node("n1");
		Node n2 = merged.new Node("n2");
		Node n3 = merged.new Node("n3");
		Node n4 = merged.new Node("n4");
		Node n5 = merged.new Node("n5");
		
		ptg1.addLocalNode(n1);
		ptg1.addLocalNode(n2);
		ptg1.addLocalNode(n3);
		ptg1.addLabel(createVar("ptg1A"), n1);
		ptg1.addLabel(createVar("ptg1A"), n2);
		ptg1.addLabel(createVar("ptg1B"), n3);
		ptg1.addLabel(createVar("ptg1C"), n3);
		ptg1.createAndAddEdge(createVar("f"), n1, n2);
		ptg1.createAndAddEdge(createVar("f"), n1, n3);
		ptg1.createAndAddEdge(createVar("g"), n2, n3);
		ptg1.createAndAddEdge(createVar("h"), n3, n1);
		
		ptg2.addLocalNode(n3);
		ptg2.addLocalNode(n4);
		ptg2.addLocalNode(n5);
		ptg2.addLabel(createVar("ptg2A"), n3);
		ptg2.addLabel(createVar("ptg2B"), n4);
		ptg2.createAndAddEdge(createVar("f"), n3, n4);
		ptg2.createAndAddEdge(createVar("f"), n4, n3);

		merged.merge(ptg1);
		merged.merge(ptg2);
		
		assertEquals(5, merged.getLocalNodes().size());
		assertEquals(2, merged.getEdgesFromNode(n3).size());
		assertEquals(5, merged.getLabels().keySet().size());
		assertEquals(2, merged.getLabels().get("ptg1A").size());
	}
	
	private ExprVariable createVar(String id) {
		return new ExprVariable(new AlloyVariable(id));
	}
}
