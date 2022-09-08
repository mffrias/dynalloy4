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
package ar.uba.dc.rfm.da2a.simplify;

import java.util.HashSet;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.AlloyVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.util.VarCollector;

public final class RemoveUnusedVariables extends Rule {

	@Override
	public AlloyModule applyTo(AlloyModule module) {
		Set<AlloyAssertion> assertions = new HashSet<AlloyAssertion>();
		for (AlloyAssertion oldAssertion : module.getAssertions()) {
			Set<AlloyVariable> usedVariables = collectUsedVariables(oldAssertion);
			AlloyAssertion newAssertion = buildNewAssertion(oldAssertion, usedVariables);
			assertions.add(newAssertion);
		}
		return new AlloyModule(module.getAlloyStr(), module.getGlobalSig(), module.getFacts(),
				assertions);
	}

	private AlloyAssertion buildNewAssertion(AlloyAssertion assertion,
			Set<AlloyVariable> usedVars) {
		AlloyTyping typing = new AlloyTyping();
		for (AlloyVariable v : usedVars)
			if (assertion.getQuantifiedVariables().contains(v))
				typing.put(v, assertion.getQuantifiedVariables().get(v));
		return new AlloyAssertion(assertion.getAssertionId(), typing, assertion
				.getFormula());
	}

	private Set<AlloyVariable> collectUsedVariables(AlloyAssertion in) {
		VarCollector varCollector = new VarCollector();
		in.accept(new AlloyVisitor(new FormulaVisitor(varCollector)));
		return varCollector.getVariables();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(RemoveUnusedVariables.class))
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "RemoveUnusedVariables";
	}

}
