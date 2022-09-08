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
import java.util.Stack;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprComprehension;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprConstant;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIfCondition;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprIntersection;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprSum;
import ar.uba.dc.rfm.alloy.ast.expressions.ExpressionVisitor;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.IFormulaVisitor;

public class LiteralCollectorExpVisitor extends ExpressionVisitor {

	protected Set<String> intLiterals = new HashSet<String>();
	protected Set<String> longLiterals = new HashSet<String>();
	protected Set<String> charLiterals = new HashSet<String>();
	protected Set<String> floatLiterals = new HashSet<String>();

	public Set<String> getIntLiterals() {
		return intLiterals;
	}

	public Set<String> getLongLiterals() {
		return longLiterals;
	}

	public Set<String> getCharLiterals() {
		return charLiterals;
	}

	public Set<String> getFloatLiterals() {
		return floatLiterals;
	}

	@Override
	public Object visit(ExprConstant n) {
		if (n.getConstantId().contains("JavaPrimitiveIntegerLiteral")){
			this.intLiterals.add(n.getConstantId());
		}
		if (n.getConstantId().contains("JavaPrimitiveLongLiteral")){
			this.longLiterals.add(n.getConstantId());
		}
		if (n.getConstantId().contains("JavaPrimitiveCharLiteral")){
			this.charLiterals.add(n.getConstantId());
		}
		if (n.getConstantId().contains("JavaPrimitiveFloatLiteral")){
			this.floatLiterals.add(n.getConstantId());
		}

		return null;
	}


}
