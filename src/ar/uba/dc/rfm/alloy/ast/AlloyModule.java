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
package ar.uba.dc.rfm.alloy.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class AlloyModule {

	private final String alloyStr;

	private final List<AlloyFact> facts;

	private final AlloySig qfSig;

	private final Set<AlloyAssertion> assertions;

	public AlloyModule(String compilableA4Spec, AlloySig qfSig, List<AlloyFact> facts,
			Set<AlloyAssertion> assertions) {
		super();
		this.alloyStr = compilableA4Spec;
		this.facts = facts;
		this.qfSig = qfSig;
		this.assertions = assertions;
	}

	public Object accept(IAlloyVisitor v) {
		return v.visit(this);
	}

	public AlloyModule(String compilableA4Spec, AlloySig globalSig,
			List<AlloyFact> facts, AlloyAssertion assertion) {
		super();
		this.alloyStr = compilableA4Spec;
		this.qfSig = globalSig;
		this.facts = facts;
		this.assertions = Collections.<AlloyAssertion>singleton(assertion);
	}

	public AlloyModule(String compilableA4Spec, List<AlloyFact> facts,
			Set<AlloyAssertion> assertions) {
		this(compilableA4Spec, null, facts, assertions);
	}

	public AlloyAssertion getAssertion() {
		if (this.assertions.size()!=1)
			throw new IllegalStateException("number of assertions is " + this.assertions.size());
		return this.getAssertions().iterator().next();
	}
	
	public AlloyAssertion getAssertion(String assertionId) {
		for (AlloyAssertion assertion : getAssertions()) {
			if (assertion.getAssertionId().equals(assertionId))
				return assertion;
		}
		return null;
	}
	
	public Set<AlloyAssertion> getAssertions() {
		return assertions;
	}
	
	public String getAlloyStr() {
		return alloyStr;
	}

	public List<AlloyFact> getFacts() {
		return facts;
	}

	public AlloySig getGlobalSig() {
		return qfSig;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("spec[");
		buff.append(this.getAlloyStr() + ",");
		buff.append(this.getGlobalSig()+ ",");
		buff.append(this.getFacts()+ ",");
		buff.append(this.getAssertions());
		buff.append("]");
		return buff.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assertions == null) ? 0 : assertions.hashCode());
		result = prime
				* result
				+ ((alloyStr == null) ? 0 : alloyStr.hashCode());
		result = prime * result + ((facts == null) ? 0 : facts.hashCode());
		result = prime * result + ((qfSig == null) ? 0 : qfSig.hashCode());
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
		final AlloyModule other = (AlloyModule) obj;
		if (assertions == null) {
			if (other.assertions != null)
				return false;
		} else if (!assertions.equals(other.assertions))
			return false;
		if (alloyStr == null) {
			if (other.alloyStr != null)
				return false;
		} else if (!alloyStr.equals(other.alloyStr))
			return false;
		if (facts == null) {
			if (other.facts != null)
				return false;
		} else if (!facts.equals(other.facts))
			return false;
		if (qfSig == null) {
			if (other.qfSig != null)
				return false;
		} else if (!qfSig.equals(other.qfSig))
			return false;
		return true;
	}

}
