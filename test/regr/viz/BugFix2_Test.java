package regr.viz;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.dynalloy.DynAlloyAnalyzer;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.trace.DynAlloySolution;
import ar.uba.dc.rfm.dynalloy.visualization.AlloyCommand;
import ar.uba.dc.rfm.dynalloy.visualization.VizException;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;

public class BugFix2_Test {

	@Test
	public void bugfix_viz_2() throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound, VizException {

		DynAlloyOptions options = new DynAlloyOptions();
		options.setRemoveQuantifier(false);
		options.setLoopUnroll(3);
		options.setStrictUnroll(false);
		options.setRunAlloyAnalyzer(true);
		options.alloyOptions = new A4Options();

		DynAlloyAnalyzer analyzer = new DynAlloyAnalyzer();
		File dynalloy_file = new File("test/regr/viz/bugfix_viz_2.dals");
		List<AlloyCommand> commands = analyzer
				.getAvailableCommands(dynalloy_file);
		for (AlloyCommand cmd : commands) {
			DynAlloySolution result = analyzer.analyzeCommand(dynalloy_file,
					cmd, options);
			assertTrue(result.isUNSAT());
		}

	}

}
