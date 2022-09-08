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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprComprehension;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprSum;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.IFormulaVisitor;

public class VarCollector extends ExpressionVisitor {

	protected Set<AlloyVariable> variables = new HashSet<AlloyVariable>();

	protected List<String> boundNames = new LinkedList<String>();
	protected Stack<List<String>> boundContext = new Stack<List<String>>();

	public Set<AlloyVariable> getVariables() {
		return variables;
	}

	@Override
	public Object visit(ExprVariable n) {
		if (!boundNames.contains(n.getVariable().toString()))
			variables.add(n.getVariable());

		return null;
	}

	@Override
	public Object visit(ExprComprehension e) {
		boundContext.push(new LinkedList<String>());

		for (String name : e.getNames()) {
			boundContext.peek().add(name);
			boundNames.add(name);
			AlloyExpression setOf = e.getSetOf(name);
			setOf.accept(this);
		}
		e.getFormula().accept(this.formulaVisitor);

		List<String> pop = boundContext.pop();
		boundNames.removeAll(pop);

		return null;
	}

	@Override
	public Object visit(ExprIfCondition e) {
		e.getCondition().accept(formulaVisitor);
		e.getLeft().accept(this);
		e.getRight().accept(this);

		return null;
	}

	@Override
	public Object visit(ExprSum e) {
		boundContext.push(new LinkedList<String>());

		for (String name : e.getNames()) {
			boundContext.peek().add(name);
			boundNames.add(name);
			AlloyExpression setOf = e.getSetOf(name);
			setOf.accept(this);
		}
		e.getExpression().accept(this);

		List<String> pop = boundContext.pop();
		boundNames.removeAll(pop);

		return null;
	}

	@Override
	public Object visit(ExprIntersection e) {
		e.getLeft().accept(this);
		e.getRight().accept(this);

		return null;
	}

}
