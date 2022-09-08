package ar.uba.dc.rfm.dynalloy.dataflow;

public class BoundTwoDimensions extends Bound {

	private BoundOneDimension firstBound;
	private BoundOneDimension secondBound;
	
	protected BoundTwoDimensions(BoundOneDimension firstDimensionBound, BoundOneDimension secondDimensionBound) {
		super(String.format("%s->%s", firstDimensionBound, secondDimensionBound));
		firstBound = firstDimensionBound;
		secondBound = secondDimensionBound;
	}
	
	public BoundOneDimension getFirstComponent() {
		return firstBound;
	}
	
	public BoundOneDimension getSecondComponent() {
		return secondBound;
	}

	@Override
	public Bound compose(Bound b) {
		// A two dimensional bound (e.g. A->B), cannot be the first operand in a composition, only
		// the second (i.e. (A->B).A is not valid, but A.(A->B) is, and returns B)
		return null;
	}
}
