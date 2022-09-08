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
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class FormulaMutator extends FormulaVisitor {

	private DynAlloyAlloyMapping mapping;

	public Object visit(EqualsFormula n) {
		AlloyExpression l = (AlloyExpression) n.getLeft().accept(exprMutator);
		AlloyExpression r = (AlloyExpression) n.getRight().accept(exprMutator);

		EqualsFormula newFormula = new EqualsFormula(l, r);

		if (getMapping() != null) {
			mapping.replaceValue(n, newFormula);
		}

		return newFormula;
	}

	public Object visit(PredicateFormula n) {
		String aliasModuleId = n.getAliasModuleId();
		String predicateId = n.getPredicateId();
		List<AlloyExpression> es = new LinkedList<AlloyExpression>();
		for (AlloyExpression e : n.getParameters()) {
			AlloyExpression sub_e = (AlloyExpression) e.accept(exprMutator);
			es.add(sub_e);
		}

		Position position = n.getPosition();

		PredicateFormula newFormula = new PredicateFormula(aliasModuleId,
				predicateId, es, position);

		if (getMapping() != null) {
			mapping.replaceValue(n, newFormula);
		}

		return newFormula;
	}

	private ExpressionMutator exprMutator;

	public FormulaMutator(ExpressionMutator _exprVisitor) {
		super(_exprVisitor);
		exprMutator = _exprVisitor;
	}

	public FormulaMutator() {
		this(new ExpressionMutator());
	}

	public Object visit(AndFormula f) {
		Vector<AlloyFormula> v = (Vector<AlloyFormula>) super.visit(f);

		AndFormula newFormula = new AndFormula(v.get(0), v.get(1));

		if (getMapping() != null) {
			getMapping().replaceValue(f, newFormula);
		}

		return newFormula;
	}

	public Object visit(ImpliesFormula f) {
		Vector<AlloyFormula> v = (Vector<AlloyFormula>) super.visit(f);

		ImpliesFormula newFormula = new ImpliesFormula(v.get(0), v.get(1));

		if (getMapping() != null) {
			getMapping().replaceValue(f, newFormula);
		}

		return newFormula;
	}

	public Object visit(NotFormula f) {
		Vector<AlloyFormula> v = (Vector<AlloyFormula>) super.visit(f);

		NotFormula newFormula = new NotFormula(v.get(0));

		if (getMapping() != null) {
			getMapping().replaceValue(f, newFormula);
		}

		return newFormula;
	}

	public Object visit(OrFormula f) {
		Vector<AlloyFormula> v = (Vector<AlloyFormula>) super.visit(f);
		OrFormula newFormula = new OrFormula(v.get(0), v.get(1));

		if (getMapping() != null) {
			getMapping().replaceValue(f, newFormula);
		}

		return newFormula;
	}

	
	public Object visit(QuantifiedFormula qf){
		AlloyFormula af = qf.getFormula();
		AlloyFormula result = (AlloyFormula)af.accept(this);
		return new QuantifiedFormula(qf.getOperator(), qf.getNames(), qf.getSets(), result);
	}
	
	
	public ExpressionMutator getExpressionMutator() {
		return exprMutator;
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}

}
