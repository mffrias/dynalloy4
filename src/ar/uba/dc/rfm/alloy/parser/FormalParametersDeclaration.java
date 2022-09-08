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
package ar.uba.dc.rfm.alloy.parser;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;

public class FormalParametersDeclaration {
	AlloyTyping typing;
	List<VariableId> parameters;
	
	public FormalParametersDeclaration() {
		typing = new AlloyTyping();
		parameters = new LinkedList<VariableId>();
	}
	
	public List<VariableId> getParameters() {
		return parameters;
	}
	
	public AlloyTyping getTyping() {
		return typing;
	}
	
	public void put(AlloyVariable v, String t){
		typing.put(v, t.trim());
		parameters.add(v.getVariableId());
	}


}
