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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import antlr.RecognitionException;
import antlr.TokenStreamException;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.alloy.ast.formulas.AlloyFormula;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyASTPlugin;
import ar.uba.dc.rfm.dynalloy.plugin.AlloyStringPlugin;
import ar.uba.dc.rfm.dynalloy.plugin.DynAlloyASTPlugin;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

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

    public void addDynAlloyASTPlugin(DynAlloyASTPlugin plugin) {
		dynalloy_ast_plugins.add(plugin);
	}
	
	public void addAlloyASTPlugin(AlloyASTPlugin plugin) {
		alloy_ast_plugins.add(plugin);
	}

	public void addAlloyStringPlugin(AlloyStringPlugin plugin) {
		alloy_string_plugins.add(plugin);
	}

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

	public AlloyModule compile(String inputFilename, String outputFilename, DynAlloyOptions options,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInObjectInvariantsByModule,
			HashMap<String, AlloyTyping> varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram,
			HashMap<String, List<AlloyFormula>> predsComingFromArithmeticConstraintsInContractsByProgram,
			boolean translatingForStryker) throws RecognitionException, TokenStreamException, IOException,
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
		AlloyModule alloyAST = dynalloyToAlloyXlator.translateDynAlloyAST(dynalloyAST, options, 
				varsAndTheirTypesComingFromArithmeticConstraintsInContractsByProgram, 
				predsComingFromArithmeticConstraintsInContractsByProgram,
				varsAndTheirTypesComingFromArithmeticConstraintsInObjectInvariantsByModule,
				predsComingFromArithmeticConstraintsInObjectInvariantsByModule);
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

	public void writeFile(String outputFileName, String alloyModule) throws IOException {
		FileWriter writer = new FileWriter(outputFileName);
		writer.write(alloyModule);
		writer.close();
	}

	public SpecContext getSpecContext() {
		return specContext;
	}

}
