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
package ar.uba.dc.rfm.dynalloy.ast.programs;

import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.TestPredicateLabel;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;

/**
 * @author jgaleotti
 *
 */
public final class TestPredicate extends DynalloyProgram {

	public TestPredicateLabel getLabel() {
		return label;
	}

	public void setLabel(TestPredicateLabel label) {
		this.label = label;
	}

	public boolean getTestPredicateIsTrue() {
		return testPredicateIsTrue;
	}

	private final PredicateFormula predicateFormula;

	private final boolean testPredicateIsTrue;

	private TestPredicateLabel label = null;

	public TestPredicate(PredicateFormula p, boolean testTruePredicate) {
		this.predicateFormula = p;
		this.testPredicateIsTrue = testTruePredicate;
	}

	public TestPredicate(PredicateFormula p) {
		this(p, true);
	}

	public TestPredicate(PredicateFormula p, boolean testIfPredicateIsTrue,
			Position pos) {
		this(p, testIfPredicateIsTrue);
		setPosition(pos);
	}

	public TestPredicate(PredicateFormula p, Position pos) {
		this(p, true);
		setPosition(pos);
	}

	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

	public PredicateFormula getPredicateFormula() {
		return predicateFormula;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 != null && arg0.getClass().equals(TestPredicate.class)) {
			TestPredicate that = (TestPredicate) arg0;
			return this.getPredicateFormula()
					.equals(that.getPredicateFormula());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return getPredicateFormula().hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		buff.append(getPredicateFormula().toString());
		if (label != null) {
			buff.append(" ");
			buff.append(label.toString());
		}
		buff.append(")?");
		return buff.toString();
	}

}
