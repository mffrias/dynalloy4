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
package ar.uba.dc.rfm.dynalloy.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.ast.expressions.AlloyExpression;
import ar.uba.dc.rfm.alloy.ast.formulas.PredicateFormula;
import ar.uba.dc.rfm.alloy.parser.IAlloyExpressionParseContext;
import ar.uba.dc.rfm.dynalloy.ast.ActionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.AssertionDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.ast.OpenDeclaration;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.parser.splitter.Document;
import ar.uba.dc.rfm.dynalloy.parser.splitter.DocumentSection;
import ar.uba.dc.rfm.dynalloy.parser.splitter.DocumentSplitter;
import ar.uba.dc.rfm.dynalloy.parser.splitter.DynalloySections;
import ar.uba.dc.rfm.dynalloy.util.DynalloySpecBuffer;

/**
 * Parses the text of a {@code .dals} file into a {@code DynalloyModule}: the first step of
 * {@link ar.uba.dc.rfm.dynalloy.DynAlloyCompiler#compile}, its only caller besides
 * {@code dynalloy.dataflow.MainCommon} and the tests. {@link DocumentSplitter} cuts the text into
 * sections, and each section is handled by its kind ({@link DynalloySections}), in two passes over
 * the text:
 * <ul>
 * <li>first, the Alloy parts: {@code module}, {@code open} and signature sections are parsed with
 *     the ANTLR grammar ({@link DynAlloyANTLRParser}), for the module id, the open declarations
 *     and the fields of the signatures, and are also copied as text; {@code pred} sections and the
 *     other Alloy text are only copied;</li>
 * <li>second, the DynAlloy parts: actions, programs and {@code assertCorrectness} sections are
 *     parsed with the ANTLR grammar.</li>
 * </ul>
 * The copied text, in the order of the file and each section followed by a line break, becomes the
 * module's Alloy text, which the translation prints as it is. See docs/ARCHITECTURE.md
 * ("Parsing").
 *
 * <p>No static state of its own, but the grammar takes the positions of {@code if} and
 * {@code while} from the static stacks of {@link DynalloyAST}, so parses must not run
 * concurrently.
 */
public class DynalloyParser {

	private AlloyTyping allFields = null;

	/**
	 * Parses a DynAlloy text into a module (see the class comment). Actions are kept by id,
	 * programs by {@code <id>::<number of parameters>} and assertions by id: a later one with the
	 * same key replaces an earlier one, so programs with the same id and the same number of
	 * parameters collapse into the last one. The fields of every signature, typed
	 * {@code <signature>->(<type>)}, become the module's {@code getDynalloyFields()}. In each
	 * assertion, the variables whose names contain {@code SK_jml_pred_java_primitive} (from TACO's
	 * Java arithmetic) are made immutable, so the translation does not index them.
	 *
	 * @param dynalloySpecStr the whole text of the {@code .dals} file
	 * @return the module
	 * @throws RecognitionException if a section does not parse; the message is the original one,
	 *         the line number is that in the whole text
	 * @throws TokenStreamException if the lexer fails on a section
	 * @throws IOException not in practice: the text is read from a string
	 * @throws AssertionNotFound never: it is only declared
	 * @throws NullPointerException if the text has a section that starts with {@code import},
	 *         which the splitter gives no kind
	 */
	public DynalloyModule parse(String dynalloySpecStr) throws IOException, RecognitionException, TokenStreamException, AssertionNotFound {

		allFields = new AlloyTyping();

		DynalloySpecBuffer buffer = prepass(dynalloySpecStr);
		DynalloyModule dynalloyAST = parse(buffer, dynalloySpecStr);
		
		dynalloyAST.setDynalloyFields(this.allFields);
		return dynalloyAST;
	}

	private DynalloyModule parse(DynalloySpecBuffer buffer, String moduleStr) throws TokenStreamException, IOException, RecognitionException {
		Document document = new Document();
		document.Load(new StringReader(moduleStr));

		DocumentSplitter splitter = new DocumentSplitter(document);
		DocumentSection sectionBuffer = new DocumentSection();
		while (splitter.next(sectionBuffer)) {
			try {
				if (sectionBuffer.getKind().equals(DynalloySections.DYNALLOY_PROGRAM))
					parseProgram(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.DYNALLOY_ACTION))
					parseAction(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.DYNALLOY_ASSERTION))
					parseAssertion(sectionBuffer, buffer);

			} catch (RecognitionException e) {
				int lineNumber = e.getLine() + sectionBuffer.getFrom().getLineNumber() - 1;
				int columnNumber = e.getColumn() + sectionBuffer.getFrom().getOffset() - 1;
				throw new RecognitionException(e.getLocalizedMessage(),// TODO
						// CHECK
						e.getFilename(), lineNumber, columnNumber);
			}
		}

		return buffer.toDynalloySpec();
	}

	private DynalloySpecBuffer prepass(String moduleStr) throws TokenStreamException, IOException, RecognitionException {
		Document document = new Document();
		document.Load(new StringReader(moduleStr));

		DynalloySpecBuffer buffer = new DynalloySpecBuffer();

		DocumentSplitter splitter = new DocumentSplitter(document);
		DocumentSection sectionBuffer = new DocumentSection();
		while (splitter.next(sectionBuffer)) {
			try {

				if (sectionBuffer.getKind().equals(DynalloySections.ALLOY_SIGNATURE))
					parseSignature(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.ALLOY_MODULE))
					parseModule(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.ALLOY_OPEN))
					parseOpen(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.ALLOY))
					copyToCompilableA4(sectionBuffer, buffer);
				else if (sectionBuffer.getKind().equals(DynalloySections.PRED_DECL))
					parsePred(sectionBuffer, buffer);

			} catch (RecognitionException e) {
				int lineNumber = e.getLine() + sectionBuffer.getFrom().getLineNumber() - 1;
				int columnNumber = e.getColumn() + sectionBuffer.getFrom().getOffset() - 1;
				throw new RecognitionException(e.getLocalizedMessage(),// TODO
						// CHECK
						e.getFilename(), lineNumber, columnNumber);
			}
		}

		return buffer;
	}

	private void parseSignature(DocumentSection section, DynalloySpecBuffer buffer) throws IOException, RecognitionException, TokenStreamException {
		DynalloyLexer lexer = new DynalloyLexer(new StringReader(section.getText()));
		DynAlloyANTLRParser parser = new DynAlloyANTLRParser(lexer);
		parser.setInitialPosition(section.getFrom());
		ASTFactory factory = new ASTFactory();
		factory.setASTNodeClass(DynalloyAST.class);
		parser.setASTFactory(factory);
		parser.bindFields(allFields);
		parser.alloySignature();
		copyToCompilableA4(section, buffer);
	}

	private void parseModule(DocumentSection section, DynalloySpecBuffer buffer) throws IOException, RecognitionException, TokenStreamException {
		DynAlloyANTLRParser parser = initializeParser(section);
		String moduleId = parser.alloyModule();
		buffer.setModuleId(moduleId);
		copyToCompilableA4(section, buffer);
	}

	
	/*
		public final PredicateFormula  predicateFormula(
		IAlloyExpressionParseContext ctx
	) throws RecognitionException, TokenStreamException {
		PredicateFormula r;
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST predicateFormula_AST = null;
		Token  aliasModuleId = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST aliasModuleId_AST = null;
		Token  pred = null;
		ar.uba.dc.rfm.dynalloy.parser.DynalloyAST pred_AST = null;
		r = null; List<AlloyExpression> args = null;
	*/
	
	private void parsePred(DocumentSection section, DynalloySpecBuffer buffer) throws IOException, RecognitionException, TokenStreamException {
		buffer.appendAlloyStr(section.getText());
		buffer.appendAlloyStr("\n");
	}
		
	private void parseOpen(DocumentSection section, DynalloySpecBuffer buffer) throws IOException, RecognitionException, TokenStreamException {
		DynAlloyANTLRParser parser = initializeParser(section);
		OpenDeclaration openDeclaration = parser.alloyOpen();
		buffer.addOpenDeclaration(openDeclaration);
		copyToCompilableA4(section, buffer);
	}

	private void copyToCompilableA4(DocumentSection section, DynalloySpecBuffer buffer) throws IOException {
		buffer.appendAlloyStr(section.getText());
		buffer.appendAlloyStr("\n");
	}

	private void parseAssertion(DocumentSection section, DynalloySpecBuffer buffer) throws RecognitionException, TokenStreamException {
		DynAlloyANTLRParser parser = initializeParser(section);
		AssertionDeclaration parsed = parser.dynalloyAssertion();
		// Will make all the variables coming from arithmetic operations immutable
		for (AlloyVariable av : parsed.getTyping().getVarsInTyping()){
			if (av.getVariableId().getString().contains("SK_jml_pred_java_primitive")){
				av.setMutable(false);
			}
		}
		buffer.addAssertion(parsed);
	}

	private void parseAction(DocumentSection section, DynalloySpecBuffer buffer) throws RecognitionException, TokenStreamException {
		DynAlloyANTLRParser parser = initializeParser(section);
		ActionDeclaration action = parser.dynalloyAction();
		buffer.putAction(action.getActionId(), action);
	}

	private void parseProgram(DocumentSection section, DynalloySpecBuffer buffer) throws RecognitionException, TokenStreamException {
		DynAlloyANTLRParser parser = initializeParser(section);
		ProgramDeclaration parsed = parser.dynalloyProgram();
		//mfrias: previous key in buffer field was the program name. I will now use 
		//"programName::#args". In this way we accept program with same name, but different number of arguments.
		buffer.putProgram(parsed.getProgramId()+"::"+parsed.getParameters().size(), parsed);
	}

	private DynAlloyANTLRParser initializeParser(DocumentSection section) {
		DynalloyLexer lexer = new DynalloyLexer(new StringReader(section.getText()));
		DynAlloyANTLRParser parser = new DynAlloyANTLRParser(lexer);
		parser.setInitialPosition(section.getFrom());
		ASTFactory factory = new ASTFactory();
		factory.setASTNodeClass(DynalloyAST.class);
		parser.setASTFactory(factory);
		parser.bindFields(allFields);

		return parser;
	}
}
