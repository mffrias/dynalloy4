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
package ar.uba.dc.rfm.alloy.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;

public class AlloyBuffer {

	public void addFact(AlloyFormula f) {
		facts.add(f);
	}

	public AlloyModule toAlloyModule() {
		AlloyAssertion assertion = new AlloyAssertion(assertionId, quantifiedVars,
				assertionFormula);
		
		return new AlloyModule(alloyStr, new AlloySig(false, true, signatureId, fields, null),
				toFacts(facts), assertion);
	}

	private List<AlloyFact> toFacts(List<AlloyFormula> l) {
		List<AlloyFact> result = new LinkedList<AlloyFact>();
		for (AlloyFormula f : l) {
			result.add(new AlloyFact(f));
		}
		return result;
	}

	private String assertionId;

	private AlloyTyping quantifiedVars = new AlloyTyping();

	private AlloyFormula assertionFormula;

	private Set<AlloyCheckCommand> chkCmds = new HashSet<AlloyCheckCommand>();

	public void setAssertionId(String assertionId) {
		this.assertionId = assertionId;
	}

	public void setAssertion(AlloyFormula formula) {
		this.assertionFormula = formula;
	}

	public void addQuantifiedVar(AlloyVariable v, String t) {
		this.quantifiedVars.put(v, t);
	}

	private String alloyStr;

	private List<AlloyFormula> facts = new LinkedList<AlloyFormula>();

	private String signatureId;

	private AlloyTyping fields = new AlloyTyping();

	public void setCompilableA4Spec(String compilableA4Spec) {
		this.alloyStr = compilableA4Spec;
	}

	public void addAllFields(AlloyTyping _quantifiedVariables) {
		for (AlloyVariable v : _quantifiedVariables.varSet()) {
			fields.put(v, _quantifiedVariables.get(v));
		}
	}

	public void setGlobalSigId(String _signatureId) {
		signatureId = _signatureId;

	}

	public void setSignatureId(String _signatureId) {
		signatureId = _signatureId;
	}

	public void appendAlloyStr(String _alloyStr) {
		if (alloyStr==null)
			alloyStr = _alloyStr;
		else
			alloyStr = alloyStr + _alloyStr;
	}

	public void addAssertion(AlloyAssertion alloyAssertion) {
		// TODO Auto-generated method stub
		
	}
}
