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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;
import static ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable.buildExprVariable;

/**
 * Represents a P(expr1,...exprn) formula
 * @author jgaleotti
 *
 */
public final class PredicateFormula extends AlloyFormula {

	public PredicateFormula(String aliasModuleId, String predicateId,
			List<AlloyExpression> parameters) {
		super();
		this.aliasModuleId = aliasModuleId;
		this.predicateId = predicateId;
		this.parameters = parameters;
	}

	public PredicateFormula(String aliasModuleId, String predicateId,
			List<AlloyExpression> parameters, Position position) {
		this(aliasModuleId, predicateId, parameters);
		setPosition(position);
	}
	
	
	public static PredicateFormula buildPredicate(String predicateId) {
		return new PredicateFormula(null, predicateId, new LinkedList<AlloyExpression>());
	}
	
	public static PredicateFormula buildPredicate(String predicateId, AlloyVariable... vs) {
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (int i = 0; i < vs.length; i++) 
			l.add(buildExprVariable(vs[i]));
		return new PredicateFormula(null, predicateId, l);
	}
	
	public static PredicateFormula buildPredicate(String predicateId, AlloyExpression... ps) {
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (int i = 0; i < ps.length; i++) 
			l.add(ps[i]);
		return new PredicateFormula(null, predicateId, l);
	}
	
	// null == this
	private final String aliasModuleId;
	
	private final String predicateId;

	private final List<AlloyExpression> parameters;

	@Override
	public Object accept(IFormulaVisitor visitor) {
		return visitor.visit(this);
	}

	public List<AlloyExpression> getParameters() {
		return parameters;
	}

	public String getPredicateId() {
		return predicateId;
	}

	@Override
	public String toString() {
		return getPredicateId().toString() + getParameters().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasModuleId == null) ? 0 : aliasModuleId.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result
				+ ((predicateId == null) ? 0 : predicateId.hashCode());
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
		final PredicateFormula other = (PredicateFormula) obj;
		if (aliasModuleId == null) {
			if (other.aliasModuleId != null)
				return false;
		} else if (!aliasModuleId.equals(other.aliasModuleId))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (predicateId == null) {
			if (other.predicateId != null)
				return false;
		} else if (!predicateId.equals(other.predicateId))
			return false;
		return true;
	}

	public String getAliasModuleId() {
		return aliasModuleId;
	}	

}
