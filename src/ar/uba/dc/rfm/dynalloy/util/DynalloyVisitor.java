package ar.uba.dc.rfm.dynalloy.util;

import java.util.Vector;

import ar.uba.dc.rfm.alloy.ast.formulas.FormulaVisitor;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.IDynalloyVisitor;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;

public class DynalloyVisitor implements IDynalloyVisitor {

	private final DfsProgramVisitor programVisitor;

	public DfsProgramVisitor getDfsProgramVisitor() {
		return programVisitor;
	}

	public DynalloyVisitor() {
		this(null);
	}

	public DynalloyVisitor(DfsProgramVisitor programVisitor) {
		super();
		this.programVisitor = programVisitor;
	}

	public Object visit(AssertionDeclaration n) {
		Vector<Object> result = new Vector<Object>();
		if (programVisitor != null) {
			FormulaVisitor formulaVisitor = programVisitor
					.getDfsFormulaVisitor();
			result.add(n.getPre().accept(formulaVisitor));
			result.add(n.getProgram().accept(programVisitor));
			result.add(n.getPost().accept(formulaVisitor));
		}
		return result;
	}

	public Object visit(DynalloyModule n) {
		Vector<Object> v = new Vector<Object>();

		for (OpenDeclaration open : n.getImports())
			v.add(open.accept(this));
		
		for (ActionDeclaration action : n.getActions())
			v.add(action.accept(this));

		for (ProgramDeclaration program : n.getPrograms())
			v.add(program.accept(this));

		for (AssertionDeclaration assertion : n.getAssertions())
			v.add(assertion.accept(this));
		return v;
	}

	public Object visit(ActionDeclaration n) {
		Vector<Object> v = new Vector<Object>();
		if (programVisitor != null) {
			v.add(n.getPre().accept(programVisitor.getDfsFormulaVisitor()));
			v.add(n.getPost().accept(programVisitor.getDfsFormulaVisitor()));
		}
		return v;
	}

	public Object visit(ProgramDeclaration p) {
		Vector<Object> v = new Vector<Object>();
		if (programVisitor != null)
			v.add(p.getBody().accept(programVisitor));
		return v;
	}

	@Override
	public Object visit(OpenDeclaration n) {
		return null;
	}

}
