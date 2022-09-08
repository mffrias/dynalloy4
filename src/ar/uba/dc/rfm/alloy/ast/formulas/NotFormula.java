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
package ar.uba.dc.rfm.alloy.ast.formulas;

public final class NotFormula extends AlloyFormula {

	private final AlloyFormula formula;

	public NotFormula(AlloyFormula form) {
		super();
		formula = form;
	}

	public Object accept(IFormulaVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0!=null) {
			if (arg0.getClass().equals(NotFormula.class)) {
				NotFormula nf = (NotFormula)arg0;
				return nf.getFormula().equals(getFormula());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getFormula().hashCode();
	}	
	
	@Override
	public String toString() {
		return "[" + "Not " + getFormula().toString()+ "]";
	}

	public AlloyFormula getFormula()
	{
		return this.formula;
	}	
	
	
}
