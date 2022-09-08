package ar.uba.dc.rfm.dynalloy.dataflow;

public class BoundOneDimensionInt extends BoundOneDimension {

	private int intBound;
	
	protected BoundOneDimensionInt(int bound) {
		super(Integer.toString(bound));
		intBound = bound;
	}

	@Override
	public String toString() {
		return String.format("Int[%s]", getBound());
	}
	
	public int getIntBound()  {
		return intBound;
	}
}
