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
package ar.uba.dc.rfm.alloy;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * Represents a formula variable.
 * 
 * @author {jgaleotti, nmaur}
 */
public final class AlloyVariable {
	private VariableId variableId;

	private int index;

	private boolean isMutable = true;

	private boolean comesFromContract = false;

	
	
	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	private static final int PLAIN_INDEX = -1;

	private static final int PRIME_INDEX = -2;
	
	private static final int PRE_STATE_INDEX = -3;
	
	
    public boolean isVariableFromContract(){
    	return comesFromContract;
    }
    
    public void setIsVariableFromContract(){
    	comesFromContract = true;
    }


	public static AlloyVariable buildAlloyVariable(String _variableId) {
		return new AlloyVariable(_variableId);
	}
	
	public static List<AlloyVariable> asAlloyVariable(List<VariableId> vs) {
		List<AlloyVariable> result = new LinkedList<AlloyVariable>();
		for (VariableId v : vs) 
			result.add(new AlloyVariable(v));
		
		return result;
	}
		
	public static AlloyVariable buildNonMutableAlloyVariable(String _variableId){
		AlloyVariable av = new AlloyVariable(_variableId);
		av.setMutable(false);
		return av;
		
	}
	
	public AlloyVariable(String _variableId) {
		this(_variableId, PLAIN_INDEX);
	}

	public AlloyVariable(VariableId _variableId) {
		this(_variableId, PLAIN_INDEX);
	}
	
	public AlloyVariable(String _variableId, boolean _isPrimed) {
		this(_variableId, _isPrimed ? PRIME_INDEX : PLAIN_INDEX);
	}

	public AlloyVariable(VariableId _variableId, boolean _isPrimed) {
		this(_variableId, _isPrimed ? PRIME_INDEX : PLAIN_INDEX);
	}

	public static AlloyVariable buildPreStateVariable(String varId) {
		return new AlloyVariable(varId, PRE_STATE_INDEX);
	}
	
	public AlloyVariable(VariableId _variableId, int _index) {
		if (_index < PRE_STATE_INDEX)
			throw new RuntimeException("Invalid index value: " + _index);

		this.variableId = _variableId;
		this.index = _index;
	}
	
	public AlloyVariable(String _variableId, int _index) {
		this(new VariableId(_variableId), _index);
	}

	/**
	 * Returns the name that identifies the variable.
	 * 
	 * Example: variable getName("s_3") = "s" variable getName("s'") = "s"
	 * variable getName("s") = "s"
	 * 
	 * @return a string that represents the variable name.
	 */
	public VariableId getVariableId() {
		return variableId;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null) {
			if (arg0.getClass().equals(AlloyVariable.class)) {
				AlloyVariable o = (AlloyVariable) arg0;
				return o.getVariableId().equals(this.getVariableId())
						&& o.getIndex() == this.getIndex();
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return variableId.hashCode() + index;
	}

	public boolean isPlain() {
		return getIndex() == PLAIN_INDEX;
	}

	public boolean isPrimed() {
		return getIndex() == PRIME_INDEX;
	}

	@Override
	public String toString() {
		if (getIndex() == PLAIN_INDEX)
			return getVariableId().toString();
		else if (getIndex() == PRIME_INDEX)
			return getVariableId().toString() + "'";
		else if (getIndex() == PRE_STATE_INDEX)
			return "\\pre[" + getVariableId().toString() + "]";
		else
			return getVariableId().toString() + "_" + getIndex();
	}

	public boolean isPreStateVar() {
		return getIndex() == PRE_STATE_INDEX;
	}

	
	public boolean isMutable() {
		return isMutable;
	}
	
	public void setMutable(boolean isMutable) {
		this.isMutable = isMutable;
	}
	
}