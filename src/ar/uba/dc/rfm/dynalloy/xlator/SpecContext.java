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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.EqualsFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

/**
 * What the translation of one DynAlloy module knows and remembers: the module's actions,
 * programs and import aliases; the calling convention ({@link CallingConvention}) and, for a
 * program, the predicate text and parameters of everything translated so far; the module
 * being translated; a counter for the prefixes of local variables; and the
 * {@code DynAlloyAlloyMapping} between DynAlloy nodes and Alloy formulas.
 *
 * <p>{@code DynAlloyTranslator.translateDynAlloyAST} creates one per translation, from the
 * module after loop unrolling, sets its fields and its mapping, and gives it to
 * {@link DynalloyXlatorVisitor}, which fills it as it translates. Afterwards
 * {@code DynAlloyCompiler.getSpecContext()} hands it to the trace builder
 * ({@code dynalloy.trace.DynAlloySolutionBuilder}, also through TACO's {@code JavaTraceStage}),
 * and {@code dynalloy.dataflow} reads it too.
 *
 * <p>Keys: programs, calling conventions and translations by
 * {@code <module>::<id>:<number of parameters>}, so overloads with different numbers of
 * parameters are told apart and overloads with the same number are not (TACO's
 * docs/KNOWN-LIMITATIONS.md, "The new method naming"); actions by {@code <module>::<id>} only,
 * so of two actions with the same id the context keeps the one loaded last, whatever their
 * parameters. A null alias means the current module. Any other alias {@code m} resolves,
 * through the module's {@code open} declarations, to the imported module's id, but only the
 * module's own actions and programs are loaded, so a call {@code m/a[...]} is not found.
 * TACO's input is one module whose calls have no alias.
 *
 * <p>No static state. The counter starts at 0 in each new context and is never reset, so the
 * prefixes {@code l0_}, {@code l1_}, ... are unique within one translation. Nothing is removed
 * from the caches.
 */
public class SpecContext {

	private Map<String, String> programCache;

	private Map<String, List<ExprVariable>> programParametersCache;

	private final Map<String, ActionDeclaration> allActions;

	private AlloyTyping allFields;

	private final Map<String, ProgramDeclaration> allPrograms;

	private final Map<String, String> allAliases;

	private String currentModuleId = null;

	private Map<String, CallingConvention> callingConventions = new HashMap<String, CallingConvention>();

	private DynAlloyAlloyMapping mapping;

	/**
	 * Registers the calling convention of an action or program under
	 * {@code <moduleId>::<callId>:<typesSeq>}, replacing any earlier one.
	 * {@link DynalloyXlatorVisitor} passes the number of parameters as {@code typesSeq}, which
	 * is what {@link #invokeAction} and {@link #invokeProgram} look up.
	 *
	 * @param moduleId the module of the action or program
	 * @param callId the action's or program's id
	 * @param typesSeq the number of parameters, as a string
	 * @param callingConvention the convention
	 */
	public void putCallingConvention(String moduleId, String callId, String typesSeq, CallingConvention callingConvention) {
		callingConventions.put(moduleId + "::" + callId + ":" + typesSeq, callingConvention);
	}

	/**
	 * Makes {@code moduleId} the current module, in which ids without an alias are looked up
	 * and new translations are stored. {@link DynalloyXlatorVisitor} switches to the module it
	 * translates; nothing switches back.
	 *
	 * @param moduleId the module's id
	 */
	public void switchToModule(String moduleId) {
		currentModuleId = moduleId;
	}

	/**
	 * Makes the module that {@code aliasModuleId} names in the current module the current one
	 * (null keeps the current module). {@code ProgramTranslator.visit(InvokeProgram)} uses it
	 * around the translation of a called program and then switches back with
	 * {@link #switchToModule}.
	 *
	 * @param aliasModuleId an alias of the current module's {@code open} declarations, or null
	 */
	public void switchToAlias(String aliasModuleId) {
		currentModuleId = resolveAlias(aliasModuleId);
	}

	public String getCurrentModuleId() {
		return currentModuleId;
	}

	/**
	 * Loads the import aliases, actions and programs of a module; nothing is translated yet,
	 * and there is no current module until {@link #switchToModule} is called.
	 *
	 * @param moduleAST the module, with its loops unrolled; its programs are the ones
	 *        translated when they are called
	 */
	public SpecContext(DynalloyModule moduleAST) {
		allActions = new HashMap<String, ActionDeclaration>();
		allPrograms = new HashMap<String, ProgramDeclaration>();
		allAliases = new HashMap<String, String>();
		programCache = new HashMap<String, String>();
		programParametersCache = new HashMap<String, List<ExprVariable>>();

		for (OpenDeclaration open : moduleAST.getImports())
			allAliases.put(moduleAST.getModuleId() + "::" + open.getAliasModuleId(), open.getModuleId());

		for (ActionDeclaration action : moduleAST.getActions())
			allActions.put(moduleAST.getModuleId() + "::" + action.getActionId(), action);
		
		for (ProgramDeclaration program : moduleAST.getPrograms()){
			allPrograms.put(moduleAST.getModuleId() + "::" + program.getProgramId() + ":" + program.getParameters().size(), program);
		}
	}

	public PredicateFormula getActionPre(String actionId) {
		return getActionPre(null, actionId);
	}

	public PredicateFormula getActionPre(String aliasModuleId, String actionId) {
		String moduleId = resolveAlias(aliasModuleId);
		if (!allActions.containsKey(moduleId + "::" + actionId))
			throw new IllegalArgumentException("action " + moduleId + "::" + actionId + " is undefined");
		return allActions.get(moduleId + "::" + actionId).getPre();
	}

	private String resolveAlias(String aliasModuleId) {
		if (aliasModuleId == null)
			return currentModuleId;
		else
			return allAliases.get(currentModuleId + "::" + aliasModuleId);
	}

	public List<VariableId> getFormalParams(String actionId) {
		return getFormalParams(null, actionId);
	}

	public List<VariableId> getFormalParams(String aliasModuleId, String actionId) {
		String moduleId = resolveAlias(aliasModuleId);
		if (!allActions.containsKey(moduleId + "::" + actionId))
			throw new IllegalArgumentException("action " + moduleId + "::" + actionId + " is undefined");

		return allActions.get(moduleId + "::" + actionId).getFormalParameters();
	}

	public PredicateFormula getActionPost(String actionId) {
		return getActionPost(null, actionId);
	}

	public PredicateFormula getActionPost(String aliasModuleId, String actionId) {
		String moduleId = resolveAlias(aliasModuleId);
		return allActions.get(moduleId + "::" + actionId).getPost();
	}

	/**
	 * Returns the program with this id and number of parameters.
	 *
	 * @param aliasModuleId the module's alias, or null for the current module
	 * @param programId the program's id
	 * @param i its number of parameters
	 * @return the program as loaded (unrolled), or null if there is none
	 */
	public ProgramDeclaration getProgram(String aliasModuleId, String programId, int i) {
		String moduleId = resolveAlias(aliasModuleId);
		return allPrograms.get(moduleId + "::" + programId + ":" + i);
	}
	
	/**
	 * Returns a program with this id, whatever its number of parameters: with overloads, the
	 * first one in the order of a {@code HashMap}, so not necessarily the one a call means. The
	 * trace builder ({@code dynalloy.trace.EvaluationVisitor}) and {@code dynalloy.dataflow}
	 * use it.
	 *
	 * @param aliasModuleId the module's alias, or null for the current module
	 * @param programId the program's id
	 * @return the program as loaded (unrolled), or null if there is none
	 */
	public ProgramDeclaration getProgram(String aliasModuleId, String programId) {
		String moduleId = resolveAlias(aliasModuleId);
		for (String key : allPrograms.keySet()){
			if ((moduleId + "::" + programId).equals(key.substring(0,key.lastIndexOf(':')))){
				return allPrograms.get(key);
			}
		}
		return null;
	}


	public AlloyTyping getFields() {
		if (allFields == null)
			throw new IllegalStateException("context fields not setted");
		return allFields;
	}

	/**
	 * Sets the module's fields. {@code DynAlloyTranslator} passes the {@code dynalloyFields}
	 * of the module it was given, before unrolling. Nothing reads them back:
	 * {@link #getFields()}, which throws an {@code IllegalStateException} while they are null,
	 * is not called anywhere.
	 *
	 * @param fields the fields' typing
	 */
	public void setFields(AlloyTyping fields) {
		this.allFields = fields;
	}

	/**
	 * Translates the assignment {@code left := right} into {@code left_1 = right_0}, with every
	 * variable of {@code right} at index 0
	 * ({@link CallingConvention#ASSIGMENT_CALLING_CONVENTION}).
	 *
	 * @param left the assigned variable, plain
	 * @param right the assigned expression, over plain variables
	 * @return a new equality
	 */
	public EqualsFormula assigment(ExprVariable left, AlloyExpression right) {
		List<AlloyExpression> actualParams = Arrays.<AlloyExpression> asList(new AlloyExpression[] { left, right });

		List<AlloyExpression> params = CallingConvention.ASSIGMENT_CALLING_CONVENTION.instantiate(actualParams);

		return new EqualsFormula(params.get(0), params.get(1));
	}

	/**
	 * Translates an action call into a call of the action's predicate, with the arguments of
	 * its calling convention ({@link CallingConvention#instantiate(List)}), and the alias as
	 * given. The convention is looked up under the alias's module, the id and the number of
	 * formal parameters of the current module's action with that id; so the action must have
	 * been translated already, and a call through an alias fails (see the class comment).
	 *
	 * @param aliasModuleId the module's alias, or null for the current module
	 * @param actionId the action's id
	 * @param actualParams the actual parameters, plain
	 * @return a new predicate call, with indices from 0 for the caller to shift
	 * @throws IllegalArgumentException if the alias does not resolve, or no action or calling
	 *         convention is found
	 */
	public PredicateFormula invokeAction(String aliasModuleId, String actionId, List<AlloyExpression> actualParams) {
		String moduleId = resolveAlias(aliasModuleId);
		if (moduleId == null)
			throw new IllegalArgumentException(("alias " + aliasModuleId + " cannot be resolved in context " + currentModuleId + "."));

		String numParams = "" + this.getFormalParams(actionId).size();
		if (!callingConventions.containsKey(moduleId + "::" + actionId + ":" + numParams))
			throw new IllegalArgumentException(("action " + moduleId + "::" + actionId + " is undefined"));

		CallingConvention callingConvention = callingConventions.get(moduleId + "::" + actionId  + ":" + numParams);
		List<AlloyExpression> parameters = callingConvention.instantiate(actualParams);
		return new PredicateFormula(aliasModuleId, actionId, parameters);
	}

	int localVarIndex = 0;

	/**
	 * Returns how many program calls have been translated in this context, which is also the
	 * n of the next prefix {@code l<n>_}. {@code dynalloy.dataflow} uses n - 1, the prefix of
	 * the last call.
	 *
	 * @return the counter
	 */
	public int getLocalVarIndex() {
		return localVarIndex;
	}

	/**
	 * Translates a program call into a call of the program's predicate: the actual parameters
	 * at the indices of the calling convention, then the program's local variables, renamed
	 * with the fresh prefix {@code l<n>_}, at theirs
	 * ({@link CallingConvention#instantiate(List, String)}). Side effect: increments the
	 * counter, so each call gets its own copies of the locals.
	 *
	 * @param aliasModuleId the module's alias, or null for the current module
	 * @param programId the program's id
	 * @param actualParams the actual parameters, plain; their number picks the overload
	 * @return the call, with indices from 0 for the caller to shift, and the renamed locals with
	 *         their types
	 * @throws IllegalArgumentException if the program has no calling convention yet (it is
	 *         translated first by {@code ProgramTranslator.visit(InvokeProgram)})
	 */
	public AlloyFormulaWithLocals invokeProgram(String aliasModuleId, String programId, List<AlloyExpression> actualParams) {

		String moduleId = resolveAlias(aliasModuleId);
		if (!callingConventions.containsKey(moduleId + "::" + programId + ":" + actualParams.size())) {
			throw new IllegalArgumentException(("program " + moduleId + "::" + programId + " is undefined"));
		}

		CallingConvention callingConvention = callingConventions.get(moduleId + "::" + programId + ":" + actualParams.size());

		String tempPrefix = String.format("l%s_", localVarIndex++);
		CallingConventionResult parameters = callingConvention.instantiate(actualParams, tempPrefix);
				
		AlloyFormula formula = new PredicateFormula(aliasModuleId, programId, parameters.getParameters());

		return new AlloyFormulaWithLocals(formula, parameters.getLocals());
	}

	public boolean isAlreadyTranslated(String aliasModuleId, String programId, int numArgs) {
		String moduleId = resolveAlias(aliasModuleId);
		return programCache.containsKey(moduleId + "::" + programId + ":" + numArgs);
	}

	public String getTranslation(String aliasModuleId, String programId, int numArgs) {
		String moduleId = resolveAlias(aliasModuleId);
		return programCache.get(moduleId + "::" + programId + ":" + numArgs);
	}

	/**
	 * Caches the translation of a program of the current module, under
	 * {@code <current module>::<programId>:<i>}, where {@link #isAlreadyTranslated} and
	 * {@link #getTranslation} find it, replacing any earlier one.
	 *
	 * @param programId the program's id
	 * @param program the program's {@code pred}, as text
	 * @param exprs the parameters of the predicate's header, which must all be variables
	 *        (a {@code ClassCastException} otherwise)
	 * @param i the program's number of parameters
	 */
	public void putTranslation(String programId, String program, List<AlloyExpression> exprs, int i) {
		programCache.put(currentModuleId + "::" + programId + ":" + i, program);
		programParametersCache.put(currentModuleId + "::" + programId + ":" + i, expressionsToVariables(exprs));
	}

	/**
	 * Looks up the parameters of a program's predicate in the cache that
	 * {@link #putTranslation} fills; the trace builder ({@code dynalloy.trace.EvaluationVisitor})
	 * and {@code dynalloy.dataflow.CfgBuilder} use it. As written it always returns null:
	 * {@link #putTranslation} stores them under {@code <module>::<id>:<number of parameters>},
	 * and this looks them up under {@code <module>::<id>}. Neither caller runs from TACO, whose
	 * trace option ({@code buildJavaTrace}) fails before a trace is built, nor from DynAlloy's own
	 * entry points; forced, the trace fails with a {@code NullPointerException} (checked by
	 * running it).
	 *
	 * @param aliasModuleId the module's alias, or null for the current module
	 * @param programId the program's id
	 * @return null (see above)
	 */
	public List<ExprVariable> getProgramParameters(String aliasModuleId, String programId) {
		String moduleId = resolveAlias(aliasModuleId);
		return programParametersCache.get(moduleId + "::" + programId);
	}

	private List<ExprVariable> expressionsToVariables(List<AlloyExpression> exprs) {
		List<ExprVariable> vars = new ArrayList<ExprVariable>();
		for (AlloyExpression expr : exprs) {
			vars.add((ExprVariable) expr);
		}
		return vars;
	}

	/**
	 * Returns null: a generated stub, which nothing calls. Unclear: what it was meant to
	 * return, and whether it is still needed.
	 *
	 * @return null
	 */
	public AlloyTyping getTemporalInvokeProgramTyping() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the mapping between DynAlloy and Alloy. 
	 * @return Mapping class
	 */
	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}

	/**
	 * Sets the DynAlloy to Alloy mapping to be used in this translation
	 * @param mapping
	 */
	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

}
