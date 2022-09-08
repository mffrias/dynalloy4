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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import edu.mit.csail.sdg.alloy4compiler.ast.ExprBinary;

import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.IFormulaVisitor;

public class ExpressionVisitor implements IExpressionVisitor {

	protected FormulaVisitor formulaVisitor;

	public void setFormulaVisitor(FormulaVisitor formulaVisitor) {
		this.formulaVisitor = formulaVisitor;
	}

	public Object visit(ExprVariable n) {
		return null;
	}

	public Object visit(ExprJoin n) {
		Vector<Object> result = new Vector<Object>();
		Object l = n.getLeft().accept(this);
		Object r = n.getRight().accept(this);
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(ExprUnion n) {
		Vector<Object> result = new Vector<Object>();
		Object l = n.getLeft().accept(this);
		Object r = n.getRight().accept(this);
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(ExprConstant n) {
		return null;
	}

	public Object visit(ExprOverride n) {
		Vector<Object> result = new Vector<Object>();
		Object l = n.getLeft().accept(this);
		Object r = n.getRight().accept(this);
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(ExprProduct n) {
		Vector<Object> result = new Vector<Object>();
		Object l = n.getLeft().accept(this);
		Object r = n.getRight().accept(this);
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(ExprFunction n) {
		Vector<Object> result = new Vector<Object>();
		for (AlloyExpression e : n.getParameters()) {
			Object r = e.accept(this);
			result.add(r);
		}
		return result;
	}

	public Object visit(ExprIntLiteral n) {
		return null;
	}

	@Override
	public Object visit(ExprIntersection n) {
		Vector<Object> result = new Vector<Object>();
		Object left_result = n.getLeft().accept(this);
		Object right_result = n.getRight().accept(this);
		result.add(left_result);
		result.add(right_result);
		return result;
	}

	@Override
	public Object visit(ExprIfCondition n) {
		Vector<Object> result = new Vector<Object>();
		Object condition_result = null;
		if (formulaVisitor != null) {
			condition_result = n.getCondition().accept(formulaVisitor);
		}
		Object left_result = n.getLeft().accept(this);
		Object right_result = n.getRight().accept(this);

		result.add(condition_result);
		result.add(left_result);
		result.add(right_result);

		return result;

	}

	@Override
	public Object visit(ExprSum n) {

		Vector<Object> set_of_results = new Vector<Object>();
		for (int i = 0; i < n.getNames().size(); i++) {
			String name = n.getNames().get(i);
			AlloyExpression set_of = n.getSetOf(name);
			Object set_of_result = set_of.accept(this);
			set_of_results.add(set_of_result);
		}

		Object expression_result = n.getExpression().accept(this);

		Vector<Object> result = new Vector<Object>();
		result.add(set_of_results);
		result.add(expression_result);
		return result;
	}

	@Override
	public Object visit(ExprComprehension n) {

		Vector<Object> setOf_results = new Vector<Object>();
		for (String name : n.getNames()) {
			AlloyExpression setOf = n.getSetOf(name);
			Object setOf_result = setOf.accept(this);
			setOf_results.add(setOf_result);
		}

		Object formula_result = null;
		if (formulaVisitor != null)
			formula_result = n.getFormula().accept(formulaVisitor);

		Vector<Object> result = new Vector<Object>();
		result.add(setOf_results);
		result.add(formula_result);

		return result;
	}

	@Override
	public Object visit(ExprUnary n) {
		Vector<Object> result = new Vector<Object>();
		Object result_expression = n.getExpression().accept(this);
		result.add(result_expression);
		return result;
	}
	
	

}
