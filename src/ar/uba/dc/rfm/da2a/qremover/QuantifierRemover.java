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
package ar.uba.dc.rfm.da2a.qremover;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.FormulaMutator;

public class QuantifierRemover {

	public static final String QF_SIGNATURE_ID = "QF";

	public QuantifierRemoverResult removeQuantifiers(AlloyAssertion assertion) {
		AlloyFormula gf = attachGlobalSigToVariables(assertion.getFormula(),
				QF_SIGNATURE_ID);
		List<AlloyFormula> assertionFormulas = (List<AlloyFormula>) gf
				.accept(new FormulaBreaker());
		return prepareGlobalSigSpec(assertion, assertionFormulas);
	}
	
	
	public QuantifierRemoverResult removeFactQuantifiers(AlloyAssertion fact) {
		AlloyFormula gf = attachGlobalSigToVariables(fact.getFormula(),
				QF_SIGNATURE_ID);
//		List<AlloyFormula> assertionFormulas = (List<AlloyFormula>) gf
//				.accept(new FormulaBreaker());
		return prepareFactGlobalSigSpec(new AlloyAssertion(fact.getAssertionId(),fact.getQuantifiedVariables(),gf));
	}
	
	
	
	private QuantifierRemoverResult prepareFactGlobalSigSpec(AlloyAssertion assertion) {

		AlloySig qfSignature = new AlloySig(false, true, QF_SIGNATURE_ID,
				assertion.getQuantifiedVariables(), null);

		String assertionId = assertion.getAssertionId();
		AlloyAssertion qfAssertion = new AlloyAssertion(assertionId, new AlloyTyping(), assertion.getFormula());
		
		return new QuantifierRemoverResult(qfSignature, new ArrayList<AlloyFact>(), qfAssertion);
	}


	private QuantifierRemoverResult prepareGlobalSigSpec(AlloyAssertion assertion,
			List<AlloyFormula> assertionFormulas) {

		AlloySig qfSignature = new AlloySig(false, true, QF_SIGNATURE_ID,
				assertion.getQuantifiedVariables(), null);

		List<AlloyFact> facts = new LinkedList<AlloyFact>();
		for (int i = 0; i < assertionFormulas.size() - 1; i++)
			facts.add(new AlloyFact(assertionFormulas.get(i)));

		String assertionId = assertion.getAssertionId();
		AlloyAssertion qfAssertion = new AlloyAssertion(assertionId , new AlloyTyping(), assertionFormulas
				.get(assertionFormulas.size() - 1));
		
		return new QuantifierRemoverResult(qfSignature, facts, qfAssertion);
	}

	
	
	
	public static class QuantifierRemoverResult {
		public QuantifierRemoverResult(AlloySig qfSignature,
				List<AlloyFact> qfFacts, AlloyAssertion qfAssertion) {
			super();
			this.qfSignature = qfSignature;
			this.qfFacts = qfFacts;
			this.qfAssertion = qfAssertion;
		}
		public AlloyAssertion qfAssertion;
		public List<AlloyFact> qfFacts;
		public AlloySig qfSignature;
	}
	
	private AlloyFormula attachGlobalSigToVariables(AlloyFormula f,
			String globalSigId) {
		return (AlloyFormula) f.accept(new FormulaMutator(
				new GlobalSigAttacher(globalSigId)));
	}
}
