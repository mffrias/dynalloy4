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
package ar.uba.dc.rfm.alloy.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprComprehension;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntLiteral;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprSum;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnary;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprFunction;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprJoin;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprOverride;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprProduct;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprUnion;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

public class ExpressionPrinter extends ExpressionVisitor {

	@Override
	public Object visit(ExprComprehension n) {

		Vector<Object> v = (Vector<Object>) super.visit(n);

		Vector<String> setOf_results = (Vector<String>) v.get(0);
		String formula_str = (String) v.get(1);

		List<String> nameDecls = new LinkedList<String>();
		for (int i = 0; i < n.getNames().size() ; i++) {
			String name_str = n.getNames().get(i);
			String setOf_str = (String) setOf_results.get(i);
			String nameDecl = String.format("%s:%s", name_str, setOf_str);
			nameDecls.add(nameDecl);
		}

		StringBuffer buff = new StringBuffer();
		buff.append("{");
		for (int i = 0; i < nameDecls.size(); i++) {
			String nameDecl = nameDecls.get(i);
			if (i != 0)
				buff.append(",");

			buff.append(nameDecl);
		}
		buff.append("|");
		buff.append(formula_str);
		buff.append("}");
		return buff.toString();
	}

	@Override
	public Object visit(ExprIfCondition n) {
		Vector<String> vector = (Vector<String>) super.visit(n);
		String conditionStr = vector.get(0);
		String leftStr = vector.get(1);
		String rightStr = vector.get(2);

		StringBuffer buff = new StringBuffer();
		buff.append(String.format("(%s=>(%s)else(%s))", conditionStr, leftStr,
				rightStr));
		return buff.toString();
	}

	@Override
	public Object visit(ExprIntersection n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		String left = v.get(0);
		String right = v.get(1);
		String result = String.format("((%s) & (%s))", left, right);
		return result;
	}

	@Override
	public Object visit(ExprSum n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);

		Vector<String> setOf_strings = (Vector<String>) v.get(0);
		String expression_str = (String) v.get(1);

		List<String> nameDecls = new LinkedList<String>();
		for (int i = 0; i < n.getNames().size(); i++) {
			String name = n.getNames().get(i);
			String setOfStr = setOf_strings.get(i);
			String nameDecl = String.format("%s:%s", name, setOfStr);
			nameDecls.add(nameDecl);
		}

		StringBuffer buff = new StringBuffer();
		buff.append("(sum ");
		for (int i = 0; i < nameDecls.size(); i++) {
			String nameDecl = nameDecls.get(i);
			if (i != 0)
				buff.append(",");

			buff.append(nameDecl);

			if (i == nameDecls.size() - 1) {
				buff.append("|");
			}
		}
		buff.append(expression_str);
		buff.append(")");
		return buff.toString();
	}

	@Override
	public Object visit(ExprConstant n) {
		return n.getConstantId();
	}

	@Override
	public Object visit(ExprJoin n) {
		Vector<String> v = (Vector<String>) super.visit(n);

		StringBuffer buff = new StringBuffer();
		if (!hasNoChild(n.getLeft())) {
			buff.append(wrap(v.get(0)));
		} else
			buff.append(v.get(0));

		buff.append(".");

		if (!hasNoChild(n.getRight())) {
			buff.append(wrap(v.get(1)));
		} else
			buff.append(v.get(1));

		return buff.toString();
	}

	private boolean hasNoChild(AlloyExpression n) {
		return n.getClass().equals(ExprVariable.class)
				|| n.getClass().equals(ExprConstant.class);
	}

	@Override
	public Object visit(ExprUnion n) {
		Vector<String> v = (Vector<String>) super.visit(n);

		StringBuffer buff = new StringBuffer();
		if (n.getLeft().getClass().equals(ExprJoin.class)) {
			buff.append(wrap(v.get(0)));
		} else
			buff.append(v.get(0));

		buff.append("+");

		if (n.getRight().getClass().equals(ExprJoin.class)) {
			buff.append(wrap(v.get(1)));
		} else
			buff.append(v.get(1));

		return buff.toString();
	}

	private String wrap(String s) {
		return "(" + s + ")";
	}

	@Override
	public Object visit(ExprVariable n) {
		return n.getVariable().toString();
	}

	@Override
	public Object visit(ExprOverride n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		String result = wrap(v.get(0)) + "++" + wrap(v.get(1));
		return result;
	}

	@Override
	public Object visit(ExprProduct n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		String result = wrap(v.get(0)) + "->" + wrap(v.get(1));
		return result;
	}

	@Override
	public Object visit(ExprFunction n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		StringBuffer buff = new StringBuffer();
		buff.append(n.getFunctionId());
		buff.append("[");
		for (int i = 0; i < v.size(); i++) {
			if (i != 0)
				buff.append(",");
			buff.append(v.get(i));

		}
		buff.append("]");
		return buff.toString();
	}

	@Override
	public Object visit(ExprIntLiteral n) {
		return new Integer(n.getIntLiteral()).toString();
	}

	@Override
	public Object visit(ExprUnary n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		String result = String.format("#(%s)", v.get(0));
		return result;
	}

}
