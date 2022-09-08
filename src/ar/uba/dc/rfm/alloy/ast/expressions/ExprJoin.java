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
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;

public final class ExprJoin extends AlloyExpression {

	public static ExprJoin join(AlloyVariable left, AlloyVariable right) {
		return join(buildExprVariable(left),buildExprVariable(right));
	}

	public static ExprJoin join(AlloyExpression left, AlloyVariable right) {
		return join(left,buildExprVariable(right));
	}
	
	public static ExprJoin join(AlloyExpression left, AlloyExpression right) {
		return new ExprJoin(left,right);
	}
	

	private final AlloyExpression left;

	private final AlloyExpression right;

	public ExprJoin(AlloyExpression left, AlloyExpression right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public Object accept(IExpressionVisitor v) {
		return v.visit(this);
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (obj.getClass().equals(ExprJoin.class))) {
			ExprJoin ej = (ExprJoin) obj;
			return getLeft().equals(ej.getLeft())
					&& getRight().equals(ej.getRight());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getLeft().hashCode() + getRight().hashCode();
	}

	@Override
	public String toString() {
		return "(" + getLeft().toString() + ").(" + getRight().toString() +")";
	}

	public AlloyExpression getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

}
