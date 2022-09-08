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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.ExpressionMutator;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class ProgramMutator extends DfsProgramVisitor {

	@Override
	public Object visit(Assigment n) {
		if (getFormulaMutator() == null)
			throw new IllegalStateException();

		ExpressionMutator exprMutator = getFormulaMutator().getExpressionMutator();
		if (exprMutator == null)
			throw new IllegalStateException();

		ExprVariable left = (ExprVariable) n.getLeft().accept(exprMutator);
		AlloyExpression right = (AlloyExpression) n.getRight().accept(exprMutator);

		return new Assigment(left, right, n.getPosition());
	}

	private DynAlloyAlloyMapping mapping;

	public ProgramMutator(FormulaMutator formulaMutator) {
		super(formulaMutator);
	}

	@Override
	public Object visit(Choice c) {
		Vector<DynalloyProgram> v = (Vector<DynalloyProgram>) super.visit(c);
		return new Choice(new LinkedList<DynalloyProgram>(v), c.getPosition());
	}

	@Override
	public Object visit(Closure c) {
		Vector<DynalloyProgram> v = (Vector<DynalloyProgram>) super.visit(c);
		return new Closure(v.get(0), c.getPosition());
	}

	@Override
	public Object visit(Composition c) {
		Vector<DynalloyProgram> v = (Vector<DynalloyProgram>) super.visit(c);
		return new Composition(new LinkedList<DynalloyProgram>(v), c.getPosition());
	}

	@Override
	public Object visit(WhileProgram w) {

		Vector<Object> v =(Vector<Object>) super.visit(w);
		PredicateFormula pf = (PredicateFormula) v.get(0);
		DynalloyProgram body = (DynalloyProgram) v.get(1);
		
		return new WhileProgram(pf, body, w.getPosition());
	}

	@Override
	public Object visit(InvokeAction u) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		for (AlloyExpression e : u.getActualParameters()) {
			if (getFormulaMutator() == null)
				throw new IllegalStateException();

			if (getFormulaMutator().getExpressionMutator() == null)
				throw new IllegalStateException();

			if (e == null)
				throw new IllegalStateException();

			ps.add((AlloyExpression) e.accept(getFormulaMutator().getExpressionMutator()));
		}
		return new InvokeAction(u.getAliasModuleId(), u.getActionId(), ps, u.getPosition());
	}

	@Override
	public Object visit(InvokeProgram u) {
		List<AlloyExpression> ps = new LinkedList<AlloyExpression>();
		for (AlloyExpression e : u.getActualParameters()) {
			if (getFormulaMutator() == null)
				throw new IllegalStateException();

			if (getFormulaMutator().getExpressionMutator() == null)
				throw new IllegalStateException();

			if (e == null)
				throw new IllegalStateException();

			ps.add((AlloyExpression) e.accept(getFormulaMutator().getExpressionMutator()));
		}
		return new InvokeProgram(u.getAliasModuleId(), u.getProgramId(), ps, u.getPosition());
	}

	@Override
	public Object visit(Skip s) {
		return new Skip(s.getPosition());
	}

	@Override
	public Object visit(TestPredicate t) {
		PredicateFormula pf = (PredicateFormula) t.getPredicateFormula().accept(getFormulaMutator());

		TestPredicate fresh_test_predicate = new TestPredicate(pf, t.getTestPredicateIsTrue(), t.getPosition());

		if (t.getLabel() != null)
			fresh_test_predicate.setLabel(t.getLabel());

		return fresh_test_predicate;
	}

	public FormulaMutator getFormulaMutator() {
		return (FormulaMutator) this.getDfsFormulaVisitor();
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}

}
