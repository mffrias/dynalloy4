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

public class QFtransformer extends ExpressionMutator {

	private HashSet<AlloyVariable> varsToPrefix = new HashSet<AlloyVariable>();
	

	
	public QFtransformer(HashSet<AlloyVariable> varsToPrefix){
		this.varsToPrefix = varsToPrefix;
	}
	
	
	public Object visit(ExprVariable ev){
		for (AlloyVariable av : this.varsToPrefix){
			if (ev.getVariable().equals(av)){
				ExprConstant ec = new ExprConstant(null, "QF");
				return new ExprJoin(ec, ev);
			}	
		}
		return ev;
			
	}

}
