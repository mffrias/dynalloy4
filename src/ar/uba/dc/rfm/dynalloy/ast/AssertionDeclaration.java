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

import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;

public final class AssertionDeclaration {

	/**
	 * Class Invariant:  forall x:variable in pre+post+program | x in assertionPs
	 * 
	 * Class Invariant:  typing.size()==assertionPs.size()
	 * 
	 * Class Invariant: forall x: variable 
	 */
	private final AlloyTyping params;

	private final PredicateFormula post;

	private final DynalloyProgram program;

	private final PredicateFormula pre;

	private final String assertionId;

	private boolean translatingForStryker = false;
	
	public AssertionDeclaration(String _assertionId, AlloyTyping _assertionPs,
			PredicateFormula _pre, DynalloyProgram _program,
			PredicateFormula _post) {
		super();
		this.assertionId = _assertionId;
		this.params = _assertionPs;
		this.pre = _pre;
		this.program = _program;
		this.post = _post;
	}

	public String getAssertionId() {
		return assertionId;
	}

	public PredicateFormula getPost() {
		return post;
	}

	public PredicateFormula getPre() {
		return pre;
	}

	public DynalloyProgram getProgram() {
		return program;
	}

	public Object accept(IDynalloyVisitor visitor) {
		return visitor.visit(this);
	}

	public AlloyTyping getTyping() {
		return params;
	}

	@Override
	protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("assert " + this.getAssertionId() + "{");
		buff.append("assertCorrectness[");
		buff.append(getTyping().toString() + "] {");
		buff.append("pre={" + getPre().toString() + "}");
		buff.append("program={" + getProgram().toString() + "}");
		buff.append("post={" + getPost().toString() + "}");
		buff.append("}");
		buff.append("}");
		return buff.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assertionId == null) ? 0 : assertionId.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((post == null) ? 0 : post.hashCode());
		result = prime * result + ((pre == null) ? 0 : pre.hashCode());
		result = prime * result + ((program == null) ? 0 : program.hashCode());
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
		final AssertionDeclaration other = (AssertionDeclaration) obj;
		if (assertionId == null) {
			if (other.assertionId != null)
				return false;
		} else if (!assertionId.equals(other.assertionId))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (post == null) {
			if (other.post != null)
				return false;
		} else if (!post.equals(other.post))
			return false;
		if (pre == null) {
			if (other.pre != null)
				return false;
		} else if (!pre.equals(other.pre))
			return false;
		if (program == null) {
			if (other.program != null)
				return false;
		} else if (!program.equals(other.program))
			return false;
		return true;
	}

}
