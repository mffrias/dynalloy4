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

import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class AlloyAssertion {

	public Object accept(IAlloyVisitor v) {
		return v.visit(this);
	}
	
	private final String assertionId;

	private final AlloyTyping quantifiedVariables;

	private final AlloyFormula formula;

	public AlloyAssertion(String assertionId,
			AlloyTyping quantifiedVariables, AlloyFormula formula) {
		super();
		this.assertionId = assertionId;
		this.quantifiedVariables = quantifiedVariables;
		this.formula = formula;
	}

	public String getAssertionId() {
		return assertionId;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public AlloyTyping getQuantifiedVariables() {
		return quantifiedVariables;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		buff.append(getAssertionId() + ",");
		buff.append(getQuantifiedVariables() + "," );
		buff.append(getFormula());
		buff.append("]");
		return buff.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assertionId == null) ? 0 : assertionId.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime
				* result
				+ ((quantifiedVariables == null) ? 0 : quantifiedVariables
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AlloyAssertion other = (AlloyAssertion) obj;
		if (assertionId == null) {
			if (other.assertionId != null)
				return false;
		} else if (!assertionId.equals(other.assertionId))
			return false;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (quantifiedVariables == null) {
			if (other.quantifiedVariables != null)
				return false;
		} else if (!quantifiedVariables.equals(other.quantifiedVariables))
			return false;
		return true;
	}

}
