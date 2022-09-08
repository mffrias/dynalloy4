package ar.uba.dc.rfm.dynalloy.plugin;

import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;

public interface DynAlloyASTPlugin {

	public DynalloyModule transform(DynalloyModule input);
}
