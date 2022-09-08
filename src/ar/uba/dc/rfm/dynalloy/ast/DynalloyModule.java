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
package ar.uba.dc.rfm.dynalloy.ast;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.util.DynalloyPrinter;

public final class DynalloyModule {

	public Object accept(IDynalloyVisitor visitor) {
		return visitor.visit(this);
	}

	private final String moduleId;

	private final Set<OpenDeclaration> openDeclarations;

	private final String alloyStr;

	private final Set<ActionDeclaration> actions;

	private final Set<ProgramDeclaration> programs;

	private final Set<AssertionDeclaration> assertions;

	private AlloyTyping dynalloyFields;
	
	private AlloyTyping varsComingFromArithmeticConstraintsInObjectInvariants;
	
	public AlloyTyping getVarsComingFromArithmeticConstraintsInObjectInvariants(){
		return this.varsComingFromArithmeticConstraintsInObjectInvariants;
	}
	
	private List<AlloyFormula> predsComingFromArithmeticConstraintsInObjectInvariants;

	public List<AlloyFormula> getPredsComingFromArithmeticConstraintsInObjectInvariants(){
		return this.predsComingFromArithmeticConstraintsInObjectInvariants;
	}
	
	public DynalloyModule(String _moduleId, Set<OpenDeclaration> _imports,
			String _alloyStr, Set<ActionDeclaration> _actions, Set<ProgramDeclaration> _programs, 
			Set<AssertionDeclaration> _assertions, AlloyTyping varsFromArithInObjInvs,
			List<AlloyFormula> predsFromArithInObjInvs) {
		super();
		this.moduleId = _moduleId;
		this.openDeclarations = _imports;
		this.alloyStr = _alloyStr;
		this.actions = _actions;
		this.programs = _programs;
		this.assertions = _assertions;
		this.varsComingFromArithmeticConstraintsInObjectInvariants = varsFromArithInObjInvs;
		this.predsComingFromArithmeticConstraintsInObjectInvariants = predsFromArithInObjInvs;
	}

	public ActionDeclaration getAction(String actionId) {
		for (ActionDeclaration action : getActions()) {
			if (action.getActionId().equals(actionId))
				return action;
		}
		return null;
	}

	public ProgramDeclaration getProgram(String programId) {
		for (ProgramDeclaration program : getPrograms()) {
			if (program.getProgramId().equals(programId))
				return program;
		}
		return null;
	}

	public AssertionDeclaration getAssertion(String assertionId) {
		for (AssertionDeclaration assertion : getAssertions()) {
			if (assertion.getAssertionId().equals(assertionId))
				return assertion;
		}
		return null;
	}

	public Set<ActionDeclaration> getActions() {
		return actions;
	}

	public Set<AssertionDeclaration> getAssertions() {
		return assertions;
	}

	public String getAlloyStr() {
		return alloyStr;
	}

	@Override
	public String toString() {
		String string = (String) this.accept(new DynalloyPrinter());
		return string;
	}

	public Set<ProgramDeclaration> getPrograms() {
		return programs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result
				+ ((alloyStr == null) ? 0 : alloyStr.hashCode());
		result = prime * result
				+ ((assertions == null) ? 0 : assertions.hashCode());
		result = prime * result + ((openDeclarations == null) ? 0 : openDeclarations.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		result = prime * result
				+ ((programs == null) ? 0 : programs.hashCode());
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
		final DynalloyModule other = (DynalloyModule) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (alloyStr == null) {
			if (other.alloyStr != null)
				return false;
		} else if (!alloyStr.equals(other.alloyStr))
			return false;
		if (assertions == null) {
			if (other.assertions != null)
				return false;
		} else if (!assertions.equals(other.assertions))
			return false;
		if (openDeclarations == null) {
			if (other.openDeclarations != null)
				return false;
		} else if (!openDeclarations.equals(other.openDeclarations))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		if (programs == null) {
			if (other.programs != null)
				return false;
		} else if (!programs.equals(other.programs))
			return false;
		return true;
	}

	public String getModuleId() {
		return moduleId;
	}

	public Set<OpenDeclaration> getImports() {
		return openDeclarations;
	}

	public void setDynalloyFields(AlloyTyping allFields) {
		this.dynalloyFields = allFields;
	}

	public AlloyTyping getDynalloyFields() {
		return this.dynalloyFields;
		
	}
	
	
}
