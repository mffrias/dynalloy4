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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.NotFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;

/**
 * Translates a loop-free DynAlloy program into an indexed Alloy formula; every visit returns
 * an {@link AlloyFormulaWithLocals}:
 * <ul>
 * <li>an action call {@code a[...]} becomes a call of the action's predicate, with the
 *     arguments of its calling convention ({@link SpecContext#invokeAction});</li>
 * <li>a program call becomes a call of the program's predicate, with its local variables
 *     renamed ({@link #visit(InvokeProgram)});</li>
 * <li>{@code x := e} becomes {@code x_1 = e_0} ({@link SpecContext#assigment});</li>
 * <li>a test {@code [f]?} becomes {@code f} indexed by {@link FirstSubindexer}, negated for a
 *     negative test (the false branch of an {@code if}, the exit of a {@code while});</li>
 * <li>{@code skip} becomes {@code TruePred[]} ({@link AlloyPredicates});</li>
 * <li>a sequence or a choice joins its parts left to right with {@link CompositionJoiner} or
 *     {@link ChoiceJoiner}, and merges their locals.</li>
 * </ul>
 * {@link DynalloyXlatorVisitor} creates one for itself and runs it on each program body and on
 * the assertion's program. If the context has a {@code DynAlloyAlloyMapping}, each visit records
 * the DynAlloy node and its formula in it (for a sequence or a choice, each prefix
 * {@code sub(i + 1)} with the formula joined so far), for the trace and the visualizer. The
 * program is not changed.
 *
 * <p>Assumes the loops are unrolled ({@code da2a.prepare.ClosureRemover}, which
 * {@code DynAlloyTranslator} runs first): a {@code Closure} or a {@code WhileProgram} throws an
 * {@code UnsupportedOperationException}. Assumes too that the actions were translated first,
 * which registers their calling conventions, and that no program calls itself, directly or
 * through others ({@link #visit(InvokeProgram)}). Without a context, only tests, {@code skip}
 * and sequences of them can be translated.
 *
 * <p>No static state but a constant {@code TruePred[]}, which is not used. The
 * {@link SpecContext} is shared with the visitor and changed by program calls.
 */
class ProgramTranslator extends DfsProgramVisitor {

	private static final PredicateFormula TRUE_CONSTANT = new PredicateFormula(
			null, AlloyPredicates.TRUE_PRED_ID, Collections
					.<AlloyExpression> emptyList());

	private SpecContext context;

	public ProgramTranslator(SpecContext _context) {
		context = _context;
	}

	public ProgramTranslator() {
		this(null);
	}

	public void bindContext(SpecContext _context) {
		context = _context;
	}

	public Object visit(Choice n) {
		Vector<AlloyFormulaWithLocals> children = (Vector<AlloyFormulaWithLocals>) super
				.visit(n);

		AlloyFormula result = null;
		AlloyTyping locals = new AlloyTyping();
		AlloyFormulaWithLocals alloyFormulaWithLocals = null;

		for (int i = 0; i < children.size(); i++) {
			AlloyFormula f = children.get(i).getFormula();
			AlloyTyping t = children.get(i).getLocals();
			if (result == null) {
				result = f;
				locals = t;
			} else {
				AlloyFormula joinedF = new ChoiceJoiner(context.getMapping())
						.join(result, f);
				result = joinedF;
				locals = locals.merge(t);
				alloyFormulaWithLocals = new AlloyFormulaWithLocals(result,
						locals);
				if (context.getMapping() != null) {
					context.getMapping().addMapping(n.sub(i + 1),
							alloyFormulaWithLocals.getFormula());
				}
			}
		}
		return alloyFormulaWithLocals;
	}

	/**
	 * @return <code>null</code> if the formula is true
	 */
	public Object visit(InvokeAction n) {
		String aliasModuleId = n.getAliasModuleId();
		String actionId = n.getActionId();
		List<AlloyExpression> actualParams = n.getActualParameters();
		PredicateFormula formula = this.context.invokeAction(aliasModuleId,
				actionId, actualParams);
		AlloyFormulaWithLocals alloyFormulaWithLocals = new AlloyFormulaWithLocals(
				formula, new AlloyTyping());
		if (context.getMapping() != null) {
			context.getMapping().addMapping(n,
					alloyFormulaWithLocals.getFormula());
		}
		return alloyFormulaWithLocals;
	}

	public Object visit(Composition n) {
		Vector<AlloyFormulaWithLocals> children = (Vector<AlloyFormulaWithLocals>) super
				.visit(n);

		AlloyFormula result = null;
		AlloyTyping locals = null;
		AlloyFormulaWithLocals alloyFormulaWithLocals = null;
		for (int i = 0; i < children.size(); i++) {
			AlloyFormula f = children.get(i).getFormula();
			AlloyTyping l = children.get(i).getLocals();
			if (result == null) {
				result = f;
				locals = l;
			} else {
				AlloyFormula joinedF = new CompositionJoiner(
						context != null ? context.getMapping() : null).join(
						result, f);
				result = joinedF;
				locals = locals.merge(l);
				alloyFormulaWithLocals = new AlloyFormulaWithLocals(result,
						locals);
				if (context != null && context.getMapping() != null) {
					context.getMapping().addMapping(n.sub(i + 1),
							alloyFormulaWithLocals.getFormula());
				}
			}
		}
		return alloyFormulaWithLocals;
	}

	public Object visit(Closure n) {
		throw new UnsupportedOperationException("Closure not supported");
	}

	public Object visit(WhileProgram n) {
		throw new UnsupportedOperationException("WhileProgram not supported");
	}
	
	public Object visit(Skip n) {
		PredicateFormula truePredicate = new PredicateFormula(null,
				AlloyPredicates.TRUE_PRED_ID, Collections
						.<AlloyExpression> emptyList());

		AlloyFormulaWithLocals result = new AlloyFormulaWithLocals(
				truePredicate, new AlloyTyping());

		if (context != null && context.getMapping() != null) {
			context.getMapping().addMapping(n, result.getFormula());
		}

		return result;
	}

	public Object visit(TestPredicate n) {
		PredicateFormula predicateformula = VariableIndexer.addIdxsToPred(n
				.getPredicateFormula());
		AlloyFormula formula;
		if (n.getTestPredicateIsTrue() == false)
			formula = new NotFormula(predicateformula);
		else
			formula = predicateformula;

		AlloyFormulaWithLocals alloyFormulaWithLocals = new AlloyFormulaWithLocals(
				formula, new AlloyTyping());
		if (context != null && context.getMapping() != null) {
			context.getMapping().addMapping(n,
					alloyFormulaWithLocals.getFormula());
		}
		return alloyFormulaWithLocals;
	}

	/**
	 * Translates a program call. If the called program (by module, id and number of arguments)
	 * has no translation yet, translates it first, with a new {@link DynalloyXlatorVisitor} on
	 * the same context (no options, empty maps), switching the context to the call's module
	 * alias and back; that caches the program's predicate and registers its calling convention.
	 * The predicate's text is not used here: {@code DynalloyXlatorVisitor.visit(DynalloyModule)}
	 * finds it in the cache when it reaches the program. Then instantiates the convention with
	 * a fresh prefix for the program's locals ({@link SpecContext#invokeProgram}, which advances
	 * the context's counter) and records the call in the context's mapping.
	 *
	 * <p>Since a program counts as translated only once its translation is finished, a program
	 * that calls itself, directly or through others, makes this recurse until the stack
	 * overflows: recursion must already be unfolded (jDynAlloy's {@code RecursionUnfolderVisitor}
	 * does it for TACO).
	 *
	 * @param n the call
	 * @return the call of the program's predicate, with the renamed locals and their types
	 * @throws NullPointerException if the context has no program with that id and number of
	 *         parameters, or no mapping
	 */
	@Override
	public Object visit(InvokeProgram n) {
		String aliasModuleId = n.getAliasModuleId();
		String programId = n.getProgramId();

		List<AlloyExpression> actualParams = n.getActualParameters();

		if (!context.isAlreadyTranslated(aliasModuleId, programId, n.getActualParameters().size())) {
			ProgramDeclaration programDeclaration = context.getProgram(
					aliasModuleId, programId, n.getActualParameters().size());

			String contextModuleId = context.getCurrentModuleId();
			context.switchToAlias(aliasModuleId);
			String programPredicate = (String) programDeclaration
					.accept(new DynalloyXlatorVisitor(context, null, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
							new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false, null));
			context.switchToModule(contextModuleId);
		}
		AlloyFormulaWithLocals formulaWithLocals = context.invokeProgram(
				aliasModuleId, programId, actualParams);

		context.getMapping().addMapping(n, formulaWithLocals.getFormula());
		return formulaWithLocals;
	}

	private String programId;
	private int temporalVariableIndex;

	/**
	 * Stores the id of the program or assertion about to be translated and resets a counter.
	 * {@link DynalloyXlatorVisitor} calls it before each one. Unclear: neither the id nor the
	 * counter is read anywhere, so the call has no effect; whether it is still needed is an open
	 * question.
	 *
	 * @param _programId the id
	 */
	public void setNewProgramId(String _programId) {
		programId = _programId;
		temporalVariableIndex = 0;
	}

	@Override
	public Object visit(Assigment n) {
		EqualsFormula formula = this.context.assigment(n.getLeft(), n
				.getRight());
		AlloyFormulaWithLocals alloyFormulaWithLocals = new AlloyFormulaWithLocals(
				formula, new AlloyTyping());
		if (context.getMapping() != null) {
			context.getMapping().addMapping(n,
					alloyFormulaWithLocals.getFormula());
		}
		return alloyFormulaWithLocals;
	}

}
