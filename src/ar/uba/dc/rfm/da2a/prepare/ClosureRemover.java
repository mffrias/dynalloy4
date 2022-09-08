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
package ar.uba.dc.rfm.da2a.prepare;

import ar.uba.dc.rfm.alloy.util.FormulaCloner;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;
import ar.uba.dc.rfm.dynalloy.util.ProgramCloner;
import ar.uba.dc.rfm.dynalloy.util.ProgramMutator;

public class ClosureRemover extends ProgramMutator {

	private int loopUnrollSize;

	private boolean addSkip;

	private boolean removeExitWhileGuard;
	
	public ClosureRemover(int loopUnrollSize, boolean addSkip, boolean removeExitWhileGuard) {
		super(new FormulaCloner());
		this.loopUnrollSize = loopUnrollSize;
		this.addSkip = addSkip;
		this.removeExitWhileGuard = removeExitWhileGuard;
	}

	@Override
	public Object visit(Closure c) {
		Closure n = (Closure) super.visit(c);
		if (this.loopUnrollSize == 0) {
			return new Skip();
		} else {
			DynalloyProgram p;
			if (this.addSkip)
				p = new Choice(n.getProgram(), new Skip());
			else
				p = n.getProgram();

			DynalloyProgram result = null;
			for (int i = 0; i < this.loopUnrollSize; i++) {
				DynalloyProgram cloned_p = (DynalloyProgram) p.accept(new ProgramCloner());
				if (result == null)
					result = cloned_p;
				else
					result = new Composition(result, cloned_p);
			}

			if (getMapping() != null) {
				getMapping().addSimplification(n, result);
			}
			return result;
		}
	}

	public Object visit(WhileProgram w) {
		WhileProgram n = (WhileProgram) super.visit(w);

		DynalloyProgram nested_unroll = new Skip();

		for (int i = 0; i < this.loopUnrollSize; i++) {
			TestPredicate true_condition = new TestPredicate(n.getCondition());
			TestPredicate false_condition = new TestPredicate(n.getCondition(), false);

			DynalloyProgram cloned_body = (DynalloyProgram) n.getBody().accept(new ProgramCloner());

			Composition then_branch = Composition.buildComposition(true_condition, cloned_body, nested_unroll);
			Composition else_branch = Composition.buildComposition(false_condition, new Skip());

			nested_unroll = new Choice(then_branch, else_branch);
		}

		TestPredicate assume_not_guard = new TestPredicate(n.getCondition(), false);
		DynalloyProgram result;
		if (removeExitWhileGuard) {
			result = nested_unroll;
		} else {
			result = Composition.buildComposition(nested_unroll, assume_not_guard);
		}
		if (getMapping() != null) {
			getMapping().addSimplification(n, result);
		}
		return result;
	}
}
