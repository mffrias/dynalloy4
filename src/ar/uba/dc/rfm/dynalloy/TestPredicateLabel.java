package ar.uba.dc.rfm.dynalloy;

public final class TestPredicateLabel {

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && obj.getClass().equals(TestPredicateLabel.class)) {
			TestPredicateLabel that = (TestPredicateLabel) obj;
			return this.isLblpos == that.isLblpos
					&& this.label_id.equals(that.label_id);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return (new Boolean(isLblpos)).hashCode() + label_id.hashCode();
	}

	@Override
	public String toString() {
		return (isLblpos ? "lblpos" : "lblneg") + " " + label_id;
	}

	private final boolean isLblpos;
	private final String label_id;

	public TestPredicateLabel(boolean isLblpos, String label_id) {
		this.isLblpos = isLblpos;
		this.label_id = label_id;
	}

	public boolean isLblpos() {
		return isLblpos;
	}

	public boolean isLblneg() {
		return !isLblpos;
	}

	public String getLabelId() {
		return label_id;
	}

}
