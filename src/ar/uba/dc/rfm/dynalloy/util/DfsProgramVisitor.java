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
package ar.uba.dc.rfm.dynalloy.util;

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.ProgramVisitor;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;

public class DfsProgramVisitor implements ProgramVisitor {

	protected FormulaVisitor getDfsFormulaVisitor() {
		return fVisitor;
	}

	public DfsProgramVisitor(FormulaVisitor f) {
		super();
		fVisitor = f;
	}

	public DfsProgramVisitor() {
		this(null);
	}

	public Object visit(Choice c) {
		Vector<Object> result = new Vector<Object>();
		for (DynalloyProgram program : c) {
			Object o = program.accept(this);
			result.add(o);
		}
		return result;
	}

	public Object visit(Closure c) {
		Vector<Object> result = new Vector<Object>();
		result.add(c.getProgram().accept(this));
		return result;
	}

	public Object visit(WhileProgram w) {
		Vector<Object> result = new Vector<Object>();
		result.add(w.getCondition().accept(fVisitor));
		result.add(w.getBody().accept(this));
		return result;
	}
	
	
	public Object visit(Skip s) {
		return null;
	}

	public Object visit(Composition c) {
		Vector<Object> result = new Vector<Object>();
		for (DynalloyProgram p : c) {
			Object o = p.accept(this);
			result.add(o);
		}
		return result;
	}

	public Object visit(TestPredicate t) {
		if (this.getDfsFormulaVisitor() != null) {
			Object u = t.getPredicateFormula().accept(fVisitor);
			Vector<Object> result = new Vector<Object>();
			result.add(u);
			return result;
		} else
			return null;
	}

	private final FormulaVisitor fVisitor;

	public Object visit(InvokeAction u) {
		if (this.getDfsFormulaVisitor() != null
				&& this.getDfsFormulaVisitor().getDfsExprVisitor() != null) {
			Vector<Object> result = new Vector<Object>();
			for (AlloyExpression e : u.getActualParameters()) {
				Object o = e.accept(this.fVisitor.getDfsExprVisitor());
				result.add(o);
			}
			return result;
		} else
			return null;
	}

	public Object visit(InvokeProgram u) {
		if (this.getDfsFormulaVisitor() != null
				&& this.getDfsFormulaVisitor().getDfsExprVisitor() != null) {
			Vector<Object> result = new Vector<Object>();
			for (AlloyExpression e : u.getActualParameters()) {
				Object o = e.accept(this.fVisitor.getDfsExprVisitor());
				result.add(o);
			}
			return result;
		} else
			return null;
	}

	@Override
	public Object visit(Assigment assigment) {
		if (this.getDfsFormulaVisitor() != null
				&& this.getDfsFormulaVisitor().getDfsExprVisitor() != null) {
			Vector<Object> result = new Vector<Object>();
			ExpressionVisitor dfsExprVisitor = this.fVisitor
					.getDfsExprVisitor();
			Object leftResult = assigment.getLeft().accept(dfsExprVisitor);
			result.add(leftResult);
			Object rightResult = assigment.getRight().accept(dfsExprVisitor);
			result.add(rightResult);
			return result;
		} else
			return null;
	}

}
