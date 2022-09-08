package ar.uba.dc.rfm.dynalloy.plugin;

import ar.uba.dc.rfm.alloy.ast.AlloyModule;

public interface AlloyASTPlugin {

	public AlloyModule transform(AlloyModule alloyModule);
	
}
