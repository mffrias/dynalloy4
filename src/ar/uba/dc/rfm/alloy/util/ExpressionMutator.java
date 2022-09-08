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

import java.util.Arrays;
import java.util.Collections;
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
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

/**
 * Substitutes variables for expressions using the abstract method
 * getExpressionForVariable
 * 
 * @author jgaleotti
 * 
 */
public class ExpressionMutator extends ExpressionVisitor {

	@Override
	public Object visit(ExprComprehension n) {

		Vector<Object> v = (Vector<Object>) super.visit(n);

		List<String> names = new LinkedList<String>(n.getNames());
		Vector<AlloyExpression> sets = (Vector<AlloyExpression>) v.get(0);
		AlloyFormula formula = (AlloyFormula) v.get(1);

		return new ExprComprehension(names, sets, formula);
	}

	@Override
	public Object visit(ExprIfCondition n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);

		AlloyFormula condition = (AlloyFormula) v.get(0);
		AlloyExpression left = (AlloyExpression) v.get(1);
		AlloyExpression right = (AlloyExpression) v.get(2);

		return new ExprIfCondition(condition, left, right);
	}

	@Override
	public Object visit(ExprIntersection n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression l = v.get(0);
		AlloyExpression r = v.get(1);
		return new ExprIntersection(l, r);
	}

	@Override
	public Object visit(ExprSum n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);

		Vector<AlloyExpression> set_of_expressions = (Vector<AlloyExpression>) v
				.get(0);
		AlloyExpression expression = (AlloyExpression) v.get(1);

		List<String> names = new LinkedList<String>(n.getNames());
		List<AlloyExpression> sets = new LinkedList<AlloyExpression>(
				set_of_expressions);

		return new ExprSum(names, sets, expression);
	}

	public Object visit(ExprVariable n) {
		return new ExprVariable(n.getVariable());
	}

	public ExpressionMutator() {

	}

	public Object visit(ExprJoin n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression l = v.get(0);
		AlloyExpression r = v.get(1);
		return new ExprJoin(l, r);
	}

	public Object visit(ExprUnion n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression l = v.get(0);
		AlloyExpression r = v.get(1);
		return new ExprUnion(l, r);
	}

	public Object visit(ExprConstant n) {
		return new ExprConstant(null, n.getConstantId());
	}

	@Override
	public Object visit(ExprOverride n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression lvalue = v.get(0);
		AlloyExpression rvalue = v.get(1);
		return new ExprOverride(lvalue, rvalue);
	}

	@Override
	public Object visit(ExprProduct n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression lvalue = v.get(0);
		AlloyExpression rvalue = v.get(1);
		return new ExprProduct(lvalue, rvalue);
	}

	@Override
	public Object visit(ExprFunction n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (AlloyExpression e : v)
			l.add(e);

		return new ExprFunction(null, n.getFunctionId(), l);
	}

	@Override
	public Object visit(ExprIntLiteral n) {
		return new ExprIntLiteral(n.getIntLiteral());
	}

	@Override
	public Object visit(ExprUnary n) {
		Vector<AlloyExpression> v = (Vector<AlloyExpression>) super.visit(n);
		AlloyExpression expression = v.get(0);
		return new ExprUnary(n.getOperator(), expression);
	}

}
