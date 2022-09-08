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

import java.util.HashSet;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

public final class ProgramDeclaration {

	public Object accept(IDynalloyVisitor visitor) {
		return visitor.visit(this);
	}
	
	
	private final String programId;

	private final List<VariableId> parameters;


	private final DynalloyProgram body;
	
	private final AlloyTyping typing;
	
	private final List<VariableId> localVariables;
	
	private final List<AlloyFormula> predsFromArithInContracts;
	
	private final AlloyTyping varsFromArithInContracts;
	
	public List<AlloyFormula> getPredsFromArithInContracts(){
		return this.predsFromArithInContracts;
	}
	

	public AlloyTyping getVarsFromArithInContracts(){
		return this.varsFromArithInContracts;
	}
	
	public AlloyTyping getParameterTyping() {
		return typing;
	}
	
	public ProgramDeclaration(String _programId, List<VariableId> _formalParameters,
			List<VariableId> _localVariables, DynalloyProgram _program, AlloyTyping _typing, List<AlloyFormula> preds,
			AlloyTyping alloyTyping) {
		super();
		this.programId = _programId;
		this.parameters = _formalParameters;
		this.body = _program;
		this.typing = _typing;
		this.localVariables = _localVariables;
		this.predsFromArithInContracts = preds;
		this.varsFromArithInContracts = alloyTyping;
	}

	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(ProgramDeclaration.class)) {
			ProgramDeclaration that = (ProgramDeclaration) o;
			return this.getProgramId().equals(that.getProgramId())
					&& this.parameters.equals(that.parameters)
					&& this.body.equals(that.body);
		} else

			return false;
	}

	public int hashCode() {
		return getProgramId().hashCode() + getParameters().hashCode()
				+ getBody().hashCode();
	}

	public List<VariableId> getParameters() {
		return parameters;
	}

	public DynalloyProgram getBody() {
		return body;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("program " + this.getProgramId());
		buff.append("[");
		for (VariableId v : getParameters()) {
			if (!buff.toString().endsWith("["))
				buff.append(",");
			buff.append(v.getString());
			buff.append(":");
			buff.append(getParameterTyping().get(new AlloyVariable(v)));
		}
		buff.append("]");
		buff.append("var [");
		for (VariableId v : getLocalVariables()) {
			if (!buff.toString().endsWith("["))
				buff.append(",");
			buff.append(v.getString());
			buff.append(":");
			buff.append(getParameterTyping().get(new AlloyVariable(v)));
		}
		buff.append("]");
		buff.append("{");
		buff.append(this.getBody().toString());
		buff.append("}");

		return buff.toString();
	}

	public String getProgramId() {
		return programId;
	}

	public List<VariableId> getLocalVariables() {
		return localVariables;
	}
}