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
package ar.uba.dc.rfm.alloy.ast;

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;

public class AlloyVisitor implements IAlloyVisitor {

	protected FormulaVisitor getFormulaVisitor() {
		return formulaVisitor;
	}

	public AlloyVisitor(FormulaVisitor formulaVisitor) {
		super();
		this.formulaVisitor = formulaVisitor;
	}

	public Object visit(AlloyModule n) {
		Vector<Object> v = new Vector<Object>();

		if (n.getGlobalSig() != null)
			v.add(n.getGlobalSig().accept(this));

		for (AlloyFact f : n.getFacts()) {
			v.add(f.accept(this));
		}
		for (AlloyAssertion assertion : n.getAssertions()) {
			v.add(assertion.accept(this));
		}

		return v;
	}

	private FormulaVisitor formulaVisitor;

	public Object visit(AlloyAssertion n) {
		if (formulaVisitor != null) {
			Vector<Object> v = new Vector<Object>();
			v.add(n.getFormula().accept(formulaVisitor));
			return v;
		}
		return null;
	}

	public Object visit(AlloySig n) {
		return null;
	}

	public Object visit(AlloyFact fact) {
		if (formulaVisitor != null) {
			Vector<Object> v = new Vector<Object>();
			v.add(fact.getFormula().accept(formulaVisitor));
			return v;
		} else
			return null;
	}

}
