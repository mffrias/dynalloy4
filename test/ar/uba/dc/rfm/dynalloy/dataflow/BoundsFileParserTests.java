package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public class BoundsFileParserTests {
	
	private static String path = "test/ar/uba/dc/rfm/dynalloy/dataflow/";
		
	@Test
	public void boundsFileParserTest() throws IOException {
		AlloyTyping typing = new AlloyTyping();
		typing.put(new AlloyVariable("field0"), "Node -> lone (Node)");
		typing.put(new AlloyVariable("field1"), "one (Node) -> set (Node)");
		typing.put(new AlloyVariable("field2"), "Node");
		typing.put(new AlloyVariable("field3"), "lone(Node)");
		typing.put(new AlloyVariable("field4"), "Int");
		BoundsFileParser parser = new BoundsFileParser(path + "BoundsFileParserTest.txt", path + "BoundsFileParserTest.xml", typing);
		Set<BoundedVariable> bounds = parser.getBoundsFromFile();
		
		for (BoundedVariable bv : bounds) {
			String var = bv.getVariable().getVariable().getVariableId().getString();
			Set<Bound> resultBounds = bv.getBounds();
			if (var.equals("field0")) {
				assertBoundsEquals(resultBounds,
						Bound.buildBound("N0", "N0"),
						Bound.buildBound("N1", "N1"),
						Bound.buildBound("N2", "N2"),
						Bound.buildBound("N3", "N3"),
						Bound.buildBound("N4", "N4"),
						Bound.buildBound("N0", "null"),
						Bound.buildBound("N1", "null"),
						Bound.buildBound("N2", "null"),
						Bound.buildBound("N3", "null"),
						Bound.buildBound("N4", "null"));
			}
			else if (var.equals("field1")) {
				assertBoundsEmpty(resultBounds);
			}
			else if (var.equals("field2")) {
				assertBoundsEquals(resultBounds,
						Bound.buildBound("N0"),
						Bound.buildBound("N1"),
						Bound.buildBound("null"));
			}
			else if (var.equals("field3")) {
				assertBoundsEmpty(resultBounds);
			}
			else if (var.equals("field4")) {
				assertBoundsEquals(resultBounds,
						Bound.buildBound(1),
						Bound.buildBound(2),
						Bound.buildBound(3),
						Bound.buildBound(5),
						Bound.buildBound(8));
			}
			else {
				assertTrue(false);
			}
		}
	}
	
	private void assertBoundsEquals(Set<Bound> resultBounds, Bound... expected) {
		Set<Bound> expectedBounds = new HashSet<Bound>(Arrays.asList(expected));
		assertTrue(resultBounds.equals(expectedBounds));
	}
	
	private void assertBoundsEmpty(Set<Bound> resultBounds) {
		assertTrue(resultBounds.size() == 0);
	}
}
