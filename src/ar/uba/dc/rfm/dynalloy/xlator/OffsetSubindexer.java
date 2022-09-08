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

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

final class OffsetSubindexer extends VarSubstitutor {

	@Override
	protected AlloyExpression getExpr(AlloyVariable v) {
		if (!v.isMutable())
			return new ExprVariable(v);
		
		VariableId varId = v.getVariableId();
		if (offsetMap.containsKey(varId)) {
			int offset = offsetMap.get(varId);
			AlloyVariable offseted_v = new AlloyVariable(varId, v.getIndex()+offset); 
			return new ExprVariable(offseted_v);
		} else
			return null;
	}

	private Map<VariableId, Integer> offsetMap;

	public OffsetSubindexer(Map<VariableId, Integer> _reindexMap) {
		offsetMap = _reindexMap;
	}

}
