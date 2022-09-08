package regr.new_grammar;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.DynAlloyCompiler;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyStringPlugin;

public class New_GrammarTest {

	@Test
	public void grammar_ext_test() throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound {
		DynAlloyCompiler c = new DynAlloyCompiler();
		DynAlloyOptions options = new DynAlloyOptions();
		options.setAssertionToCheck("grammarExtAssert");
		options.setLoopUnroll(3);
		options.setStrictUnroll(false);
		options.setRemoveQuantifier(false);
		options.setRunAlloyAnalyzer(false);
		options.setBuildDynAlloyTrace(false);

		AlloyModule alloyAST = c.compile("test/regr/new_grammar/grammar_ext.dals",
				"test/regr/new_grammar/grammar_ext.als", options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);

		// Print Alloy AST
		String optionsHeader = c.buildOptionsHeader(options);
		AlloyPrinter printer = new AlloyPrinter();
		String alloyStr = (String) alloyAST.accept(printer);
		String alloyStrWithHeader = optionsHeader + "\n" + alloyStr;

		// Apply AlloyString plugins
		for (AlloyStringPlugin string_plugin: c.getAlloyStringPlugins()) {
			alloyStrWithHeader = string_plugin.transform(alloyStrWithHeader);
		}

		// System.out.println(alloyStrWithHeader);
		// Write Alloy file
		c.writeFile("test/regr/new_grammar/grammar_ext.als", alloyStrWithHeader);

	
	}

	@Test
	public void new_gramma_test() throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound {
		DynAlloyCompiler c = new DynAlloyCompiler();
		DynAlloyOptions options = new DynAlloyOptions();
		
		options.setAssertionToCheck("newGrammarAssert");
		options.setLoopUnroll(3);
		options.setStrictUnroll(false);
		options.setRemoveQuantifier(false);
		options.setRunAlloyAnalyzer(false);
		options.setBuildDynAlloyTrace(false);
		
		AlloyModule alloyAST = c.compile("test/regr/new_grammar/new_grammar.dals",
				"test/regr/new_grammar/new_grammar.als", options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);
		
		// Print Alloy AST
		String optionsHeader = c.buildOptionsHeader(options);
		AlloyPrinter printer = new AlloyPrinter();
		String alloyStr = (String) alloyAST.accept(printer);
		String alloyStrWithHeader = optionsHeader + "\n" + alloyStr;

		// Apply AlloyString plugins
		for (AlloyStringPlugin string_plugin: c.getAlloyStringPlugins()) {
			alloyStrWithHeader = string_plugin.transform(alloyStrWithHeader);
		}

		// System.out.println(alloyStrWithHeader);
		// Write Alloy file
		c.writeFile("test/regr/new_grammar/new_grammar.als", alloyStrWithHeader);

	}

	@Test
	public void old_grammar_test() throws RecognitionException,
			TokenStreamException, IOException, AssertionNotFound {
		DynAlloyCompiler c = new DynAlloyCompiler();
		DynAlloyOptions options = new DynAlloyOptions();
		options.setAssertionToCheck(null);
		options.setLoopUnroll(3);
		options.setStrictUnroll(false);
		options.setRemoveQuantifier(false);
		options.setRunAlloyAnalyzer(false);
		options.setBuildDynAlloyTrace(false);
		
				
		c.compile("test/regr/new_grammar/old_grammar.dals",
				"test/regr/new_grammar/old_grammar.als", options, new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(),
				new HashMap<String, AlloyTyping>(), new HashMap<String, List<AlloyFormula>>(), false);

	}

}
