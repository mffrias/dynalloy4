package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class CfgBuilderTests {
	
	private static String path = "test/ar/uba/dc/rfm/dynalloy/dataflow/"; 
	
	@Test
	public void cfgBuilderSimpleTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound  {
		ProgramDeclaration program = MainCommon.getFirstProgram(path + "SimpleProgram.dals");
		
		//SimpleProgram: (a := b + b := a); DoNothing[a]; (DoNothing[a] + skip)
		ControlFlowGraph cfg = new ControlFlowGraph(program.getBody());
		
		//Amount of nodes 
		List<CfgNode> nodes = cfg.getNodes(); 
		assertEquals(7, nodes.size()); //Every choice adds a Skip
		
		//CFG nodes and edges
		//node[0]: a := b
		assertTrue(nodes.get(0).getProgram() instanceof Assigment);
		assertSetEmpty(nodes.get(0).getPredecessors());
		assertSetEquals(nodes.get(0).getSuccessors(), nodes.get(2));
	
		//node[1]: b := a
		assertTrue(nodes.get(1).getProgram() instanceof Assigment);
		assertSetEmpty(nodes.get(1).getPredecessors());
		assertSetEquals(nodes.get(1).getSuccessors(), nodes.get(2));
		
		//node[2]: Skip (added by CfgBuilder)
		assertTrue(nodes.get(2).getProgram() instanceof Skip);
		assertSetEquals(nodes.get(2).getPredecessors(), nodes.get(0), nodes.get(1));
		assertSetEquals(nodes.get(2).getSuccessors(), nodes.get(3));
		
		//node[3]: DoNothing[a]
		assertTrue(nodes.get(3).getProgram() instanceof InvokeAction);
		assertSetEquals(nodes.get(3).getPredecessors(), nodes.get(2));
		assertSetEquals(nodes.get(3).getSuccessors(), nodes.get(4), nodes.get(5));
		
		//node[4]: DoNothing[a]
		assertTrue(nodes.get(4).getProgram() instanceof InvokeAction);
		assertSetEquals(nodes.get(4).getPredecessors(), nodes.get(3));
		assertSetEquals(nodes.get(4).getSuccessors(), nodes.get(6));
		
		//node[5]: Skip
		assertTrue(nodes.get(5).getProgram() instanceof Skip);
		assertSetEquals(nodes.get(5).getPredecessors(), nodes.get(3));
		assertSetEquals(nodes.get(5).getSuccessors(), nodes.get(6));
		
		//node[6]: Skip (added by CfgBuilder)
		assertTrue(nodes.get(6).getProgram() instanceof Skip);
		assertSetEquals(nodes.get(6).getPredecessors(), nodes.get(4), nodes.get(5));
		assertSetEmpty(nodes.get(6).getSuccessors());
		
		//CFG heads and tails
		assertSetEquals(cfg.getHeads(), nodes.get(0), nodes.get(1));
		assertSetEquals(cfg.getTails(), nodes.get(6));
	}
	
	@Test
	public void cfgBuilderComplexTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound  {
		ProgramDeclaration program = MainCommon.getFirstProgram(path + "ComplexProgram.dals");
	
		ControlFlowGraph cfg = new ControlFlowGraph(program.getBody());

		//Amount of nodes 
		List<CfgNode> nodes = cfg.getNodes(); 
		assertEquals(16, nodes.size()); //12 nodes, plus 4 choices that create extra Skips
		
		//CFG nodes and edges
		assertTrue(nodes.get(0).getProgram().toString().equals("a:=b"));
		assertSetEmpty(nodes.get(0).getPredecessors());
		assertSetEquals(nodes.get(0).getSuccessors(), nodes.get(3));
		
		assertTrue(nodes.get(1).getProgram().toString().equals("b:=a"));
		assertSetEmpty(nodes.get(1).getPredecessors());
		assertSetEquals(nodes.get(1).getSuccessors(), nodes.get(4));

		assertTrue(nodes.get(2).getProgram().toString().equals("a:=(b).(f)"));
		assertSetEmpty(nodes.get(2).getPredecessors());
		assertSetEquals(nodes.get(2).getSuccessors(), nodes.get(3));
		
		//Added by CfgBuilder
		assertTrue(nodes.get(3).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(3).getPredecessors(), nodes.get(0), nodes.get(2), nodes.get(10));
		assertSetEquals(nodes.get(3).getSuccessors(), nodes.get(5), nodes.get(6));
		
		assertTrue(nodes.get(4).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(4).getPredecessors(), nodes.get(1));
		assertSetEquals(nodes.get(4).getSuccessors(), nodes.get(7), nodes.get(8));
		
		assertTrue(nodes.get(5).getProgram().toString().equals("(DummyPred[a])?"));
		assertSetEquals(nodes.get(5).getPredecessors(), nodes.get(3));
		assertSetEquals(nodes.get(5).getSuccessors(), nodes.get(9));
		
		assertTrue(nodes.get(6).getProgram().toString().equals("f:=(f)++((a)->(b))"));
		assertSetEquals(nodes.get(6).getPredecessors(), nodes.get(3));
		assertSetEquals(nodes.get(6).getSuccessors(), nodes.get(9));		
		
		assertTrue(nodes.get(7).getProgram().toString().equals("a:=a"));
		assertSetEquals(nodes.get(7).getPredecessors(), nodes.get(4));
		assertSetEquals(nodes.get(7).getSuccessors(), nodes.get(10));
		
		assertTrue(nodes.get(8).getProgram().toString().equals("b:=b"));
		assertSetEquals(nodes.get(8).getPredecessors(), nodes.get(4));
		assertSetEquals(nodes.get(8).getSuccessors(), nodes.get(10));
		
		//Added by CfgBuilder
		assertTrue(nodes.get(9).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(9).getPredecessors(), nodes.get(5), nodes.get(6));
		assertSetEquals(nodes.get(9).getSuccessors(), nodes.get(11));
		
		//Added by CfgBuilder
		assertTrue(nodes.get(10).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(10).getPredecessors(), nodes.get(7), nodes.get(8));
		assertSetEquals(nodes.get(10).getSuccessors(), nodes.get(3));
		
		assertTrue(nodes.get(11).getProgram().toString().equals("(DummyPred[a])?"));
		assertSetEquals(nodes.get(11).getPredecessors(), nodes.get(9));
		assertSetEquals(nodes.get(11).getSuccessors(), nodes.get(12));
		
		assertTrue(nodes.get(12).getProgram().toString().equals("f:=(f)++((a)->(null/sub[[b, 1]]))"));
		assertSetEquals(nodes.get(12).getPredecessors(), nodes.get(11));
		assertSetEquals(nodes.get(12).getSuccessors(), nodes.get(13), nodes.get(14));
		
		assertTrue(nodes.get(13).getProgram().toString().equals("DoNothing[a]"));
		assertSetEquals(nodes.get(13).getPredecessors(), nodes.get(12));
		assertSetEquals(nodes.get(13).getSuccessors(), nodes.get(15));
		
		assertTrue(nodes.get(14).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(14).getPredecessors(), nodes.get(12));
		assertSetEquals(nodes.get(14).getSuccessors(), nodes.get(15));
		
		//Added by CfgBuilder
		assertTrue(nodes.get(15).getProgram().toString().equals("skip"));
		assertSetEquals(nodes.get(15).getPredecessors(), nodes.get(13), nodes.get(14));
		assertSetEmpty(nodes.get(15).getSuccessors());
		
		//CFG heads and tails
		assertSetEquals(cfg.getHeads(), nodes.get(0), nodes.get(1), nodes.get(2));
		assertSetEquals(cfg.getTails(), nodes.get(15));
	}
	
	@Test
	public void cfgHeadsAndTailsTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		ProgramDeclaration program = MainCommon.getFirstProgram(path + "MultipleHeadsAndTailsProgram.dals");
		
		//Program: (a := b) + (DoNothing[a]) + (skip) + (b := a)
		ControlFlowGraph cfg = new ControlFlowGraph(program.getBody());
		
		//Amount of nodes 
		List<CfgNode> nodes = cfg.getNodes(); 
		assertEquals(5, nodes.size()); //4 nodes and 1 extra Skip
		
		//CFG nodes and edges
		assertTrue(nodes.get(0).getProgram() instanceof Assigment);
		assertSetEmpty(nodes.get(0).getPredecessors());
		assertSetEquals(nodes.get(0).getSuccessors(), nodes.get(4));
		
		assertTrue(nodes.get(1).getProgram() instanceof InvokeAction);
		assertSetEmpty(nodes.get(1).getPredecessors());
		assertSetEquals(nodes.get(1).getSuccessors(), nodes.get(4));
		
		assertTrue(nodes.get(2).getProgram() instanceof Skip);
		assertSetEmpty(nodes.get(2).getPredecessors());
		assertSetEquals(nodes.get(2).getSuccessors(), nodes.get(4));
		
		assertTrue(nodes.get(3).getProgram() instanceof Assigment);
		assertSetEmpty(nodes.get(3).getPredecessors());
		assertSetEquals(nodes.get(3).getSuccessors(), nodes.get(4));
		
		//CFG heads and tails
		assertSetEquals(cfg.getHeads(), nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));
		assertSetEquals(cfg.getTails(), nodes.get(4));
	}
	
	@Test
	public void cfgBuilderMultipleCallSimpleProgramTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound  {
		DynalloyModule module = MainCommon.getModules(path + "MultipleCallSimpleProgram.dals");
		SpecContext context = new SpecContext(module);
		context.switchToModule(module.getModuleId());
		
		//MultipleCallSimpleProgram, after inlining the calls, should be:
		//a := b + skip
		ControlFlowGraph cfg = new ControlFlowGraph(module.getPrograms().iterator().next(), context);
		
		//Amount of nodes 
		List<CfgNode> nodes = cfg.getNodes(); 
		assertEquals(3, nodes.size()); //2 nodes in choice, plus extra Skip
		
		//CFG nodes and edges
		assertTrue(nodes.get(0).getProgram() instanceof Assigment);
		assertSetEmpty(nodes.get(0).getPredecessors());
		assertSetEquals(nodes.get(0).getSuccessors(), nodes.get(2));
		
		assertTrue(nodes.get(1).getProgram() instanceof Skip);
		assertSetEmpty(nodes.get(1).getPredecessors());
		assertSetEquals(nodes.get(1).getSuccessors(), nodes.get(2));
		
		//CFG heads and tails
		assertSetEquals(cfg.getHeads(), nodes.get(0), nodes.get(1));
		assertSetEquals(cfg.getTails(), nodes.get(2));
	}
	
	private void assertSetEmpty(Set<CfgNode> nodes) {
		assertSetEquals(nodes);
	}
	
	private void assertSetEquals(Set<CfgNode> nodes, CfgNode... nodeArgs) {
		Set<CfgNode> set = new LinkedHashSet<CfgNode>(Arrays.asList(nodeArgs));
		assertTrue(nodes.equals(set));
	}
	
}
