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
package ar.uba.dc.rfm.dynalloy.xlator;

import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

/**
 * Substitutes variables for expressions using a (incomplete) mapping
 * AlloyVariable -> AlloyExpression
 * 
 * @author jgaleotti
 * 
 */
public final class FormalVariableSubstitutor extends VarSubstitutor {

	protected AlloyExpression getExpr(AlloyVariable variable) {
		if (map.get(variable.getVariableId()) == null)
			return null; //do nothing
		else if (variable.isPrimed()) {
			if (map.get(variable.getVariableId()).getClass().equals(
					ExprVariable.class)) {
				AlloyVariable new_value = ((ExprVariable) map.get(variable
						.getVariableId())).getVariable();
				return new ExprVariable(new AlloyVariable(new_value
						.getVariableId(), true));
			} else
				throw new RuntimeException(
						"The left-hand side of an update must be a variable");
		} else
			return map.get(variable.getVariableId());

	}

	private final Map<VariableId, AlloyExpression> map;

	private final Set<AlloyVariable> fields;

	public FormalVariableSubstitutor(Map<VariableId, AlloyExpression> _map,
			Set<AlloyVariable> _fields) {
		map = _map;
		fields = _fields;
	}

	@Override
	protected void doWhenVarNotFound(ExprVariable n) {
		if (!fields.contains(n.getVariable()))
			throw new RuntimeException(
					"no actual parameter for formal parameter: "
							+ n.getVariable());
	}

}
