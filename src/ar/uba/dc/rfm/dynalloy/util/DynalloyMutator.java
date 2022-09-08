package ar.uba.dc.rfm.dynalloy.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.programs.DynalloyProgram;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class DynalloyMutator extends DynalloyVisitor {

	private DynAlloyAlloyMapping mapping;
	
	public DynalloyMutator(ProgramMutator programMutator) {
		super(programMutator);
	}
	
	public DynalloyMutator(ProgramMutator programMutator, DynAlloyAlloyMapping mapping) {
		this(programMutator);
		programMutator.setMapping(mapping);
	}

	public ProgramMutator getProgramMutator() {
		return (ProgramMutator) this.getDfsProgramVisitor();
	}

	@Override
	public Object visit(ActionDeclaration n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);
		PredicateFormula pre = (PredicateFormula) v.get(0);
		PredicateFormula post = (PredicateFormula) v.get(1);
		return new ActionDeclaration(n.getActionId(),
				new LinkedList<VariableId>(n.getFormalParameters()), pre, post,
				n.getTyping());
	}

	@Override
	public Object visit(AssertionDeclaration n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);
		PredicateFormula pre = (PredicateFormula) v.get(0);
		DynalloyProgram program = (DynalloyProgram) v.get(1);
		PredicateFormula post = (PredicateFormula) v.get(2);
		return new AssertionDeclaration(n.getAssertionId(), n.getTyping(), pre,
				program, post);
	}

	@Override
	public Object visit(DynalloyModule n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);
		Set<OpenDeclaration> imports = new HashSet<OpenDeclaration>();
		Set<ActionDeclaration> actions = new HashSet<ActionDeclaration>();
		Set<ProgramDeclaration> programs = new HashSet<ProgramDeclaration>();
		Set<AssertionDeclaration> assertions = new HashSet<AssertionDeclaration>();

		for (int i = 0; i < v.size(); i++) {
			Object child = v.get(i);
			if (child instanceof OpenDeclaration)
				imports.add((OpenDeclaration)child);
			else if (child instanceof ActionDeclaration)
				actions.add((ActionDeclaration) child);
			else if (child instanceof ProgramDeclaration)
				programs.add((ProgramDeclaration) child);
			else if (child instanceof AssertionDeclaration) {
				assertions.add((AssertionDeclaration) child);
			} else
				throw new IllegalArgumentException("Unknown child class:" + child.getClass().getName());
		}
		DynalloyModule dynalloyModule = new DynalloyModule(n.getModuleId(), imports, n.getAlloyStr(),
				actions, programs, assertions, new AlloyTyping(), new ArrayList<AlloyFormula>());
		dynalloyModule.setDynalloyFields(n.getDynalloyFields());
		return dynalloyModule;
	}

	@Override
	public Object visit(ProgramDeclaration n) {
		Vector<Object> v = (Vector<Object>) super.visit(n);
		DynalloyProgram p = (DynalloyProgram) v.get(0);
		return new ProgramDeclaration(n.getProgramId(),
				new LinkedList<VariableId>(n.getParameters()), n.getLocalVariables(), p, n
								.getParameterTyping(), n.getPredsFromArithInContracts(), n.getVarsFromArithInContracts());
	}

	@Override
	public Object visit(OpenDeclaration n) {
		return new OpenDeclaration(n.getModuleId(),n.getAliasModuleId());
	}

	public void setMapping(DynAlloyAlloyMapping mapping) {
		this.mapping = mapping;
	}

	public DynAlloyAlloyMapping getMapping() {
		return mapping;
	}

}
