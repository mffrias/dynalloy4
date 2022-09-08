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

public class ArgAppenderExpressionMutator extends ExpressionMutator {

	private HashSet<AlloyVariable> varsToPrefix = new HashSet<AlloyVariable>();
	

	
	public ArgAppenderExpressionMutator(HashSet<AlloyVariable> varsToPrefix){
		this.varsToPrefix = varsToPrefix;
	}
	
	
	public Object visit(ExprVariable ev){
		for (AlloyVariable av : this.varsToPrefix){
			if (ev.getVariable().getVariableId().getString().equals(av.getVariableId().getString())){
				return new ExprVariable(
						new AlloyVariable("arg_"+ev.getVariable().getVariableId().getString(),ev.getVariable().isPrimed()));
			}	
		}
		return ev;
			
	}

}
