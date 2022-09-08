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

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;

public class FormulaVisitor implements IFormulaVisitor {

	private ExpressionVisitor dfsExprVisitor;

	public FormulaVisitor() {
		this(null);
	}

	public void setExpressionVisitor(ExpressionVisitor expressionVisitor) {
		dfsExprVisitor = expressionVisitor;
	}
	
	public FormulaVisitor(ExpressionVisitor _exprVisitor) {
		dfsExprVisitor = _exprVisitor;
		if (dfsExprVisitor != null)
			dfsExprVisitor.setFormulaVisitor(this);
	}

	public Object visit(AndFormula f) {
		Object l = f.getLeft().accept(this);
		Object r = f.getRight().accept(this);
		Vector<Object> result = new Vector<Object>();
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(OrFormula f) {
		Object l = f.getLeft().accept(this);
		Object r = f.getRight().accept(this);
		Vector<Object> result = new Vector<Object>();
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(ImpliesFormula f) {
		Object l = f.getLeft().accept(this);
		if (f.getRight() == null){
			int i = 0;
		}
		Object r = f.getRight().accept(this);
		Vector<Object> result = new Vector<Object>();
		result.add(l);
		result.add(r);
		return result;
	}

	public Object visit(NotFormula f) {
		Object u = f.getFormula().accept(this);
		Vector<Object> result = new Vector<Object>();
		result.add(u);
		return result;
	}

	public Object visit(PredicateFormula n) {
		Vector<Object> result = new Vector<Object>();
		if (getDfsExprVisitor() != null) {
			for (AlloyExpression e : n.getParameters()) {
				Object r = e.accept(getDfsExprVisitor());
				result.add(r);
			}
		}
		return result;
	}

	public Object visit(EqualsFormula n) {
		Vector<Object> result = new Vector<Object>();
		if (getDfsExprVisitor() != null) {
			result.add(n.getLeft().accept(getDfsExprVisitor()));
			result.add(n.getRight().accept(getDfsExprVisitor()));
		}
		return result;
	}
	
	
	public Object visit(QuantifiedFormula n){
		Vector<Object> result = new Vector<Object>();
		result.add(n.getOperator());
		result.add(n.getNames());
		result.add(n.getSets());
		AlloyFormula af = (AlloyFormula)n.getFormula().accept(this);
		result.add(af);
		return result;
	}

	public ExpressionVisitor getDfsExprVisitor() {
		return dfsExprVisitor;
	}

}
