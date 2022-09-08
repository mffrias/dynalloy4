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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyCheckCommand;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyTypingPrinter;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.AlloyVariableComparator;
import ar.uba.dc.rfm.alloy.ast.AlloyAssertion;
import ar.uba.dc.rfm.alloy.ast.AlloyFact;
import ar.uba.dc.rfm.alloy.ast.AlloySig;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.AlloyVisitor;

public class AlloyPrinter extends AlloyVisitor {

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.formulaPrinter.setPrettyPrinting(prettyPrinting);
		this.prettyPrinting = prettyPrinting;
	}

	private static final String EXTENDS_KEYWORD = "extends";

	private static final String SIG_KEYWORD = "sig";

	private static final String ONE_KEYWORD = "one";

	private static final String ABSTRACT_KEYWORD = "abstract";

	private static final String NEXT_LINE_CHR = "\n";

	private boolean prettyPrinting = false;

	private FormulaPrinter formulaPrinter;

	public AlloyPrinter() {
		super(new FormulaPrinter());
		this.formulaPrinter = (FormulaPrinter) this.getFormulaVisitor();
	}

	@Override
	public Object visit(AlloyAssertion n) {
		Vector<String> v = (Vector<String>) super.visit(n);
		StringBuffer buff = new StringBuffer();
		buff.append("assert" + BLANK_CHR + n.getAssertionId() + "{");

		if (prettyPrinting)
			buff.append("\n");

		if (!n.getQuantifiedVariables().varSet().isEmpty()) {
			buff.append("all ");

			List<AlloyVariable> sortedList = new LinkedList<AlloyVariable>(n
					.getQuantifiedVariables().varSet());
			Collections.sort(sortedList, new AlloyVariableComparator());

			for (int i = 0; i < sortedList.size(); i++) {
				AlloyVariable alloyVariable = sortedList.get(i);
				String alloyVariableDecl = String.format("%s : %s",
						alloyVariable, n.getQuantifiedVariables().get(
								alloyVariable));
				if (i != 0) {
					buff.append(",");
					if (prettyPrinting) {
						buff.append("\n");
						buff.append("    ");
					}
				}
				buff.append(alloyVariableDecl);
			}
			buff.append(" | {");

			if (prettyPrinting)
				buff.append("\n");
		}
		String formulaString = v.get(0);

		if (prettyPrinting)
			formulaString = increaseIdentation(formulaString);

		buff.append(formulaString);

		if (!n.getQuantifiedVariables().varSet().isEmpty()) {
			buff.append("  }");
			if (prettyPrinting)
				buff.append("\n");
		}
		
		buff.append("}");


		
		return buff.toString();
	}

	private String printCheckCommand(String assertionId, AlloyCheckCommand chk) {
		StringBuffer buff = new StringBuffer();
		buff.append("check" + BLANK_CHR + assertionId + BLANK_CHR);
		boolean fstTime = true;
		for (String signatureId : sortedSet(chk.signatureSet())) {
			if (fstTime) {
				buff.append("for" + BLANK_CHR);
				fstTime = false;
			} else
				buff.append(", ");

			if (chk.isExactly(signatureId))
				buff.append("exactly" + BLANK_CHR);
			buff.append(printScope(chk.getSize(signatureId)) + BLANK_CHR
					+ signatureId);
		}
		buff.append(NEXT_LINE_CHR);
		return buff.toString();
	}

	private String printScope(int size) {
		if (size < 10)
			return "0" + size;
		else
			return new Integer(size).toString();
	}

	private SortedSet<String> sortedSet(Set<String> s) {
		return new TreeSet<String>(s);
	}

	private String printTyping(AlloyTyping t) {
		return new AlloyTypingPrinter().print(t);
	}

	private static final Character BLANK_CHR = new Character(' ');

	@Override
	public Object visit(AlloySig n) {
		StringBuffer buff = new StringBuffer();
		if (n.isAbstract())
			buff.append(ABSTRACT_KEYWORD + BLANK_CHR);
		if (n.isOne())
			buff.append(ONE_KEYWORD + BLANK_CHR);

		buff.append(SIG_KEYWORD + BLANK_CHR + n.getSignatureId() + BLANK_CHR);

		if (n.isExtension())
			buff.append(EXTENDS_KEYWORD + BLANK_CHR + n.getExtSigId()
					+ BLANK_CHR);

		buff.append("{");

		if (prettyPrinting)
			buff.append("\n");

		String signatureFields = printTyping(n.getFields());

		if (prettyPrinting && !n.getFields().isEmpty()) {
			signatureFields = signatureFields.replace(",", ",\n  ");
			signatureFields = "  " + signatureFields;
		}

		buff.append(signatureFields);

		if (prettyPrinting && !n.getFields().isEmpty())
			buff.append("\n");

		buff.append("}");
		buff.append(NEXT_LINE_CHR);

		return buff.toString();
	}

	@Override
	public Object visit(AlloyModule n) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(n.getAlloyStr());

		AlloyPrinter printer = new AlloyPrinter();
		printer.setPrettyPrinting(true);

		AlloySig qfSignature = n.getGlobalSig();
		if (qfSignature != null) {
			String strSignature = (String) qfSignature.accept(printer);
			buffer.append(strSignature);
		}

		for (AlloyFact fact : n.getFacts()) {
			String strFact = (String) fact.accept(printer);
			buffer.append("\n");
			buffer.append("\n");
			buffer.append(strFact);
		}

		for (AlloyAssertion alloyAssertion : n.getAssertions()) {
			String alloyAssertionStr = (String) alloyAssertion.accept(printer);

			buffer.append("\n");
			buffer.append("\n");
			buffer.append(alloyAssertionStr + "\n");
		}

		return buffer.toString();

	}

	@Override
	public Object visit(AlloyFact fact) {
		Vector<String> v = (Vector<String>) super.visit(fact);
		StringBuffer buff = new StringBuffer();
		buff.append("fact" + BLANK_CHR + "{");
		if (prettyPrinting)
			buff.append("\n");

		String formulaString = v.get(0);

		if (prettyPrinting)
			formulaString = increaseIdentation(formulaString);

		buff.append(formulaString);

		if (prettyPrinting)
			buff.append("\n");

		buff.append("}");
		return buff.toString();
	}

	public static String increaseIdentation(String string) {
		return increaseIdentation(string, 2);
	}

	public static String increaseIdentation(String string, int indentationSize) {
		StringBuffer buffer = new StringBuffer();
		String[] lines = string.split("\n");

		char[] chars = new char[indentationSize];
		Arrays.fill(chars, ' ');
		String indentation = new String(chars);

		if (lines.length == 1) {
			buffer.append(indentation + lines[0]);
		} else {
			for (String line : lines) {
				buffer.append(indentation + line + "\n");
			}
		}
		return buffer.toString();
	}

}
