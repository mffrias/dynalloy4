package ar.uba.dc.rfm.dynalloy.visualization.gui;

import java.util.EventListener;

public interface TraceSelectionListener extends EventListener {
	
	public void traceSelectionChanged(TraceSelectionEvent evt);

}
