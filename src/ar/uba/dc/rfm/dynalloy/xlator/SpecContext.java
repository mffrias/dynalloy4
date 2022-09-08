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

	public void putCallingConvention(String moduleId, String callId, String typesSeq, CallingConvention callingConvention) {
		callingConventions.put(moduleId + "::" + callId + ":" + typesSeq, callingConvention);
	}

	public void switchToModule(String moduleId) {
		currentModuleId = moduleId;
	}

	public void switchToAlias(String aliasModuleId) {
		currentModuleId = resolveAlias(aliasModuleId);
	}

	public String getCurrentModuleId() {
		return currentModuleId;
	}

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

	public ProgramDeclaration getProgram(String aliasModuleId, String programId, int i) {
		String moduleId = resolveAlias(aliasModuleId);
		return allPrograms.get(moduleId + "::" + programId + ":" + i);
	}
	
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

	public void setFields(AlloyTyping fields) {
		this.allFields = fields;
	}

	public EqualsFormula assigment(ExprVariable left, AlloyExpression right) {
		List<AlloyExpression> actualParams = Arrays.<AlloyExpression> asList(new AlloyExpression[] { left, right });

		List<AlloyExpression> params = CallingConvention.ASSIGMENT_CALLING_CONVENTION.instantiate(actualParams);

		return new EqualsFormula(params.get(0), params.get(1));
	}

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

	public int getLocalVarIndex() {
		return localVarIndex;
	}

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

	public void putTranslation(String programId, String program, List<AlloyExpression> exprs, int i) {
		programCache.put(currentModuleId + "::" + programId + ":" + i, program);
		programParametersCache.put(currentModuleId + "::" + programId + ":" + i, expressionsToVariables(exprs));
	}

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
