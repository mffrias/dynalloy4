package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;

public class BoundTests {

	@Test
	public void boundProjectionSimpleTest() {
		Bound b1 = Bound.buildBound("A");
		Bound b2 = Bound.buildBound("A", "B");
		
		Bound expected = Bound.buildBound("B");
		assertEquals(expected, b1.compose(b2));
	}
	
	@Test
	public void boundProjectionConcatenationTest() {
		Bound b1 = Bound.buildBound("A");
		Bound b2 = Bound.buildBound("A", "B");
		Bound b3 = Bound.buildBound("B", "C");
		Bound b4 = Bound.buildBound("C", "D");
		
		Bound expected = Bound.buildBound("D");
		assertEquals(expected, b1.compose(b2).compose(b3).compose(b4));
	}
	
	@Test
	public void boundCartesianProductTest() {
		Set<Bound> bounds1 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1"),
				Bound.buildBound("X1", "X2"),
				Bound.buildBound("A2")));
		
		Set<Bound> bounds2 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("B1"),
				Bound.buildBound("B2"),
				Bound.buildBound("Z1", "Z2")));

		//Two-dimensional bounds should be ignored
		Set<Bound> expected = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "B1"),
				Bound.buildBound("A1", "B2"),
				Bound.buildBound("A2", "B1"),
				Bound.buildBound("A2", "B2")));
		
		assertTrue(expected.equals(Bound.cartesianProduct(bounds1, bounds2)));
	}
	
	@Test
	public void boundOverrideWithLeftSingletonSetInProduct() {
		Set<Bound> bounds1 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "B1"),
				Bound.buildBound("A1", "B2"),
				Bound.buildBound("A2", "null"),
				Bound.buildBound("A2", "B3")));
		
		//This is the result of the product {A1}->{C1, C2},
		//and the set on the left ({A1}) is a singleton
		Set<Bound> bounds2 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "C1"),
				Bound.buildBound("A1", "C2")));

		//Since it is a singleton, we can be sure that the original bounds for A1 are overridden
		Set<Bound> expected = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "C1"),
				Bound.buildBound("A1", "C2"),
				Bound.buildBound("A2", "null"),
				Bound.buildBound("A2", "B3")));
		
		Set<Bound> result = Bound.override(bounds1, bounds2);
		assertTrue(expected.equals(result));
	}
	
	@Test
	public void boundOverrideWithLeftNonSingletonSetInProduct() {
		Set<Bound> bounds1 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "B1"),
				Bound.buildBound("A1", "B2"),
				Bound.buildBound("A2", "null")));
		
		//This is the result of the product {A1, A2}->{C1, C2},
		//and the set on the left ({A1, A2}) is not a singleton.
		Set<Bound> bounds2 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "C1"),
				Bound.buildBound("A1", "C2"),
				Bound.buildBound("A2", "C1"),
				Bound.buildBound("A2", "C2")));

		//This time it's not a singleton, so the original bounds for A1 and A2 are kept.
		Set<Bound> expected = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("A1", "B1"),
				Bound.buildBound("A1", "B2"),
				Bound.buildBound("A2", "null"),
				Bound.buildBound("A1", "C1"),
				Bound.buildBound("A1", "C2"),
				Bound.buildBound("A2", "C1"),
				Bound.buildBound("A2", "C2")));
		
		Set<Bound> result = Bound.override(bounds1, bounds2);
		assertTrue(expected.equals(result));
	}
	
	@Test
	public void boundOverrideBugTest() {
		Set<Bound> bounds1 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("L", "null"),
				Bound.buildBound("L", "N1"),
				Bound.buildBound("L", "N2")));
		
		Set<Bound> bounds2 = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("L", "null"),
				Bound.buildBound("L", "N1"),
				Bound.buildBound("L", "N2"),
				Bound.buildBound("L", "N3"),
				Bound.buildBound("L", "N4"),
				Bound.buildBound("L", "N5")));

		Set<Bound> expected = new HashSet<Bound>(Arrays.asList(
				Bound.buildBound("L", "null"),
				Bound.buildBound("L", "N1"),
				Bound.buildBound("L", "N2"),
				Bound.buildBound("L", "N3"),
				Bound.buildBound("L", "N4"),
				Bound.buildBound("L", "N5")));
		
		Set<Bound> result = Bound.override(bounds1, bounds2);
		assertTrue(expected.equals(result));
	}
	
	@Test
	public void intBoundMinAndMaxTest() {
		Bound.setBitwidth(1);
		assertEquals(-1, Bound.getMinIntBound());
		assertEquals(0, Bound.getMaxIntBound());
		
		Bound.setBitwidth(6);
		assertEquals(-32, Bound.getMinIntBound());
		assertEquals(31, Bound.getMaxIntBound());
	}
	
	@Test (expected = IllegalStateException.class)
	public void intBoundExpectedExceptionWithInvalidBound() {
		Bound.setBitwidth(3);
		// 2^3 = 8 => Valid range: [-4, 3]
		Bound.buildBound(4);
	}
	
	@Test
	public void intBoundModularArithmeticTest() {
		//We perform the tests for several different bitwidths
		for (int bitwidth = 2; bitwidth <= 10; bitwidth++) {
			Bound.setBitwidth(bitwidth);
			//Max + 1 = Min
			{
				Set<Bound> add1 = createBoundSet(Bound.buildBound(Bound.getMaxIntBound()));
				Set<Bound> add2 = createBoundSet(Bound.buildBound(1));
				Set<Bound> expected = createBoundSet(Bound.buildBound(Bound.getMinIntBound()));
				assertEquals(expected, Bound.add(add1, add2));
			}
			//Min - 1 = Max
			{
				Set<Bound> sub1 = createBoundSet(Bound.buildBound(Bound.getMinIntBound()));
				Set<Bound> sub2 = createBoundSet(Bound.buildBound(1));
				Set<Bound> expected = createBoundSet(Bound.buildBound(Bound.getMaxIntBound()));
				assertEquals(expected, Bound.sub(sub1, sub2));
			}
			//Max + Max = 2^(n-1)-1 + 2^(n-1)-1 = 2^n - 2 =(mod 2^n)= -2  
			{
				Set<Bound> add1 = createBoundSet(Bound.buildBound(Bound.getMaxIntBound()));
				Set<Bound> add2 = createBoundSet(Bound.buildBound(Bound.getMaxIntBound()));
				Set<Bound> expected = createBoundSet(Bound.buildBound(-2));
				assertEquals(expected, Bound.add(add1, add2));
			}
			//Min + Min = -2^(n-1) -2^(n-1) = -2^n =(mod 2^n)= 0  
			{
				Set<Bound> sub1 = createBoundSet(Bound.buildBound(Bound.getMinIntBound()));
				Set<Bound> sub2 = createBoundSet(Bound.buildBound(Bound.getMinIntBound()));
				Set<Bound> expected = createBoundSet(Bound.buildBound(0));
				assertEquals(expected, Bound.add(sub1, sub2));
			}
		}
	}
	
	@Test
	public void intBoundNegateTest() {
		for (int bitwidth = 2; bitwidth <= 10; bitwidth++) {
			Bound.setBitwidth(bitwidth);
			//The MinInt bound is not affected by negation
			{
				Set<Bound> bounds = createBoundSet(Bound.buildBound(Bound.getMinIntBound()));
				assertEquals(bounds, Bound.negate(bounds));
			}
			//All the rest of the bounds are negated
			for (int b = Bound.getMinIntBound() + 1; b <= Bound.getMaxIntBound(); b++) {
				Set<Bound> bounds = createBoundSet(Bound.buildBound(b));
				Set<Bound> expected = createBoundSet(Bound.buildBound(-b));
				assertEquals(expected, Bound.negate(bounds));
			}
		}		
	}
	
	private Set<Bound> createBoundSet(Bound... bounds) {
		return new LinkedHashSet<Bound>(Arrays.asList(bounds));
	}
	
}
	