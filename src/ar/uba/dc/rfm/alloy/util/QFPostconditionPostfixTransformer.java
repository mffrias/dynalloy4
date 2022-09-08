package ar.uba.dc.rfm.alloy.util;

import java.util.HashMap;
import java.util.HashSet;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;

public class QFPostconditionPostfixTransformer extends ExpressionMutator {

	private HashSet<AlloyVariable> varsToPrefix = new HashSet<AlloyVariable>();
	

	
	public QFPostconditionPostfixTransformer(HashSet<AlloyVariable> varsToPrefix){
		this.varsToPrefix = varsToPrefix;
	}
	
	
	public Object visit(ExprVariable ev){
		
		String varName = ev.getVariable().getVariableId().getString();
		if (varName.startsWith("SK_jml_pred_java_primitive_") && ev.getVariable().isVariableFromContract())
			varName += "_1";
		for (AlloyVariable av : this.varsToPrefix){
			if (varName.equals(av.getVariableId().getString())){
				AlloyVariable newAV = new AlloyVariable(varName, true);
				newAV.setIsVariableFromContract();
				ExprVariable newEV = new ExprVariable(newAV);
//				ExprConstant ec = new ExprConstant(null, "QF");
//				return new ExprJoin(ec, newEV);
				return newEV;
			}	
		}
		return ev;
			
	}

}
