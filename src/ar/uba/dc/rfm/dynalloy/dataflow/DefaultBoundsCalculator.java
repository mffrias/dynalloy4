package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.dataflow.CfgVarCollector.GlobalVariable;

public class DefaultBoundsCalculator {

	private BoundsFileParser parser;
	private CfgVarCollector varCollector;
	
	public DefaultBoundsCalculator(BoundsFileParser parser, CfgVarCollector varCollector) {
		this.parser = parser;
		this.varCollector = varCollector;
	}
	
	
	/* 
	 * Removes bounds that give no relevant information (i.e., the ones that have all the possible values)
	 */
	public Set<BoundedVariable> removeIrrelevantBounds(Set<BoundedVariable> bounds) {
		Set<BoundedVariable> relevantBounds = new LinkedHashSet<BoundedVariable>();
		
		HashMap<ExprVariable, String> varTypes = getVarTypes();
		for (BoundedVariable bv : bounds) {
			String type = varTypes.get(bv.getVariable());
			if (!bv.getBounds().equals(parser.getDefaultBoundsForType(type)))
				relevantBounds.add(bv);
		}
		
		return relevantBounds;
	}
	
	public Set<BoundedVariable> getBoundsComplement(Set<BoundedVariable> bounds) {
		Set<BoundedVariable> complement = new LinkedHashSet<BoundedVariable>();
		for (BoundedVariable bv : bounds) {
			BoundedVariable bvComplement = getBoundsComplement(bv);
			if (bvComplement.getBounds().size() != 0)
				complement.add(bvComplement);
		}
		return complement;
	}
	
	public BoundedVariable getBoundsComplement(BoundedVariable bv) {
		BoundedVariable bvComplement = new BoundedVariable(bv.getVariable());
		HashMap<ExprVariable, String> varTypes = getVarTypes();
		String bvType = varTypes.get(bv.getVariable());
		Set<Bound> allBounds = parser.getDefaultBoundsForType(bvType);
		for (Bound bound : allBounds)
			if (!bv.getBounds().contains(bound))
				bvComplement.getBounds().add(bound);
		
		return bvComplement;
	}
	
	public HashMap<ExprVariable, String> getVarTypes() {
		Set<GlobalVariable> globalVars = varCollector.getCfgVariables();
		HashMap<ExprVariable, String> varTypes = new LinkedHashMap<ExprVariable, String>();
		for (GlobalVariable gv : globalVars) {
			varTypes.put(gv.getVar(), gv.getType());
		}
		return varTypes;
	}
	
	public Set<Bound> getDefaultBoundsForType(String type) {
		return parser.getDefaultBoundsForType(type);
	}
}
