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

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public final class AlloyFact {

	private final AlloyFormula formula;

	public AlloyFact(AlloyFormula f) {
		super();
		this.formula = f;
	}

	public AlloyFormula getFormula() {
		return formula;
	}

	public Object accept(IAlloyVisitor v) {
		return v.visit(this);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(AlloyFact.class)) {
			AlloyFact that = (AlloyFact) arg0;
			return this.getFormula().equals(that.getFormula());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return this.getFormula().hashCode();
	}

	@Override
	public String toString() {
		return "fact[" + this.getFormula().toString() + "]";
	}

}
