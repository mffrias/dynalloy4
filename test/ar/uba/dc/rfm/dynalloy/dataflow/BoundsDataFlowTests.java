package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ArgumentException;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;


public class BoundsDataFlowTests {
	
	private static String path = "test/ar/uba/dc/rfm/dynalloy/dataflow/"; 

	@Test
	public void boundsDataFlowAssignmentConstantTest() throws ArgumentException {
		//Assignment: A := C
		ExprVariable left = createVar("A");
		AlloyExpression right = new ExprConstant(null, "C");
		DynalloyProgram assignment = new Assigment(left, right);
		
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg);
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expected = createBounds(Bound.buildBound("C"));
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(expected.equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowAssignmentIntLiteralTest() throws ArgumentException {
		//Assignment: A := N
		Bound.setBitwidth(7);
		ExprVariable left = createVar("A");
		AlloyExpression right = new ExprIntLiteral(42);
		DynalloyProgram assignment = new Assigment(left, right);
		
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg);
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expected = createBounds(Bound.buildBound(42));
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(expected.equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowAssignmentVarTest() throws ArgumentException {
		//Assignment: A := X
		ExprVariable left = createVar("A");
		AlloyExpression right = createVar("X");
		DynalloyProgram assignment = new Assigment(left, right);
		
		BoundedVariable a = createBoundedVar("A", Bound.buildBound("N0"), Bound.buildBound("N1"));
		BoundedVariable x = createBoundedVar("X", Bound.buildBound("N2"), Bound.buildBound("N3"), Bound.buildBound("N4"));
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, Arrays.asList(a, x));
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(x.getBounds().equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowAssignmentJoinTest() throws ArgumentException {
		//Assignment: A := X.f
		ExprVariable left = createVar("A");
		AlloyExpression right = new ExprJoin(createVar("X"), createVar("f"));
		DynalloyProgram assignment = new Assigment(left, right);
		
		BoundedVariable x = createBoundedVar("X", Bound.buildBound("N1"), Bound.buildBound("N2"), Bound.buildBound("N3"));
		BoundedVariable f = createBoundedVar("f", Bound.buildBound("N1", "N5"), Bound.buildBound("N3", "N6"), Bound.buildBound("N4", "N7"));
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, Arrays.asList(x, f));
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expected = createBounds(Bound.buildBound("N5"), Bound.buildBound("N6"));
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(expected.equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowAssignmentProductTest() throws ArgumentException {
		//Assignment: A := X->Y
		ExprVariable left = createVar("A");
		AlloyExpression right = new ExprProduct(createVar("X"), createVar("Y"));
		DynalloyProgram assignment = new Assigment(left, right);
		
		BoundedVariable x = createBoundedVar("X", Bound.buildBound("N1"), Bound.buildBound("N2"));
		BoundedVariable y = createBoundedVar("Y", Bound.buildBound("N3"), Bound.buildBound("N4"), Bound.buildBound("N5"));
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, Arrays.asList(x, y));
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expected = createBounds(
				Bound.buildBound("N1", "N3"),
				Bound.buildBound("N1", "N4"),
				Bound.buildBound("N1", "N5"),
				Bound.buildBound("N2", "N3"),
				Bound.buildBound("N2", "N4"),
				Bound.buildBound("N2", "N5"));
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(expected.equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowAssignmentOverrideTest() throws ArgumentException {
		//Assignment: A := f ++ (X->Y)
		ExprVariable left = createVar("A");
		AlloyExpression right = new ExprOverride(createVar("f"), new ExprProduct(createVar("X"), createVar("Y")));
		DynalloyProgram assignment = new Assigment(left, right);
		
		BoundedVariable x = createBoundedVar("X", Bound.buildBound("N2"), Bound.buildBound("N5"));
		BoundedVariable y = createBoundedVar("Y", Bound.buildBound("N3"), Bound.buildBound("N4"));
		BoundedVariable f = createBoundedVar("f", Bound.buildBound("N1", "C1"), Bound.buildBound("N1", "D1"), Bound.buildBound("N2", "C2"), Bound.buildBound("N3", "C3"));
		ControlFlowGraph cfg = new ControlFlowGraph(assignment);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, Arrays.asList(x, y, f));
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expected = new LinkedHashSet<Bound>(Arrays.asList(
				Bound.buildBound("N1", "C1"),
				Bound.buildBound("N1", "D1"),
				Bound.buildBound("N2", "C2"),
				Bound.buildBound("N3", "C3"),
				Bound.buildBound("N2", "N3"),
				Bound.buildBound("N2", "N4"),
				Bound.buildBound("N5", "N3"),
				Bound.buildBound("N5", "N4")));
		BoundedVariable endFlowForA = getBoundsFor(flow, "A");
		
		assertTrue(expected.equals(endFlowForA.getBounds()));
	}
	
	@Test
	public void boundsDataFlowConstantPropagationTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		ProgramDeclaration program = MainCommon.getFirstProgram(path + "ConstantPropagationProgram.dals");
		
		ControlFlowGraph cfg = new ControlFlowGraph(program.getBody());
		BoundsDataFlow analysis = new BoundsDataFlow(cfg);
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expectedForA = new LinkedHashSet<Bound>(Arrays.asList(
				Bound.buildBound("c"),
				Bound.buildBound("d")));
		BoundedVariable endFlowForA = getBoundsFor(flow, "a");
		
		Set<Bound> expectedForB = createBounds(
				Bound.buildBound(1));
		BoundedVariable endFlowForB = getBoundsFor(flow, "b");
		
		assertTrue(expectedForA.equals(endFlowForA.getBounds()));
		assertTrue(expectedForB.equals(endFlowForB.getBounds()));
	}
	
	@Test
	public void boundsDataFlowNumericFunctionsTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		ProgramDeclaration program = MainCommon.getFirstProgram(path + "NumericFunctionsProgram.dals");
		
		BoundedVariable a = createBoundedVar("a", Bound.buildBound(1), Bound.buildBound(2), Bound.buildBound(3));
		BoundedVariable b = createBoundedVar("b", Bound.buildBound(-1), Bound.buildBound(-2));
		ControlFlowGraph cfg = new ControlFlowGraph(program.getBody());
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, Arrays.asList(a, b));
		analysis.performAnalysis();
		
		FlowSet<BoundedVariable> flow = analysis.getTailsMergedFinalFlow();
		Set<Bound> expectedForA = createBounds(
				Bound.buildBound(0),
				Bound.buildBound(1),
				Bound.buildBound(2),
				Bound.buildBound(3),
				Bound.buildBound(4));
		BoundedVariable endFlowForA = getBoundsFor(flow, "a");
		
		Set<Bound> expectedForB = createBounds(
				Bound.buildBound(5),
				Bound.buildBound(6),
				Bound.buildBound(7));
		BoundedVariable endFlowForB = getBoundsFor(flow, "b");
		
		assertTrue(expectedForA.equals(endFlowForA.getBounds()));
		assertTrue(expectedForB.equals(endFlowForB.getBounds()));
	}
	
	@Test
	public void boundsDataFlowSimpeInvokeProgramTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		Set<BoundedVariable> finalFlow = MainBounds.getAlloyDataflowBounds(
				"P1",
				path + "SimpleInvokeProgram.dals", 
				path + "SimpleInvokeProgram.txt", 
				path + "SimpleInvokeProgram.xml");
		
		assertEquals(9, finalFlow.size());
		
		{
			Set<Bound> result = getBoundsFor(finalFlow, "x_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(1),
					Bound.buildBound(2));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l1_foo_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(1),
					Bound.buildBound(2));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l1_foo_2").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l1_l0_bar_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l1_foo_3").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(3),
					Bound.buildBound(4));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l2_foo_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l2_foo_2").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(3),
					Bound.buildBound(4));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l2_l0_bar_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(3),
					Bound.buildBound(4));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l2_l2_foo_3").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(4),
					Bound.buildBound(5));
			assertEquals(expected, result);
		}
	}
	
	@Test
	public void boundsDataFlowInvokeProgramWithCollidingLocalsInDifferentLevels() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		Set<BoundedVariable> finalFlow = MainBounds.getAlloyDataflowBounds(
				"P1",
				path + "CollidingLocalsInvokeProgram.dals", 
				path + "CollidingLocalsInvokeProgram.txt", 
				path + "CollidingLocalsInvokeProgram.xml");
		
		assertEquals(11, finalFlow.size());
		
		{
			Set<Bound> result = getBoundsFor(finalFlow, "x_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l1_local_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(3),
					Bound.buildBound(4));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l1_local_2").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(4),
					Bound.buildBound(5));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l1_local_3").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(5),
					Bound.buildBound(6));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l1_l0_local_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(5),
					Bound.buildBound(6));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l2_local_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(4),
					Bound.buildBound(5));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l2_local_2").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(5),
					Bound.buildBound(6));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l2_local_3").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(6),
					Bound.buildBound(7));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l2_l0_local_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(6),
					Bound.buildBound(7));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "y_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l3_l3_local_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(2),
					Bound.buildBound(3));
			assertEquals(expected, result);
		}
	}
	
	@Test
	public void boundsDataFlowAlloyIncarnationSynchronizationTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		Set<BoundedVariable> finalFlow = MainBounds.getAlloyDataflowBounds(
				"AlloyIncarnationSynchronizationProgram",
				path + "AlloyIncarnationSynchronizationProgram.dals", 
				path + "AlloyIncarnationSynchronizationProgram.txt", 
				path + "AlloyIncarnationSynchronizationProgram.xml");
		
		{
			Set<Bound> result = getBoundsFor(finalFlow, "z_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound(0),
					Bound.buildBound(1),
					Bound.buildBound(2),
					Bound.buildBound(3),
					Bound.buildBound(4),
					Bound.buildBound(5),
					Bound.buildBound(6));
			assertEquals(expected, result);
		}
	}
	
	@Test
	public void boundsDataFlowGlobalVarNestedResolutionTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
		Set<BoundedVariable> finalFlow = MainBounds.getAlloyDataflowBounds(
				"GlobalVarNestedResolution",
				path + "GlobalVarNestedResolution.dals", 
				path + "GlobalVarNestedResolution.txt", 
				path + "GlobalVarNestedResolution.xml");
		
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l1_l1_l0_foo_1").getBounds();
			Set<Bound> expected = createBounds(Bound.buildBound(1));
			assertEquals(expected, result);
		}
		{
			Set<Bound> result = getBoundsFor(finalFlow, "l1_l1_l0_bar_1").getBounds();
			Set<Bound> expected = createBounds(Bound.buildBound(2));
			assertEquals(expected, result);
		}
	}
	
	@Test
	public void boundsDataFlowEndToEndCListRemove15Test() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {		
		Set<BoundedVariable> finalFlow = MainBounds.getAlloyDataflowBounds(
				"CacheList_remove_0",
				path + "CList_rem-s15.dals", 
				path + "CList_15_state_golden.txt", 
				path + "CList_15_state_golden.xml");
		
		{
			//This is the only nullDerefBool set only to false, because it is initialized like that
			Set<Bound> result = getBoundsFor(finalFlow, "l0_nullDerefBool_1").getBounds();
			Set<Bound> expected = createBounds(
					Bound.buildBound("false"));
			assertEquals(expected, result);
		}
		{
			//This variable is set during the loop unroll
			Set<Bound> result = getBoundsFor(finalFlow, "l0_currentIndex_11").getBounds();
			Set<Bound> expected = new LinkedHashSet<Bound>();
			for (int i = -10; i <= 14; i++)
				expected.add(Bound.buildBound(i));
			assertEquals(expected, result);
		}
		{
			//This variable is set during the loop unroll
			Set<Bound> result = getBoundsFor(finalFlow, "l0_node1_4").getBounds();
			Set<Bound> expected = new LinkedHashSet<Bound>();
			expected.add(Bound.buildBound("null"));
			for (int i = 0; i <= 13; i++)
				expected.add(Bound.buildBound("N" + i));
			assertEquals(expected, result);
		}
		{
			//This variable is a parameter to the program, which comes with initial bounds
			Set<Bound> result = getBoundsFor(finalFlow, "listSize_1").getBounds();
			Set<Bound> expected = new LinkedHashSet<Bound>();
			for (int i = -1; i <= 14; i++)
				expected.add(Bound.buildBound("CL0", i));
			assertEquals(expected, result);
		}
	}
	
	private ExprVariable createVar(String varId) {
		return new ExprVariable(BoundedVariable.createAlloyVar(varId));
	}
	
	private Set<Bound> createBounds(Bound... bounds) {
		return new LinkedHashSet<Bound>(Arrays.asList(bounds));
	}
	
	private BoundedVariable createBoundedVar(String variableId, Bound... bounds) {
		ExprVariable var = createVar(variableId);
		Set<Bound> boundsSet = createBounds(bounds);
		
		return new BoundedVariable(var, boundsSet);
	}
	
	private BoundedVariable getBoundsFor(Set<BoundedVariable> bounds, String variableId) throws ArgumentException {
		for (BoundedVariable bv : bounds)
			if (bv.getVariable().toString().equals(variableId))
				return bv;
		
		throw new ArgumentException(String.format("VariableId not found: %s", variableId));
	}
	
	private BoundedVariable getBoundsFor(FlowSet<BoundedVariable> flow, String variableId) throws ArgumentException {
		BoundsFlowSet bfs = (BoundsFlowSet)flow;
		ExprVariable var = new ExprVariable(new AlloyVariable(variableId));
		if (!bfs.isVariableBounded(var))
			throw new ArgumentException(String.format("VariableId not found: %s", variableId));
		
		return bfs.getBounds(var);
	}
}
