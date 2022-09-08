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

public final class ExprUnion extends AlloyExpression {

	private final AlloyExpression left;
	private final AlloyExpression right;

	public static AlloyExpression buildExprUnion(AlloyExpression... exprs) {
		ExprUnion result = new ExprUnion(exprs[0],exprs[1]);
		for (int i = 2; i < exprs.length; i++) {
			result = new ExprUnion(result, exprs[i]);
		}
		return result;
	}
	
	public ExprUnion(AlloyExpression left, AlloyExpression right) {
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
		if ((obj != null) && (obj.getClass().equals(ExprUnion.class))) {
			ExprUnion eu = (ExprUnion) obj;
			return getLeft().equals(eu.getLeft())
					&& getRight().equals(eu.getRight());
		} else
			return false;
	}

	public AlloyExpression getLeft() {
		return left;
	}

	public AlloyExpression getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return getLeft().hashCode() + getRight().hashCode();
	}

	@Override
	public String toString() {
		return "(" + getLeft().toString() + ")+(" + getRight().toString() +")";
	}

}
