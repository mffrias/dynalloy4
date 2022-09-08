package ar.uba.dc.rfm.alloy.ast.formulas;


public final class IffFormula extends AlloyFormula {

	public AlloyFormula getLeft() {
		return left;
	}

	public AlloyFormula getRight() {
		return right;
	}

	public IffFormula(AlloyFormula left, AlloyFormula right) {
		super();
		this.left = left;
		this.right = right;
	}

	private final AlloyFormula left;
	
	private final AlloyFormula right;
	
	@Override
	public Object accept(IFormulaVisitor visitor) {
		if (!(visitor instanceof IJDynAlloyFormulaVisitor))
			throw new IllegalArgumentException(
					this.getClass().getName()
							+ " is not supposed to be called using this kind of visitor: "
							+ visitor.getClass().getName());
		else {
			IJDynAlloyFormulaVisitor dynjalloyFormulaVisitor = (IJDynAlloyFormulaVisitor) visitor;
			return dynjalloyFormulaVisitor.visit(this);
		}
	}

}
