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
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.AlloyVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class AlloyMutator extends AlloyVisitor {

	public AlloyMutator() {
		super(new FormulaMutator());
	}

	public AlloyMutator(FormulaMutator formulaMutator) {
		super(formulaMutator);
	}

	@Override
	public Object visit(AlloyAssertion n) {
		Vector<AlloyFormula> vec = (Vector<AlloyFormula>) super.visit(n);
		AlloyTyping t = new AlloyTyping();
		for (AlloyVariable v : n.getQuantifiedVariables().varSet()) {
			t.put(v, n.getQuantifiedVariables().get(v));
		}
		return new AlloyAssertion(n.getAssertionId(), t, vec.get(0));
	}

	@Override
	public Object visit(AlloyFact n) {
		Vector<AlloyFormula> v = (Vector<AlloyFormula>) super.visit(n);
		return new AlloyFact(v.get(0));
	}

	@Override
	public Object visit(AlloySig n) {
		return n;
	}

	@Override
	public Object visit(AlloyModule n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);

		AlloySig s = null;
		List<AlloyFact> fs = new LinkedList<AlloyFact>();
		AlloyAssertion a = null;

		for (Object o : v) {
			if (o != null) {
				if (o.getClass().equals(AlloyFact.class))
					fs.add((AlloyFact) o);
				else if (o.getClass().equals(AlloySig.class))
					s = (AlloySig) o;
				else if (o.getClass().equals(AlloyAssertion.class))
					a = (AlloyAssertion) o;
			}
		}

		return new AlloyModule(n.getAlloyStr(), s, fs, a);
	}

}
