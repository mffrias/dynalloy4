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
package ar.uba.dc.rfm.dynalloy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

public class DynalloySpecBuffer {

	private StringBuffer compilableA4Spec;

	private Map<String, ActionDeclaration> actions;

	private Map<String, ProgramDeclaration> programs;

	private Map<String, AssertionDeclaration> assertions;

	public DynalloySpecBuffer() {
		compilableA4Spec = new StringBuffer();
		imports = new HashSet<OpenDeclaration>();
		actions = new HashMap<String, ActionDeclaration>();
		programs = new HashMap<String, ProgramDeclaration>();
		assertions = new HashMap<String, AssertionDeclaration>();
		predicateDecls = new HashMap<String, PredicateFormula>();
	}

	public void addAssertion(AssertionDeclaration _assertion) {
		assertions.put(_assertion.getAssertionId(), _assertion);
	}

	public void setDynalloyProgram(String assertionId, DynalloyProgram _p) {
		if (!assertions.containsKey(assertionId))
			throw new IllegalStateException();
		AssertionDeclaration oldAssertion = assertions.get(assertionId);
		AssertionDeclaration newAssertion = new AssertionDeclaration(
				oldAssertion.getAssertionId(), oldAssertion.getTyping(),
				oldAssertion.getPre(), _p, oldAssertion.getPost());
		assertions.put(assertionId, newAssertion);
	}

	public void clearActions() {
		actions.clear();
	}

	public void putAction(String actionId, ActionDeclaration body) {
		actions.put(actionId, body);
	}

	public void putAllActions(Map<String, ActionDeclaration> m) {
		actions.putAll(m);
	}

	public void setCompilableA4Spec(String _a4Spec) {
		compilableA4Spec = new StringBuffer();
		compilableA4Spec.append(_a4Spec);
	}

	
	private String moduleId;
	private Set<OpenDeclaration> imports;
	
	private Map<String, PredicateFormula> predicateDecls;
	
	public void addOpenDeclaration(OpenDeclaration openDeclaration) {
		imports.add(openDeclaration);
	}
	
	public void addPredicateDeclaration(PredicateFormula pf) {
		predicateDecls.put(pf.getPredicateId(), pf);
	}

	
	public void setModuleId(String _moduleId) {
		moduleId = _moduleId;
	}
	public DynalloyModule toDynalloySpec() {
		return new DynalloyModule(moduleId,
				imports,
				compilableA4Spec.toString(),
				new HashSet<ActionDeclaration>(actions.values()), 
				new HashSet<ProgramDeclaration>(programs.values()), 
				new HashSet<AssertionDeclaration>(assertions.values()),
				new AlloyTyping(), new ArrayList<AlloyFormula>());
	}

	public void putProgram(String programId, ProgramDeclaration program) {
		programs.put(programId, program);

	}

	public void appendAlloyStr(String text) {
		compilableA4Spec.append(text);
	}

}
