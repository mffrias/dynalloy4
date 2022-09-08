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

public final class ExprConstant extends AlloyExpression {

	private final String aliasModuleId;
	private final String constantId;
	@Override
	public Object accept(IExpressionVisitor v) {
	    return v.visit(this);
	}
	
	public static ExprConstant buildExprConstant(String _constantId) {
		return new ExprConstant(null, _constantId);
	}

	public ExprConstant(String _aliasModuleId, String _constantId) {
		super();
		aliasModuleId = _aliasModuleId;
		constantId = _constantId;
	}

	public String getConstantId() {
	    return constantId;
	}

	@Override
	public String toString() {
		if (aliasModuleId == null) {
		    return getConstantId();
		} else {
		    return aliasModuleId + "/" + getConstantId();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasModuleId == null) ? 0 : aliasModuleId.hashCode());
		result = prime * result
				+ ((constantId == null) ? 0 : constantId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ExprConstant other = (ExprConstant) obj;
		if (aliasModuleId == null) {
			if (other.aliasModuleId != null)
				return false;
		} else if (!aliasModuleId.equals(other.aliasModuleId))
			return false;
		if (constantId == null) {
			if (other.constantId != null)
				return false;
		} else if (!constantId.equals(other.constantId))
			return false;
		return true;
	}

}
