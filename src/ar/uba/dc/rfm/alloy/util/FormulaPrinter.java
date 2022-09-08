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

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.AndFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.ImpliesFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.OrFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

import static ar.uba.dc.rfm.alloy.util.AlloyPrinter.increaseIdentation;

public class FormulaPrinter extends FormulaVisitor {

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.prettyPrinting = prettyPrinting;
	}

	private boolean prettyPrinting = false;

	public FormulaPrinter() {
		this(new ExpressionPrinter());
	}

	public FormulaPrinter(ExpressionVisitor expressionPrinter) {
		super(expressionPrinter);
	}

	@Override
	public Object visit(AndFormula f) {
		Vector<String> v = (Vector<String>) super.visit(f);

		String left;
		String right;

		if (compatibleToAnd(f.getLeft()))
			left = v.get(0);
		else
			left = wrap(v.get(0));

		if (compatibleToAnd(f.getRight()))
			right = v.get(1);
		else
			right = wrap(v.get(1));

		StringBuffer buff = new StringBuffer();

		if (prettyPrinting) {
			buff.append(left + "\n");
			buff.append("and \n");
			buff.append(right);
		} else {
			buff.append(left);
			buff.append(" and ");
			buff.append(right);
		}

		return buff.toString();
	}

	@Override
	public Object visit(ImpliesFormula f) {
		Vector<String> v = (Vector<String>) super.visit(f);

		String left;
		String right;

		if (compatibleToImplies(f.getLeft()))
			left = v.get(0);
		else
			left = wrap(v.get(0));

		if (compatibleToImplies(f.getRight()))
			right = v.get(1);
		else
			right = wrap(v.get(1));

		StringBuffer buff = new StringBuffer();

		if (prettyPrinting) {
			buff.append(left + "\n");
			String impliesKeyword = "implies ";
			buff.append(impliesKeyword + "\n");
			buff.append(increaseIdentation(right, impliesKeyword.length()));
		} else {
			buff.append(left);
			buff.append(" implies ");
			buff.append(right);
		}
		return buff.toString();
	}

	@Override
	public Object visit(NotFormula f) {
		Vector<String> v = (Vector<String>) super.visit(f);
		return "not " + wrap(v.get(0));
	}

	private String wrap(String s) {
		if (prettyPrinting) {
			StringBuffer buff = new StringBuffer();
			buff.append("(\n");
			buff.append(increaseIdentation(s + "\n"));
			buff.append(")");
			return buff.toString();
		} else
			return "(" + s + ")";
	}

	private boolean compatibleToOr(AlloyFormula f) {
		return f.getClass().equals(OrFormula.class)
				|| f.getClass().equals(PredicateFormula.class);
	}

	@Override
	public Object visit(OrFormula f) {
		Vector<String> v = (Vector<String>) super.visit(f);

		StringBuffer buff = new StringBuffer();

		String left;
		String right;

		if (compatibleToOr(f.getLeft()))
			left = v.get(0);
		else
			left = wrap(v.get(0));

		if (compatibleToOr(f.getRight()))
			right = v.get(1);
		else
			right = wrap(v.get(1));

		if (prettyPrinting) {
			buff.append(left + "\n");
			buff.append("or \n");
			buff.append(right + "\n");
		} else {
			buff.append(left);
			buff.append(" or ");
			buff.append(right);
		}
		return buff.toString();
	}

	@Override
	public Object visit(PredicateFormula n) {
		Vector<String> v = (Vector<String>) super.visit(n);

		StringBuffer buff = new StringBuffer();
		String predicateId = (n.getAliasModuleId() == null ? "" : n
				.getAliasModuleId()
				+ "/")
				+ n.getPredicateId();

		buff.append(predicateId + "[");

		for (int i = 0; i < v.size(); i++) {
			String s = v.get(i);
			if (i == 0) {
				buff.append(s);
			} else {
				buff.append(",");
				if (prettyPrinting) {
					buff.append("\n");
					s = increaseIdentation(s, predicateId.length());
				}
				buff.append(s);
			}
		}
		buff.append("]");

		return buff.toString();
	}

	@Override
	public Object visit(EqualsFormula n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		return v.get(0) + "=" + v.get(1);
	}

	private boolean compatibleToImplies(AlloyFormula f) {
		return f.getClass().equals(PredicateFormula.class);
	}

	private boolean compatibleToAnd(AlloyFormula f) {
		return f.getClass().equals(AndFormula.class)
				|| f.getClass().equals(PredicateFormula.class);
	}
}
