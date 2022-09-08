package ar.uba.dc.rfm.alloy.ast.formulas;

import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public class PredicateCallAlloyFormula extends AlloyFormula implements IProgramCall {

	private final String programId;
	private final List<AlloyExpression> arguments;
	private final boolean isSuperCall;
	private final boolean isStatic;

	public PredicateCallAlloyFormula(boolean isSuper, String programId, List<AlloyExpression> arguments, boolean isStatic) {
		super();
		this.isSuperCall = isSuper;
		this.programId = programId;
		this.arguments = arguments;
		this.isStatic = isStatic;
	}

	@Override
	public Object accept(IFormulaVisitor visitor) {
		if (!(visitor instanceof IJDynAlloyFormulaVisitor))
			throw new IllegalArgumentException(this.getClass().getName() + " is not supposed to be called using this kind of visitor: "
					+ visitor.getClass().getName());
		else {
			IJDynAlloyFormulaVisitor dynjalloyFormulaVisitor = (IJDynAlloyFormulaVisitor) visitor;
			return dynjalloyFormulaVisitor.visit(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#getProgramId()
	 */
	public String getProgramId() {
		return programId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#getArguments()
	 */
	public List<AlloyExpression> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (AlloyExpression e : getArguments()) {
			if (buffer.length() > 0)
				buffer.append(",");
			buffer.append(e.toString());
		}
		return (this.isSuperCall ? "super " : "") + (this.isStatic ? "staticCallSpec " : "callSpec ")  + getProgramId() + "[" + buffer.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result + (isSuperCall ? 1231 : 1237);
		result = prime * result + ((programId == null) ? 0 : programId.hashCode());
		result = prime * result + (isStatic ? 1345 : 1456);
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
		final PredicateCallAlloyFormula other = (PredicateCallAlloyFormula) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (isSuperCall != other.isSuperCall)
			return false;
		if (isStatic != other.isStatic)
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#isSuperCall()
	 */
	public boolean isSuperCall() {
		return isSuperCall;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ar.edu.dynjml4alloy.ast.ICallProgram#isStatic()
	 */
	public boolean isStatic() {
		return this.isStatic;
	}
}
