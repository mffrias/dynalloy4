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
package ar.uba.dc.rfm.dynalloy.xlator;

import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.IdxRange;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.util.VarSubstitutor;

/**
 * Gives the first indices to the variables of a formula that has none yet: {@code x} becomes
 * {@code x_0} and {@code x'} becomes {@code x_1}; {@code \pre[x]} becomes {@code x_0} marked
 * immutable, so that {@link OffsetSubindexer} never shifts it and {@link IdxRangeCollectHelper}
 * leaves it out of the ranges: it always means the value before the enclosing program. Used
 * through a {@code FormulaMutator} (new nodes) on the pre- and postconditions of actions
 * ({@link VariableIndexer}), on tests ({@link ProgramTranslator}), and on the pre, the post
 * and TACO's arithmetic predicates in the assertion ({@link DynalloyXlatorVisitor}).
 *
 * <p>Assumes that no variable is indexed yet: an indexed one makes it throw a
 * {@code RuntimeException} ("Indexer only deals with plain and primed variables"). Variables
 * already marked immutable are left as they are. No state.
 */
final class FirstSubindexer extends VarSubstitutor {

	public FirstSubindexer() {
	}

	@Override
	protected AlloyExpression getExpr(AlloyVariable variable) {
		AlloyVariable indexedVariable;
		if (variable.isPlain()) {
			indexedVariable = new AlloyVariable(variable.getVariableId(),
					IdxRange.SUBINDEX_0);
			return new ExprVariable(indexedVariable);
		} else if (variable.isPrimed()) {
			indexedVariable = new AlloyVariable(variable.getVariableId(),
					IdxRange.SUBINDEX_1);
			return new ExprVariable(indexedVariable);
		} else if (variable.isPreStateVar()) {
			indexedVariable = new AlloyVariable(variable.getVariableId(),
					IdxRange.SUBINDEX_0);
			indexedVariable.setMutable(false);
			return new ExprVariable(indexedVariable);
		} else
			throw new RuntimeException(variable
					+ " Indexer only deals with plain and primed variables");
	}

	@Override
	protected void doWhenVarNotFound(ExprVariable n) {
		throw new RuntimeException("cannot give index to variable: "
				+ n.getVariable());
	}

}
