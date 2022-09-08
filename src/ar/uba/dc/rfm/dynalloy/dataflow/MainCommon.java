package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.DynAlloyTranslator;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class MainCommon {
	
	private static String getAssertionToCheck(String dynalloyFilePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(dynalloyFilePath));
		String line;
		while((line = br.readLine()) != null) {
			if (line.contains("assertCorrectness ")) {
				String assertion = line.replace("assertCorrectness", "");
				assertion = assertion.replace("[", "");
				assertion = assertion.trim();
				return assertion;
			}
		}
		
		return null;
	}
	
	public static SpecContext translateModule(String dynalloyFilePath) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		DynalloyModule module = getModules(dynalloyFilePath);
		DynAlloyTranslator translator = new DynAlloyTranslator(false);
		
		DynAlloyOptions options = new DynAlloyOptions();
		options.setAssertionToCheck(getAssertionToCheck(dynalloyFilePath));
		options.setLoopUnroll(10);
		options.setRemoveQuantifier(true);
		options.setStrictUnroll(false);
		options.setRunAlloyAnalyzer(false);
		options.setBuildDynAlloyTrace(false);
		
		translator.translateDynAlloyAST(module, options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>());
		return translator.getSpecContext();
	}
	
	public static ProgramDeclaration getFirstProgram(String dynalloyFileName) throws IOException, RecognitionException, TokenStreamException, AssertionNotFound {
		DynalloyModule module = getModules(dynalloyFileName);
		return module.getPrograms().iterator().next();
	}
	
	public static DynalloyModule getModules(String dynalloyFileName) throws IOException, RecognitionException, TokenStreamException, AssertionNotFound {
		String file = readFile(dynalloyFileName);
		DynalloyParser parser = new DynalloyParser();
		return parser.parse(file);
	}
	
	public static String readFile(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		StringBuffer buffer = new StringBuffer();
		String line;
		while((line = br.readLine()) != null)
			buffer.append(line + "\n");
		
		return buffer.toString();
	}
}
