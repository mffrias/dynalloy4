/*
 * Dynalloy Translator
 * Copyright (c) 2007 Universidad de Buenos Aires
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */
package ar.uba.dc.rfm.dynalloy;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


import antlr.RecognitionException;
import antlr.TokenStreamException;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyASTPlugin;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyStringPlugin;
import ar.uba.dc.rfm.dynalloy.plugin.DynAlloyASTPlugin;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

/**
 * dynalloy4's entry point for translating a DynAlloy file into an Alloy file: it reads and parses
 * the {@code .dals} file ({@link DynalloyParser}), applies the registered plugins, translates the
 * module ({@link DynAlloyTranslator}) and writes the {@code .als} file. In TACO's pipeline it
 * comes after jDynAlloy, whose output TACO writes to {@code output/output.dals}, and before the
 * Alloy Analyzer ({@code analyzer.AlloyAnalyzer}): TACO's
 * {@code DynalloyToAlloyManager.process_dynalloy_module} creates one per translation, registers
 * TACO's plugins and calls {@link #compile}. In dynalloy4, {@link DynAlloyAnalyzer} and the
 * visualizer use it too, with empty maps, which fails on this branch (docs/KNOWN-LIMITATIONS.md,
 * "DynAlloy works only through TACO"). See docs/ARCHITECTURE.md ("Translation").
 *
 * <p>Static state: {@link #isCheckAndAfterRunSpec}, which keeps its value between translations in
 * one JVM; nothing in dynalloy4 resets it, and TACO sets it before every translation. The plugins
 * and the {@code SpecContext} belong to the instance; plugins stay registered for every later
 * {@code compile} on it.
 */
public class DynAlloyCompiler {

	private List<AlloyASTPlugin> alloy_ast_plugins = new LinkedList<AlloyASTPlugin>();

	private List<AlloyStringPlugin> alloy_string_plugins = new LinkedList<AlloyStringPlugin>();

	private List<DynAlloyASTPlugin>  dynalloy_ast_plugins = new LinkedList<DynAlloyASTPlugin>();

	private boolean translatingForStryker = false;
	
	private SpecContext specContext;

	public DynAlloyCompiler() {}
	
	public List<AlloyStringPlugin> getAlloyStringPlugins(){
		return this.alloy_string_plugins;
	}

    /**
     * Registers a plugin that {@link #compile} applies to the parsed DynAlloy module, before loop
     * unrolling and translation. Plugins run in the order of registration, each on the module the
     * previous one returned. TACO registers its scope inference and
     * {@code DynAlloyAppendCommandPlugin} (the {@code check} command) here.
     *
     * @param plugin the plugin
     */
    public void addDynAlloyASTPlugin(DynAlloyASTPlugin plugin) {
		dynalloy_ast_plugins.add(plugin);
	}
	
	/**
	 * Registers a plugin that {@link #compile} applies to the Alloy module of the translation,
	 * before printing it, in the order of registration. TACO registers its symmetry-breaking and
	 * tight-bounds plugins here.
	 *
	 * @param plugin the plugin
	 */
	public void addAlloyASTPlugin(AlloyASTPlugin plugin) {
		alloy_ast_plugins.add(plugin);
	}

	/**
	 * Registers a plugin that {@link #compile} applies to the printed Alloy text, the options
	 * header included, just before writing it, in the order of registration. TACO registers its
	 * Java-arithmetic plugins here. The module that {@code compile} returns does not reflect what
	 * these plugins change.
	 *
	 * @param plugin the plugin
	 */
	public void addAlloyStringPlugin(AlloyStringPlugin plugin) {
		alloy_string_plugins.add(plugin);
	}

	/**
	 * Static: whether to translate the assertion for computing a method's specified output:
	 * {@code pre => not post}, with the program's formula used only for the variables' indices.
	 * {@link #compile} copies it into {@link DynAlloyTranslator#isCheckAndAfterRunSpec}, which
	 * copies it into the flag of the same name of {@code DynalloyXlatorVisitor}. Only TACO sets it
	 * ({@code DynalloyToAlloyManager}, before every translation); it keeps its value for later
	 * translations in the same JVM. See docs/ARCHITECTURE.md ("What TACO passes in") and
	 * docs/OPEN-QUESTIONS.md (1 and 3).
	 */
	public static boolean isCheckAndAfterRunSpec = false;

	private String readFile(Reader reader) throws IOException {
		LineNumberReader lnreader = null;
		StringBuffer buff = new StringBuffer();
		try {
			lnreader = new LineNumberReader(reader);
			String line = "";
			while ((line = lnreader.readLine()) != null) {
				buff.append(line);
				buff.append("\n");
			}
		} finally {
			lnreader.close();
		}
		return buff.toString();
	}

	/**
	 * Translates a DynAlloy file into an Alloy file: reads and parses {@code inputFilename},
	 * applies the DynAlloy-AST plugins, translates the module with a new
	 * {@link DynAlloyTranslator} (loop unrolling, then {@code DynalloyXlatorVisitor}), applies the
	 * Alloy-AST plugins, prints the module after the comment of {@link #buildOptionsHeader},
	 * applies the string plugins and writes the result to {@code outputFilename}. Keeps the
	 * translation's {@code SpecContext} for {@link #getSpecContext()}. See docs/ARCHITECTURE.md
	 * ("Translation").
	 *
	 * <p>Side effects: copies the static {@link #isCheckAndAfterRunSpec} into
	 * {@code DynAlloyTranslator}; replaces {@code outputFilename}.
	 *
	 * <p>Assumes what TACO passes in: four maps that are not null, the invariant ones with an
	 * entry for the module under analysis ({@code options.getModuleUnderAnalysis()}), and an
	 * assertion id of the form {@code check_<program>}; with the empty maps that the console
	 * passes, the translation stops with a {@code NullPointerException}
	 * (docs/KNOWN-LIMITATIONS.md, "DynAlloy works only through TACO"). The module must have an
	 * assertion; only one is translated.
	 *
	 * @param inputFilename the DynAlloy file to read (TACO's {@code output/output.dals})
	 * @param outputFilename the Alloy file to write, replaced if it exists (TACO's
	 *        {@code output/output.als})
	 * @param options the options of the translation (see {@link DynAlloyOptions})
	 * @param varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule per module
	 *        id, the variables that come from arithmetic constraints in its object invariants,
	 *        with their types
	 * @param predsComingFromArithmeticConstraintsInObjectInvariantsByModule per module id, the
	 *        predicates on those variables
	 * @param varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram per program id,
	 *        the variables that come from arithmetic constraints in its contract, with their types
	 * @param predsComingFromArithmeticConstraintsInContractsByProgram per program id, the
	 *        predicates on those variables
	 * @param translatingForStryker true to negate the post, for Stryker's feedback checks:
	 *        {@code (pre and program) => not post}
	 * @param inputToFix only tested for null: when it is not null, the index range of the module's
	 *        variables in the invariant predicates of the post is widened (docs/ARCHITECTURE.md,
	 *        "What TACO passes in"; docs/OPEN-QUESTIONS.md, 2)
	 * @return the Alloy module after the Alloy-AST plugins, without the changes of the string
	 *         plugins; TACO does not use it
	 * @throws RecognitionException if a section of the DynAlloy text does not parse, with the line
	 *         number in the whole file
	 * @throws TokenStreamException if the lexer fails on the DynAlloy text
	 * @throws IOException if the input cannot be read or the output cannot be written
	 * @throws AssertionNotFound never: it is only declared (see {@link AssertionNotFound})
	 */
	public AlloyModule compile(String inputFilename, String outputFilename, DynAlloyOptions options,
							   HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
							   HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule,
							   HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
							   HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
							   boolean translatingForStryker, Object inputToFix) throws RecognitionException, TokenStreamException, IOException,
			AssertionNotFound {

		//REGIS: Place in which new DynAlloy may be included
		
		// Read DynAlloy file
		FileReader reader = new FileReader(inputFilename);
		String dynalloyStr = readFile(reader);

		// Parse AST
		DynalloyParser dynalloyParser = new DynalloyParser();
		DynalloyModule dynalloyAST = dynalloyParser.parse(dynalloyStr);

		// Apply DynAlloyAST plugins
		for (DynAlloyASTPlugin ast_plugin: this.dynalloy_ast_plugins) {
			dynalloyAST = ast_plugin.transform(dynalloyAST);
		}

		// Translate AST
		DynAlloyTranslator dynalloyToAlloyXlator = new DynAlloyTranslator(translatingForStryker);

		DynAlloyTranslator.isCheckAndAfterRunSpec = isCheckAndAfterRunSpec;

		AlloyModule alloyAST = dynalloyToAlloyXlator.translateDynAlloyAST(dynalloyAST, options, 
				varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
				predsComingFromArithmeticConstraintsInContractsByProgram,
				varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule, inputToFix);
		this.specContext = dynalloyToAlloyXlator.getSpecContext();
		
		// Apply AlloyAST plugins
		for (AlloyASTPlugin ast_plugin: this.alloy_ast_plugins) {
			alloyAST = ast_plugin.transform(alloyAST);
		}
		
		
		// Print Alloy AST
		String optionsHeader = buildOptionsHeader(options);
		AlloyPrinter printer = new AlloyPrinter();
		String alloyStr = (String) alloyAST.accept(printer);
		String alloyStrWithHeader = optionsHeader + "\n" + alloyStr;
				
		// Apply AlloyString plugins
		for (AlloyStringPlugin string_plugin: this.alloy_string_plugins) {
			alloyStrWithHeader = string_plugin.transform(alloyStrWithHeader);
		}
		
		// Write Alloy file
		writeFile(outputFilename, alloyStrWithHeader);
		
		return alloyAST;

	}

	/**
	 * Returns the comment that {@link #compile} puts at the top of the Alloy file, with the
	 * options {@code assertionId}, {@code loopUnroll}, {@code removeQuantifiers},
	 * {@code strictUnrolling} and {@code build_dynalloy_trace}, one per line.
	 * {@code DynAlloyAnalyzer} also calls it.
	 *
	 * @param options the options; not null
	 * @return the comment, ending with a line break
	 */
	public String buildOptionsHeader(DynAlloyOptions options) {
		StringBuffer buff = new StringBuffer();
		buff.append("/* \n");
		buff.append(" * DynAlloy translator options " + "\n");
		buff.append(" * --------------------------- " + "\n");
		buff.append(" * assertionId= " + (options.getAssertionId() == null ? "null" : options.getAssertionId()) + "\n");
		buff.append(" * loopUnroll= " + options.getLoopUnroll() + "\n");
		buff.append(" * removeQuantifiers= " + (options.getRemoveQuantifiers() ? "true" : "false") + "\n");
		buff.append(" * strictUnrolling= " + (options.getStrictUnrolling() ? "true" : "false") + "\n");
		buff.append(" * build_dynalloy_trace= " + (options.getBuildDynAlloyTrace() ? "true" : "false") + "\n");
		buff.append(" */ \n");

		return buff.toString();
	}

	/**
	 * Writes a text to a file, replacing the file if it exists, in the platform's default
	 * encoding. {@link #compile} and {@code DynAlloyAnalyzer} use it for the Alloy file.
	 *
	 * @param outputFileName the path of the file
	 * @param alloyModule the text
	 * @throws IOException if the file cannot be written
	 */
	public void writeFile(String outputFileName, String alloyModule) throws IOException {
		FileWriter writer = new FileWriter(outputFileName);
		writer.write(alloyModule);
		writer.close();
	}

	/**
	 * Returns the {@code SpecContext} of the last {@link #compile}: the actions and programs of
	 * the unrolled module, the fields of its signatures, and the mapping between DynAlloy programs
	 * and their Alloy formulas. TACO passes it to {@code JavaTraceStage}, and
	 * {@link DynAlloyAnalyzer} to {@code DynAlloySolutionBuilder}, to map a counterexample back to
	 * DynAlloy.
	 *
	 * @return the context, or null before the first {@code compile}
	 */
	public SpecContext getSpecContext() {
		return specContext;
	}

}
