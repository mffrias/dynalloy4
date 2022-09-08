package ar.uba.dc.rfm.alloy.ast.expressions;

import java.util.LinkedList;
import java.util.List;

public final class ExprFunction extends AlloyExpression {

	private final String aliasModuleId;
	private final List<AlloyExpression> parameters;
	private final String functionId;
	private String returnType;

	public ExprFunction(String aliasModuleId, String functionId,
			List<AlloyExpression> parameters) {
		super();
		this.aliasModuleId = aliasModuleId;
		this.functionId = functionId;
		this.parameters = parameters;
	}

	public String getReturnType(){
		return this.returnType;
	}
	
	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	public static ExprFunction buildExprFunction(String functionId,
			AlloyExpression... ps) {
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (int i = 0; i < ps.length; i++)
			l.add(ps[i]);
		return new ExprFunction(null, functionId, l);
	}
	
	
	

	public List<AlloyExpression> getParameters() {
		return parameters;
	}

	public String getFunctionId() {
		return functionId;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (AlloyExpression e : this.parameters) {
			if (buffer.length() > 0)
				buffer.append(",");
			buffer.append(e.toString());
		}
		return (aliasModuleId==null?"null":aliasModuleId) + "/" + getFunctionId() + "[" + buffer.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasModuleId == null) ? 0 : aliasModuleId.hashCode());
		result = prime * result
				+ ((functionId == null) ? 0 : functionId.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
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
		final ExprFunction other = (ExprFunction) obj;
		if (aliasModuleId == null) {
			if (other.aliasModuleId != null)
				return false;
		} else if (!aliasModuleId.equals(other.aliasModuleId))
			return false;
		if (functionId == null) {
			if (other.functionId != null)
				return false;
		} else if (!functionId.equals(other.functionId))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	public static AlloyExpression buildExprFunction(String functionId, AlloyExpression[] params, String cType) {
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (int i = 0; i < params.length; i++)
			l.add(params[i]);
		ExprFunction retFun = new ExprFunction(null, functionId, l);
		retFun.returnType = cType;
		return retFun;
	}

}
