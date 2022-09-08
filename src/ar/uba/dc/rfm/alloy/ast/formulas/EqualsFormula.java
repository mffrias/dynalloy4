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
package ar.uba.dc.rfm.alloy.ast.formulas;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;

public final class EqualsFormula extends AlloyFormula {

	private final AlloyExpression left;

	private final AlloyExpression right;

	public EqualsFormula(AlloyExpression left, AlloyExpression right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public Object accept(IFormulaVisitor visitor) {
		return visitor.visit(this);
	}

	public AlloyExpression getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(EqualsFormula.class)) {
			EqualsFormula that = (EqualsFormula) arg0;
			return this.getLeft().equals(that.getLeft())
					&& this.getRight().equals(that.getRight());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getLeft().hashCode() + getRight().hashCode();
	}

	@Override
	public String toString() {
		return getLeft().toString() + "=" + getRight().toString();
	}

}
