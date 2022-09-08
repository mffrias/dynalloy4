package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Bound implements Comparable {
	
	protected String bound;
	protected static int bitwidth = 4;
	
	protected Bound(String bound) {
		this.bound = bound;
	}
	
	public String getBound() {
		return bound;
	}
	
	/**
	 * If this is a Bound of the form 'A' and the composing bound of the form 'A->B', returns B.
	 * If the bounds are incompatible in any way, returns NULL.
	 */
	public abstract Bound compose(Bound projector);
	
	@Override
	public String toString() {
		return bound;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bound == null) ? 0 : bound.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bound other = (Bound) obj;
		if (bound == null) {
			if (other.bound != null)
				return false;
		} else if (!bound.equals(other.bound))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(Object arg0) {
		Integer i = this.hashCode();
		return i.compareTo(arg0.hashCode());
	}

	public static Bound buildBound(String oneDimensionalBound) {
		return new BoundOneDimension(oneDimensionalBound);
	}
	
	public static Bound buildBound(int oneDimensionIntBound) {
		if (oneDimensionIntBound < getMinIntBound() || oneDimensionIntBound > getMaxIntBound())
			throw new IllegalStateException(String.format("Int bound %d is outside the range [%d, %d] allowed for bitwidth %d", oneDimensionIntBound, getMinIntBound(), getMaxIntBound(), bitwidth));
		
		return new BoundOneDimensionInt(oneDimensionIntBound);
	}
	
	public static Bound buildBound(String firstDimension, String secondDimension) {
		return new BoundTwoDimensions(new BoundOneDimension(firstDimension), new BoundOneDimension(secondDimension));
	}
	
	public static Bound buildBound(String firstDimension, int secondDimensionIntBound) {
		return new BoundTwoDimensions(new BoundOneDimension(firstDimension), new BoundOneDimensionInt(secondDimensionIntBound));
	}
	
	public static void setBitwidth(int bitwidth) {
		//Valid range of int bounds: [-2^(n-1), 2^(n-1)-1]
		Bound.bitwidth = bitwidth;
	}
	
	public static int getMinIntBound() {
		return -1 * (int)Math.pow(2, bitwidth - 1);
	}
	
	public static int getMaxIntBound() {
		return (int)Math.pow(2, bitwidth - 1) - 1;
	}
	
	public static Set<Bound> compose(Set<Bound> leftBounds, Set<Bound> rightBounds) {
		Set<Bound> composition = new LinkedHashSet<Bound>();

		for (Bound l : leftBounds) {
			for (Bound r : rightBounds) {
				Bound composed = l.compose(r);
				if (composed != null)
					composition.add(composed);
			}
		}
		
		return composition;
	}

	public static Set<Bound> cartesianProduct(Set<Bound> leftBounds, Set<Bound> rightBounds) {
		Set<Bound> product = new LinkedHashSet<Bound>();
		
		for (Bound l : leftBounds) {
			//We don't allow nulls in the first component, which wouldn't make any sense
			if (l.getBound().equals("null"))
				continue;
			
			if (l instanceof BoundOneDimension) {
				for (Bound r : rightBounds) {
					if (r instanceof BoundOneDimension) {
						BoundOneDimension oneLeft = (BoundOneDimension)l;
						BoundOneDimension oneRight = (BoundOneDimension)r;
						product.add(new BoundTwoDimensions(oneLeft, oneRight));
					}
				}
			}
		}
		
		return product;
	}

	public static Set<Bound> override(Set<Bound> leftBounds, Set<Bound> rightBounds) {
		Set<Bound> result = new LinkedHashSet<Bound>();
		Set<BoundTwoDimensions> twoDimensionalLeftBounds = new LinkedHashSet<BoundTwoDimensions>();
		Set<BoundTwoDimensions> twoDimensionalRightBounds = new LinkedHashSet<BoundTwoDimensions>();
		Set<BoundOneDimension> rightFirstComponents = new LinkedHashSet<BoundOneDimension>();
		Set<BoundOneDimension> leftFirstComponents = new LinkedHashSet<BoundOneDimension>();
		
		for (Bound r : rightBounds) {
			if (r instanceof BoundTwoDimensions) {
				BoundTwoDimensions right = (BoundTwoDimensions)r;
				twoDimensionalRightBounds.add(right);
				rightFirstComponents.add(right.getFirstComponent());
			}
		}
		
		for (Bound l : leftBounds) {
			if (l instanceof BoundTwoDimensions) {
				BoundTwoDimensions left = (BoundTwoDimensions)l;
				twoDimensionalLeftBounds.add(left);
				leftFirstComponents.add(left.getFirstComponent());
			}
		}
		
		if (rightFirstComponents.size() == 1) {
			//Keep the left bounds that are not overridden
			for (BoundTwoDimensions left : twoDimensionalLeftBounds) {
				if (!rightFirstComponents.contains(left.getFirstComponent()))
					result.add(left);
			}
		}
		else {
			//This time, we have to keep all the left bounds...
			for (BoundTwoDimensions left : twoDimensionalLeftBounds) {
				result.add(left);
			}
		}
		
		//Keep all the right bounds
		for (BoundTwoDimensions right : twoDimensionalRightBounds) {
			result.add(right);
		}
		
		return result;
	}
	
	private interface IntBoundOperation{
        int calculate(int a, int b);
    }
	
	public static Set<Bound> negate(Set<Bound> bounds) {
		Set<Bound> negatedBounds = new LinkedHashSet<Bound>();
		
		for (Bound b : bounds) {
			if (!(b instanceof BoundOneDimensionInt))
				throw new IllegalArgumentException("Onedimensional int bound expected");
		
			int intBound = ((BoundOneDimensionInt)b).getIntBound();
			//The min bound has no positive counterpart, so the result is itself
			if (intBound == Bound.getMinIntBound())
				negatedBounds.add(Bound.buildBound(intBound));
			else
				negatedBounds.add(Bound.buildBound(-intBound));
		}
		
		return negatedBounds;
	}
	
	public static Set<Bound> add(Set<Bound> leftBounds, Set<Bound> rightBounds) {
		IntBoundOperation add = new IntBoundOperation(){
            public int calculate(int a, int b) {
                return a + b;
            }
        };
    
        return performIntOperation(leftBounds, rightBounds, add);
	}
	
	public static Set<Bound> sub(Set<Bound> leftBounds, Set<Bound> rightBounds) {
		IntBoundOperation sub = new IntBoundOperation(){
            public int calculate(int a, int b) {
                return a - b;
            }
        };
    
        return performIntOperation(leftBounds, rightBounds, sub);
	}
	
	private static Set<Bound> performIntOperation(Set<Bound> leftBounds, Set<Bound> rightBounds, IntBoundOperation operator) {
		Set<Bound> result = new LinkedHashSet<Bound>();
		
		for (Bound l : leftBounds) {
			if (!(l instanceof BoundOneDimensionInt))
				throw new IllegalArgumentException("Onedimensional int bound expected");
			for (Bound r : rightBounds) {
				if (!(r instanceof BoundOneDimensionInt))
					throw new IllegalArgumentException("Onedimensional int bound expected");
				int leftInt = ((BoundOneDimensionInt)l).getIntBound();
				int rightInt = ((BoundOneDimensionInt)r).getIntBound();
				int tempResult = operator.calculate(leftInt, rightInt);
				//We are working with modular arithmetic, so we have to fix the result:
				//First, since we are working in the [-2^(n-1), 2^(n-1) -1], we get to
				//the [0, 2^n -1] range, in order to use the modulo operator
				int resZeroBased = tempResult + (int)Math.pow(2, bitwidth - 1);
				//Now we can use the modulo operator
				int fixedResZeroBased = resZeroBased % (int)Math.pow(2, bitwidth);
				//We have to be careful here: if we had a negative value, Java % will return
				//a negative remainder, so we get the positive one
				if (fixedResZeroBased < 0)
					fixedResZeroBased += (int)Math.pow(2, bitwidth);
				//Last, we get the result back to the original range where we need it
				int finalRes = fixedResZeroBased - (int)Math.pow(2, bitwidth - 1);
				result.add(Bound.buildBound(finalRes));
			}
		}
		
		return result;
	}
	
}
