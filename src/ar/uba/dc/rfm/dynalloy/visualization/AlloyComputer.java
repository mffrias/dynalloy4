package ar.uba.dc.rfm.dynalloy.visualization;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import kodkod.engine.fol2sat.HigherOrderDeclException;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Computer;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.XMLNode;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4SolutionReader;

/** This object performs expression evaluation. */
public class AlloyComputer implements Computer {

	private String filename = null;

	private String mainname = null;

	public void setMainname(String mainname) {
		this.mainname = mainname;
	}
	
	public final String compute(final Object input) throws Exception {
		if (input instanceof File) {
			filename = ((File) input).getAbsolutePath();
			return "";
		}
		if (!(input instanceof String))
			return "";
		final String str = (String) input;
		if (str.trim().length() == 0)
			return ""; // Empty line
		Module root = null;
		A4Solution ans = null;
		try {
			Map<String, String> fc = new LinkedHashMap<String, String>();
			XMLNode x = new XMLNode(new File(filename));
			if (!x.is("alloy"))
				throw new Exception();

			root = CompUtil.parseEverything_fromFile(A4Reporter.NOP, fc,
					mainname, 1);
			ans = A4SolutionReader.read(root.getAllReachableSigs(), x);
			for (ExprVar a : ans.getAllAtoms()) {
				root.addGlobal(a.label, a);
			}
			for (ExprVar a : ans.getAllSkolems()) {
				root.addGlobal(a.label, a);
			}
		} catch (Throwable ex) {
			throw new ErrorFatal("Failed to read or parse the XML file.");
		}
		try {
			Expr e = CompUtil.parseOneExpression_fromString(root, str);
			return ans.eval(e).toString();
		} catch (HigherOrderDeclException ex) {
			throw new ErrorType(
					"Higher-order quantification is not allowed in the evaluator.");
		}
	}
}