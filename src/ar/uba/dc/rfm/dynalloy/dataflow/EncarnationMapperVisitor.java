package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.ast.programs.Assigment;
import ar.uba.dc.rfm.dynalloy.util.DfsProgramVisitor;
import ar.uba.dc.rfm.dynalloy.visualization.DynAlloyAlloyMapping;

public class EncarnationMapperVisitor extends DfsProgramVisitor {
	private DynAlloyAlloyMapping mapping;
	private List<String> encarnations;
	
	public EncarnationMapperVisitor(DynAlloyAlloyMapping m) {
		mapping = m;
		encarnations = new LinkedList<String>();
	}
	
	@Override
	public Object visit(Assigment dynalloy) {
		AlloyFormula alloy = mapping.getAlloy(dynalloy);
		encarnations.add(dynalloy.toString() + " -> " + alloy.toString());
		return null;
	}
	
	public void DumpMapping(PrintStream s) {
		for (String str : encarnations) {
			s.println(str);
		}
	}
}
