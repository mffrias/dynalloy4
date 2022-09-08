package ar.uba.dc.rfm.dynalloy.dataflow;

public class BoundOneDimension extends Bound {
	
	protected BoundOneDimension(String bound) {
		super(bound);
	}
	
	@Override
	public Bound compose(Bound b) {
		// A one-dimensional bound can only be composed with a two-dimensional one,
		// and the result is a one-dimensional bound consisting of the second component
		// of the two-dimensional (e.g. 'A' compose 'A->B' = 'B') 
		if (!(b instanceof BoundTwoDimensions))
			return null;
		
		BoundTwoDimensions b2 = (BoundTwoDimensions)b;
		if (this.equals(b2.getFirstComponent()))
			return b2.getSecondComponent();
		else
			return null;
	}	
}
