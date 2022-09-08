package ar.uba.dc.rfm.alloy.ast.expressions;


public class ThizVarCollectorVisitor extends ExpressionVisitor {

	boolean result;

	public ThizVarCollectorVisitor() {
	}

	public boolean getResult(){
		return result;
	}

	public Object visit(ExprVariable n) {
		result = result || n.getVariable().getVariableId().getString().equals("thiz");
		return null;
	}

}
