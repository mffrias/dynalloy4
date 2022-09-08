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
package ar.uba.dc.rfm.dynalloy.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;

public class DynalloyProgramParseContext implements
		IDynalloyProgramParseContext {

	private final Map<String, AlloyVariable> vars;

	public DynalloyProgramParseContext(Set<AlloyVariable> ctxVariables,
			Set<AlloyVariable> ctxFields, boolean allowPrimed) {
		this.vars = new HashMap<String, AlloyVariable>();
		Set<AlloyVariable> union = new HashSet<AlloyVariable>();
		union.addAll(ctxVariables);
		union.addAll(ctxFields);
		for (AlloyVariable v : union) {
			this.vars.put(v.toString(), v);
			if (allowPrimed) {
				AlloyVariable vPrimed = new AlloyVariable(v.getVariableId(),
						true);
				this.vars.put(vPrimed.toString(), vPrimed);
			}
		}
	}

	public AlloyVariable getAlloyVariable(String token) {
		return vars.get(token);
	}

	public boolean isVariableName(String token) {
		return vars.containsKey(token);
	}

	public int getIntLiteral(String token) {
		return Integer.parseInt(token);
	}

	public boolean isIntLiteral(String token) {
		try {
			Integer.parseInt(token);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
