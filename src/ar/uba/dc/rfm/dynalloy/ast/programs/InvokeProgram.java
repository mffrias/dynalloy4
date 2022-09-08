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
package ar.uba.dc.rfm.dynalloy.ast.programs;

import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Position;

/**
 * @author jgaleotti
 *
 */
public final class InvokeProgram extends DynalloyProgram {

	private final String aliasModuleId;
	
	private final String programId;

	private final List<AlloyExpression> actualParameters;

	public InvokeProgram(String _aliasModuleId,
			String _atomicActionId, List<AlloyExpression> _actualParameters) {
		super();
		aliasModuleId = _aliasModuleId;
		actualParameters = _actualParameters;
		programId = _atomicActionId;
	}

	public InvokeProgram(String _aliasModuleId, String _atomicActionId,
			List<AlloyExpression> _actualParameters, Position pos) {
		this(_aliasModuleId, _atomicActionId, _actualParameters);
		setPosition(pos);
	}
	
	public List<AlloyExpression> getActualParameters() {
		return actualParameters;
	}

	public String getProgramId() {
		return programId;
	}

	@Override
	public Object accept(ProgramVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public String toString() {
		return "call " + aliasModuleId + "/" + programId + "[" + actualParameters.toString() + "]";
	}

	public static InvokeProgram buildInvokeProgram(String programId,
			AlloyExpression... ps) {
		List<AlloyExpression> l = new LinkedList<AlloyExpression>();
		for (int i = 0; i < ps.length; i++)
			l.add(ps[i]);
		return new InvokeProgram(null, programId, l);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((actualParameters == null) ? 0 : actualParameters.hashCode());
		result = prime * result
				+ ((aliasModuleId == null) ? 0 : aliasModuleId.hashCode());
		result = prime * result
				+ ((programId == null) ? 0 : programId.hashCode());
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
		final InvokeProgram other = (InvokeProgram) obj;
		if (actualParameters == null) {
			if (other.actualParameters != null)
				return false;
		} else if (!actualParameters.equals(other.actualParameters))
			return false;
		if (aliasModuleId == null) {
			if (other.aliasModuleId != null)
				return false;
		} else if (!aliasModuleId.equals(other.aliasModuleId))
			return false;
		if (programId == null) {
			if (other.programId != null)
				return false;
		} else if (!programId.equals(other.programId))
			return false;
		return true;
	}

	public String getAliasModuleId() {
		return aliasModuleId;
	}
}
