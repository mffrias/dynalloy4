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
package ar.uba.dc.rfm.da2a.qremover;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.IFormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.QuantifiedFormula;
import ar.uba.dc.rfm.dynalloy.visualization.DynalloyVisualizerException;

class FormulaBreaker implements IFormulaVisitor {

	private boolean alreadyVisiting;

	public FormulaBreaker() {
		alreadyVisiting = false;
	}

	public Object visit(AndFormula f) {
		if (!alreadyVisiting)
			throw new UnsupportedOperationException();

		List<AlloyFormula> l = (List<AlloyFormula>) f.getLeft().accept(this);
		List<AlloyFormula> r = (List<AlloyFormula>) f.getRight().accept(this);
		return mergeLists(l, r);
	}

	private List<AlloyFormula> mergeLists(List<AlloyFormula> l1, List<AlloyFormula> l2) {
		List<AlloyFormula> result = new LinkedList<AlloyFormula>();
		result.addAll(l1);
		result.addAll(l2);
		return result;
	}

	public Object visit(OrFormula f) {
		if (!alreadyVisiting)
			throw new UnsupportedOperationException();

		return buildSingleton(f);
	}

	public Object visit(ImpliesFormula f) {
		if (alreadyVisiting)
			throw new UnsupportedOperationException();
		else
			alreadyVisiting = true;

		List<AlloyFormula> l = (List<AlloyFormula>) f.getLeft().accept(this);
		List<AlloyFormula> r = (List<AlloyFormula>) f.getRight().accept(this);
		return mergeLists(l, r);
	}

	public Object visit(NotFormula f) {
		if (!alreadyVisiting)
			throw new UnsupportedOperationException();

		return buildSingleton(f);
	}

	public Object visit(PredicateFormula f) {
		if (!alreadyVisiting)
			throw new UnsupportedOperationException();

		return buildSingleton(f);
	}

	private List<AlloyFormula> buildSingleton(AlloyFormula f) {
		List<AlloyFormula> result = new LinkedList<AlloyFormula>();
		result.add(f);
		return result;
	}

	public Object visit(EqualsFormula n) {
		if (!alreadyVisiting)
			throw new UnsupportedOperationException();

		return buildSingleton(n);
	}
	
	public Object visit(QuantifiedFormula n) {
		throw new DynalloyVisualizerException();
	}
	
}
