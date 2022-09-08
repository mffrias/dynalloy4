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
							new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false));
			context.switchToModule(contextModuleId);
		}
		AlloyFormulaWithLocals formulaWithLocals = context.invokeProgram(
				aliasModuleId, programId, actualParams);

		context.getMapping().addMapping(n, formulaWithLocals.getFormula());
		return formulaWithLocals;
	}

	private String programId;
	private int temporalVariableIndex;

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
