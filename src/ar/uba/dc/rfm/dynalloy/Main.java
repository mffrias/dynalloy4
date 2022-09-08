package ar.uba.dc.rfm.dynalloy;

import java.io.IOException;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import ar.uba.dc.rfm.dynalloy.visualization.gui.DynAlloyVizGUI;

public class Main {

	private static final String GUI = "gui";
	private static final String VIZ = "viz";

	enum MODE {
		VIZ, CONSOLE
	}

	/**
	 * @param args
	 * @throws AssertionNotFound 
	 * @throws IOException 
	 * @throws TokenStreamException 
	 * @throws RecognitionException 
	 * @throws VizException 
	 */
	public static void main(String[] args) throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound, VizException {

		System.out.println("DynAlloy application");
		System.out.println("Main options :");
		System.out
				.println("  * viz : Start the DynAlloy Analyzer (backep by Alloy)");
		System.out.println("  * gui : Start the DynAlloy -> Alloy translator ");
		System.out
				.println("By default, the command line interface will be started.");
		System.out.println("");
		System.out.println("");

		MODE mode = MODE.CONSOLE;
		if ((args.length > 0) && (args[0].equals(VIZ)))
			mode = MODE.VIZ;

		if (mode.equals(MODE.VIZ)) {
			System.out.println("Starting DynAlloy Analyzer...");
			DynAlloyVizGUI.main(args);
		} else {
			System.out.println("Starting DynAlloy command line interface...");
			DynAlloyConsole.main(args);
		}
	}

}
