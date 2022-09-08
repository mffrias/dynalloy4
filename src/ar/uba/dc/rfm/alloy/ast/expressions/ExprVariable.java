/*
 * Dynalloy Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.alloy.ast.expressions;


import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;


/**
 * @author jgaleotti
 * 
 */
public class ExprVariable extends AlloyExpression {

	public static ExprVariable buildExprVariable(String variableId) {
		AlloyVariable av = new AlloyVariable(variableId, false);
		av.setMutable(true);
		return new ExprVariable(av);
	}
	
	public static ExprVariable buildNonMutableExprVariable(String variableId) {
		AlloyVariable av = AlloyVariable.buildNonMutableAlloyVariable(variableId);
		return new ExprVariable(av);
	}
	
	
	public static ExprVariable buildExprVariable(AlloyVariable v) {
		return new ExprVariable(v);
	}
	
	@Override
    public Object accept(IExpressionVisitor v) {
        return v.visit(this);
    }

	
    private final AlloyVariable var;

    private boolean comesFromContract = false;
    
    public boolean isVariableFromContract(){
    	return var.isVariableFromContract();
    }
    
    public void setIsVariableFromContract(){
    	var.setIsVariableFromContract();
    }
    
    public ExprVariable(AlloyVariable _var) {
        var = _var;
    }

    public AlloyVariable getVariable() {
        return var;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(ExprVariable.class))) {
            ExprVariable vc = (ExprVariable) obj;
            return getVariable().equals(vc.getVariable());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return getVariable().hashCode();
    }

	@Override
	public String toString() {
		return getVariable().toString();
	}

}
