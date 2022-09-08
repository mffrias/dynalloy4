package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public class BoundedVariableTests {
	
	@Test
	public void BoundedVariableNameWithUnderscoreTest() {
		BoundedVariable bv = new BoundedVariable("l0_varname");
		assertEquals(bv.getVariable(), new ExprVariable(new AlloyVariable("l0_varname")));
	}
	
	@Test
	public void BoundedVariableNameWithMultipleUnderscoreTest() {
		BoundedVariable bv = new BoundedVariable("l0_l1_l2_varname");
		assertEquals(bv.getVariable(), new ExprVariable(new AlloyVariable("l0_l1_l2_varname")));
	}
	
	@Test
	public void BoundedVariableNameWithIndexTest() {
		BoundedVariable bv = new BoundedVariable("varname_3");
		assertEquals(bv.getVariable(), new ExprVariable(new AlloyVariable("varname", 3)));
	}
	
	@Test
	public void BoundedVariableNameWithUnderscoreAndIndexTest() {
		BoundedVariable bv = new BoundedVariable("l0_varname_0");
		assertEquals(bv.getVariable(), new ExprVariable(new AlloyVariable("l0_varname", 0)));
	}
}
