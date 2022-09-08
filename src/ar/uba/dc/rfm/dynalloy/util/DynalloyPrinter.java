package ar.uba.dc.rfm.dynalloy.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyTypingPrinter;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.util.FormulaPrinter;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.IDynalloyVisitor;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.ast.programs.Choice;
import ar.uba.dc.rfm.dynalloy.ast.programs.Closure;
import ar.uba.dc.rfm.dynalloy.ast.programs.Composition;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeAction;
import ar.uba.dc.rfm.dynalloy.ast.programs.InvokeProgram;
import ar.uba.dc.rfm.dynalloy.ast.programs.Skip;
import ar.uba.dc.rfm.dynalloy.ast.programs.TestPredicate;
import ar.uba.dc.rfm.dynalloy.ast.programs.WhileProgram;

public class DynalloyPrinter extends DfsProgramVisitor implements IDynalloyVisitor {

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.prettyPrinting = prettyPrinting;
	}

	private boolean prettyPrinting = false;

	@Override
	public Object visit(Assigment n) {
		Vector<String> ps = (Vector<String>) super.visit(n);
		String left = ps.get(0);
		String right = ps.get(1);
		return left + ":=" + right;
	}

	private String visitIfThenElse(TestPredicate conditionThenPredicate, DynalloyProgram thenProgram, DynalloyProgram elseProgram) {

		PredicateFormula conditionThenFormula = conditionThenPredicate.getPredicateFormula();

		String conditionStr = (String) conditionThenFormula.accept(this.getDfsFormulaVisitor());
		String thenStr = (String) thenProgram.accept(this);
		String elseStr = (String) elseProgram.accept(this);

		StringBuffer sb = new StringBuffer();
		sb.append("if " + conditionStr + " ");

		if (conditionThenPredicate.getLabel() != null) {
			String labelStr = conditionThenPredicate.getLabel().toString();
			sb.append(labelStr);
		}

		sb.append(" {\n");

		sb.append(increaseIdentation(thenStr) + "\n");
		sb.append("} else {\n");
		sb.append(increaseIdentation(elseStr) + "\n");
		sb.append("}\n");
		return sb.toString();
	}

	private String visitChoice(Choice c) {
		Vector<String> ps = (Vector<String>) super.visit(c);
		StringBuffer b = new StringBuffer();
		for (String p : ps) {
			if (!b.toString().isEmpty()) {
				b.append("+");
				b.append("\n");
			}
			b.append(increaseIdentation(paren(p)));
			b.append("\n");
		}
		return b.toString();
	}

	public enum DynalloyGrammar {
		C_LIKE, DYNAMIC_LOGIC_LIKE
	}

	private DynalloyGrammar grammar;

	public void setGrammar(DynalloyGrammar grammar) {
		this.grammar = grammar;
	}

	private FormulaPrinter formulaPrinter = new FormulaPrinter();

	private AlloyTypingPrinter typingPrinter = new AlloyTypingPrinter();

	public DynalloyPrinter() {
		super(new FormulaPrinter());
		this.grammar = DynalloyGrammar.DYNAMIC_LOGIC_LIKE;
	}

	private String paren(String s) {
		return "(" + s + ")";
	}

	@Override
	public Object visit(Choice c) {

		/*
		 * TestPredicate t1 = new TestPredicate(pred, positionFromAST()); r =
		 * new Composition(t1,r, positionFromAST());
		 * 
		 * TestPredicate t2 = new TestPredicate(pred, false, positionFromAST());
		 * other = new Composition(t2,other, positionFromAST());
		 * 
		 * r = new Choice(r,other, positionFromAST());
		 */

		switch (grammar) {
		case C_LIKE: {
			if (c.size() == 2)
				if (c.get(0) instanceof Composition)
					if (c.get(1) instanceof Composition) {
						Composition l = (Composition) c.get(0);
						Composition r = (Composition) c.get(1);
						if (l.get(0) instanceof TestPredicate)
							if (r.get(0) instanceof TestPredicate) {
								TestPredicate conditionThen = (TestPredicate) l.get(0);
								TestPredicate conditionElse = (TestPredicate) r.get(0);

								DynalloyProgram thenProgram = l.subSequence(1);
								DynalloyProgram elseProgram = r.subSequence(1);

								return visitIfThenElse(conditionThen, thenProgram, elseProgram);
							}
					}
			return visitChoice(c);
		}
		case DYNAMIC_LOGIC_LIKE: {
			return visitChoice(c);
		}
		default:
			throw new IllegalStateException("Unknown grammar:" + grammar);
		}
	}

	@Override
	public Object visit(Closure c) {
		Vector<String> ps = (Vector<String>) super.visit(c);

		switch (grammar) {
		case C_LIKE: {
			StringBuffer sb = new StringBuffer();
			sb.append("repeat {\n");
			sb.append(increaseIdentation(ps.get(0)) + "\n");
			sb.append("}\n");
			return sb.toString();
		}
		case DYNAMIC_LOGIC_LIKE:
			return paren(ps.get(0)) + "*";
		default:
			throw new IllegalStateException("Unknown grammar:" + grammar);
		}

	}

	@Override
	public Object visit(WhileProgram w) {
		Vector<String> ps = (Vector<String>) super.visit(w);

		StringBuffer sb = new StringBuffer();
		sb.append("while ");
		sb.append(ps.get(0));
		sb.append("do {\n");
		sb.append(increaseIdentation(ps.get(1)) + "\n");
		sb.append("}\n");
		return sb.toString();

	}

	@Override
	public Object visit(Composition c) {
		Vector<String> ps = (Vector<String>) super.visit(c);
		StringBuffer b = new StringBuffer();
		for (String p : ps) {
			if (!b.toString().isEmpty()) {
				b.append(";");
				b.append("\n");
			}
			String pStr;
			switch (grammar) {
			case C_LIKE:
				pStr = p;
				break;
			case DYNAMIC_LOGIC_LIKE:
				pStr = paren(p);
				break;
			default:
				pStr = p;
			}
			b.append(pStr);
		}
		return b.toString();
	}

	@Override
	public Object visit(InvokeAction u) {
		Vector<String> ps = (Vector<String>) super.visit(u);
		StringBuffer b = new StringBuffer();
		for (String p : ps) {
			if (!b.toString().isEmpty())
				b.append(",");
			b.append(p);
		}
		return (u.getAliasModuleId() == null ? "" : u.getAliasModuleId() + "/") + u.getActionId() + "[" + b.toString() + "]";
	}

	@Override
	public Object visit(InvokeProgram u) {
		Vector<String> ps = (Vector<String>) super.visit(u);
		StringBuffer b = new StringBuffer();
		for (String p : ps) {
			if (!b.toString().isEmpty())
				b.append(",");
			b.append(p);
		}
		return "call " + (u.getAliasModuleId() == null ? "" : u.getAliasModuleId() + "/") + u.getProgramId() + "[" + b.toString() + "]";
	}

	@Override
	public Object visit(Skip s) {
		return "skip";
	}

	@Override
	public Object visit(TestPredicate t) {
		Vector<String> ps = (Vector<String>) super.visit(t);

		switch (grammar) {
		case C_LIKE: {
			StringBuffer buff = new StringBuffer();
			buff.append("assume ");

			if (!t.getTestPredicateIsTrue())
				buff.append("NOT ");

			buff.append(ps.get(0).toString());

			if (t.getLabel() != null) {
				buff.append(" ");
				buff.append(t.getLabel().toString());
				buff.append(" ");
			}

			return buff.toString();
		}
		case DYNAMIC_LOGIC_LIKE:
			return "[" + ps.get(0) + "]?";
		default:
			throw new IllegalStateException("Unknown grammar:" + grammar);
		}
	}

	private List<AlloyVariable> toAlloyVariable(List<VariableId> formalParameters) {
		List<AlloyVariable> r = new LinkedList<AlloyVariable>();
		for (VariableId id : formalParameters) {
			r.add(new AlloyVariable(id));
		}
		return r;
	}

	public Object visit(ActionDeclaration a) {

		formulaPrinter.setPrettyPrinting(this.prettyPrinting);

		StringBuffer buff = new StringBuffer();
		buff.append("action " + a.getActionId());
		String parameterDecl = typingPrinter.print(a.getTyping(), toAlloyVariable(a.getFormalParameters()));

		if (prettyPrinting) {
			parameterDecl = parameterDecl.replace(",", ",\n  ");
			if (!a.getTyping().isEmpty())
				parameterDecl = "  " + parameterDecl + "\n";
		}

		buff.append("[\n");
		buff.append(parameterDecl + "]");
		buff.append("{" + "\n");

		buff.append("pre {\n");
		String preconditionString = (String) a.getPre().accept(formulaPrinter);
		buff.append(increaseIdentation(preconditionString));
		buff.append("}\n");
		buff.append("post {\n");
		String postconditionString = (String) a.getPost().accept(formulaPrinter);
		buff.append(increaseIdentation(postconditionString));
		buff.append("}\n");
		buff.append("}" + "\n");
		return buff.toString();
	}

	public Object visit(AssertionDeclaration n) {
		formulaPrinter.setPrettyPrinting(this.prettyPrinting);

		StringBuffer buff = new StringBuffer();
		buff.append("assertCorrectness " + n.getAssertionId() + "[\n");
		String parameterDecl = typingPrinter.print(n.getTyping());

		if (prettyPrinting) {
			parameterDecl = parameterDecl.replace(",", ",\n  ");
			if (!n.getTyping().isEmpty())
				parameterDecl = "  " + parameterDecl + "\n";
		}

		buff.append(parameterDecl);
		buff.append("]{\n");

		String preconditionString = (String) n.getPre().accept(formulaPrinter);
		buff.append("pre={\n");
		buff.append(increaseIdentation(preconditionString));
		buff.append("\n");
		buff.append("}\n");

		String programString = (String) n.getProgram().accept(this);
		buff.append("program={\n");
		buff.append(increaseIdentation(programString));
		buff.append("\n");
		buff.append("}\n");

		String postconditionString = (String) n.getPost().accept(formulaPrinter);
		buff.append("post={\n");
		buff.append(increaseIdentation(postconditionString));
		buff.append("\n");
		buff.append("}\n");

		buff.append("}\n");
		return buff.toString();
	}

	public Object visit(DynalloyModule module) {
		StringBuffer buff = new StringBuffer();
		buff.append(module.getAlloyStr());

		for (ActionDeclaration action : module.getActions())
			buff.append(action.accept(this) + "\n");

		for (ProgramDeclaration program : module.getPrograms())
			buff.append(program.accept(this) + "\n");

		for (AssertionDeclaration assertion : module.getAssertions())
			buff.append(assertion.accept(this) + "\n");

		return buff.toString();
	}

	public Object visit(ProgramDeclaration p) {
		StringBuffer buff = new StringBuffer();
		buff.append("program " + p.getProgramId() + "[");
		if (prettyPrinting)
			buff.append("\n");

		for (int i = 0; i < p.getParameters().size(); i++) {
			VariableId parameter = p.getParameters().get(i);

			if (i != 0) {
				buff.append(",");
				if (prettyPrinting)
					buff.append("\n");
			}

			String type = p.getParameterTyping().get(new AlloyVariable(parameter));
			String parameterDecl = parameter + ":" + type;

			if (prettyPrinting)
				parameterDecl = "  " + parameterDecl;

			buff.append(parameterDecl);
		}
		if (prettyPrinting)
			buff.append("\n");

		buff.append("] var [\n");

		StringBuffer sndBuff = new StringBuffer();
		for (int i = 0; i < p.getLocalVariables().size(); i++) {
			VariableId localVar = p.getLocalVariables().get(i);
			if (i != 0) {
				sndBuff.append(",");
				if (prettyPrinting)
					sndBuff.append("\n");
			}

			String type = p.getParameterTyping().get(new AlloyVariable(localVar));
			String localVarDecl = localVar + ":" + type;

			if (prettyPrinting)
				localVarDecl = "  " + localVarDecl;

			sndBuff.append(localVarDecl);
		}
		buff.append(sndBuff.toString());

		if (prettyPrinting)
			buff.append("\n");

		buff.append("]{" + "\n");

		String program = (String) p.getBody().accept(this);
		buff.append(increaseIdentation(program) + "\n");
		buff.append("}" + "\n");
		return buff.toString();
	}

	private static String increaseIdentation(String string) {
		StringBuffer buffer = new StringBuffer();
		String[] lines = string.split("\n");
		for (String line : lines) {
			buffer.append("   " + line + "\n");
		}
		return buffer.toString();
	}

	@Override
	public Object visit(OpenDeclaration n) {
		return "open " + n.getModuleId() + " as " + n.getModuleId();
	}

}
