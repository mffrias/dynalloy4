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

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;

public final class ActionDeclaration {

	public Object accept(IDynalloyVisitor visitor) {
		return visitor.visit(this);
	}

	private final String actionId;

	private final List<VariableId> formalParameters;

	private final PredicateFormula pre;

	private final PredicateFormula post;

	private final AlloyTyping typing;

	public ActionDeclaration(String _actionId, List<VariableId> _formalParameters,
			PredicateFormula _pre, PredicateFormula _pos, AlloyTyping t) {
		super();
		this.actionId = _actionId;
		this.formalParameters = _formalParameters;
		this.pre = _pre;
		this.post = _pos;
		this.typing = t;
	}

	public boolean equals(Object o) {
		if (o != null && o.getClass().equals(ActionDeclaration.class)) {
			ActionDeclaration that = (ActionDeclaration) o;
			return this.getActionId().equals(that.getActionId())
					&& this.formalParameters.equals(that.formalParameters)
					&& this.typing.equals(that.typing)
					&& this.pre.equals(that.pre) && this.post.equals(that.post);
		} else

			return false;
	}

	public int hashCode() {
		return getActionId().hashCode() + getFormalParameters().hashCode()
				+ getTyping().hashCode() + getPre().hashCode()
				+ getPost().hashCode();
	}

	public List<VariableId> getFormalParameters() {
		return formalParameters;
	}

	public PredicateFormula getPost() {
		return post;
	}

	public PredicateFormula getPre() {
		return pre;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("[");
		for (VariableId v : getFormalParameters()) {
			if (!buff.toString().endsWith("["))
				buff.append(",");
			buff.append(v.getString());
			buff.append(":");
			buff.append(typing.get(new AlloyVariable(v)));
		}
		buff.append("]");

		buff.append("[pre: ");
		buff.append(this.getPre().toString());

		buff.append(",post:");
		buff.append(this.getPost().toString());
		buff.append("]");

		return buff.toString();
	}

	public String getActionId() {
		return actionId;
	}

	public AlloyTyping getTyping() {
		return typing;
	}
}