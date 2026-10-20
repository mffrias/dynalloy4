package ar.uba.dc.rfm.dynalloy.parser.splitter;

/**
 * Cuts the text of a {@code .dals} file into sections, for {@code DynalloyParser}. The ANTLR
 * grammar has rules only for the DynAlloy parts (actions, programs, assertions) and for the Alloy
 * declarations that DynAlloy reads (module, open, signatures), so the rest (facts, functions,
 * predicates, commands, comments) has to be cut out and passed through as Alloy text. The parser
 * also makes the positions in a section absolute from the section's start. A section is made of
 * tokens ({@link DocumentTokenizer}, which keeps comments whole, so that braces in comments do not
 * count), and its first token gives its kind ({@link DynalloySections}) and where it ends:
 * <ul>
 * <li>{@code module} ({@code ALLOY_MODULE}) and {@code open} ({@code ALLOY_OPEN}): at the last
 *     token that ends on the keyword's line;</li>
 * <li>{@code action} or {@code act} ({@code DYNALLOY_ACTION}), {@code program} or {@code prog}
 *     ({@code DYNALLOY_PROGRAM}), {@code assertCorrectness} ({@code DYNALLOY_ASSERTION}),
 *     {@code sig}, {@code abstract} or {@code one} ({@code ALLOY_SIGNATURE}), and {@code pred}
 *     ({@code PRED_DECL}): at the right brace that balances the first left brace, or at the end of
 *     the text;</li>
 * <li>anything else ({@code ALLOY}): before the next token that is one of those keywords or
 *     {@code import}, except {@code one}, which may appear in the type of a parameter.</li>
 * </ul>
 * {@code import} starts a section like those of the second item, but with no kind (null), on which
 * {@code DynalloyParser} fails with a {@code NullPointerException}. Unclear: what {@code import}
 * sections are for (the method that recognizes the keyword is called {@code beginsDynalloyCheck}).
 * In Alloy text a keyword is recognized wherever it is a whole token, so the Alloy text must not
 * use the keywords as names. See docs/ARCHITECTURE.md ("Parsing"). No static state; one instance
 * per pass over a document.
 */
public class DocumentSplitter {

	private final Document doc;
	private final DocumentTokenizer lexer;
	private DocumentSection tokenBuffer;
	private boolean useTokenBufferValue;

	public DocumentSplitter(Document doc) {
		this.doc = doc;
		this.lexer = new DocumentTokenizer(doc);
		tokenBuffer = new DocumentSection();
		useTokenBufferValue = false;
	}

	/**
	 * Reads the next section into {@code section}: sets its document, kind and positions (see the
	 * class comment). After a {@code module}, {@code open} or Alloy text section it has read one
	 * token too many, which it keeps for the next call.
	 *
	 * @param section the section to fill; the caller reuses one instance
	 * @return false when there are no more sections
	 */
	public boolean next(DocumentSection section) {
		section.setDoc(doc);

		boolean n;

		if (!useTokenBufferValue) {
			n = lexer.next(tokenBuffer);
			if (n == false)
				return false;
		}
		useTokenBufferValue = false;

		if (beginsAlloyModule(tokenBuffer) || beginsAlloyOpen(tokenBuffer)) {
			section.setKind(getKind(tokenBuffer));
			section.setFrom(tokenBuffer.getFrom());
			section.setTo(tokenBuffer.getTo());
			int lineOfCheck = tokenBuffer.getFrom().getLineNumber();
			while ((n = lexer.next(tokenBuffer))
					&& tokenBuffer.getTo().getLineNumber() == lineOfCheck) {
				section.setTo(tokenBuffer.getTo());
			}
			if (!n)
				return true;
			useTokenBufferValue = true;
			return true;
		}
		if (isSplitKeyword(tokenBuffer)) {
			section.setKind(getKind(tokenBuffer));
			section.setFrom(tokenBuffer.getFrom());
			section.setTo(tokenBuffer.getTo());
			while ((n = lexer.next(tokenBuffer)) && !isLBrace(tokenBuffer)) {
				section.setTo(tokenBuffer.getTo());
			}
			if (!n)
				return true;
			section.setTo(tokenBuffer.getTo());
			int braceBalance = 1;
			while (braceBalance != 0) {
				n = lexer.next(tokenBuffer);
				if (!n)
					return true;
				section.setTo(tokenBuffer.getTo());
				if (isLBrace(tokenBuffer)) {
					braceBalance++;
				} else if (isRBrace(tokenBuffer)) {
					braceBalance--;
				}
			}
		} else {
			section.setKind(DynalloySections.ALLOY);
			section.setFrom(tokenBuffer.getFrom());
			section.setTo(tokenBuffer.getTo());
			while (lexer.next(tokenBuffer)) {
				// "one" may appear on relational parameter type declaration
				if (isSplitKeyword(tokenBuffer) && !tokenBuffer.getText().equals("one")) {
					useTokenBufferValue = true;
					return true;
				}
				section.setTo(tokenBuffer.getTo());
			}
		}
		return true;
	}

	private String getKind(DocumentSection tok) {
		if (beginsDynalloyAction(tok))
			return DynalloySections.DYNALLOY_ACTION;
		else if (beginsDynalloyProgram(tok))
			return DynalloySections.DYNALLOY_PROGRAM;
		else if (beginsDynalloyAssertion(tok)) {
			return DynalloySections.DYNALLOY_ASSERTION;
		} else if (beginsAlloySignature(tok)) {
			return DynalloySections.ALLOY_SIGNATURE;
		} else if (beginsAlloyModule(tok)) {
			return DynalloySections.ALLOY_MODULE;
		} else if (beginsPredicateDeclaration(tok)){
			return DynalloySections.PRED_DECL;
		} else if (beginsAlloyOpen(tok)) {
			return DynalloySections.ALLOY_OPEN;
		} else
			return null;
	}

	private boolean isRBrace(DocumentSection tok) {
		return tok.getText().equals("}");
	}

	private boolean isLBrace(DocumentSection tok) {
		return tok.getText().equals("{");
	}

	private boolean isSplitKeyword(DocumentSection tok) {
		return beginsDynalloyAction(tok) || beginsDynalloyProgram(tok)
				|| beginsDynalloyAssertion(tok) || beginsDynalloyCheck(tok)
				|| beginsAlloySignature(tok) || beginsAlloyOpen(tok)
				|| beginsAlloyModule(tok) || beginsPredicateDeclaration(tok);
	}
	
	private boolean beginsPredicateDeclaration(DocumentSection tok) {
		return tok.getText().equals("pred");
	}


	private boolean beginsAlloyOpen(DocumentSection tok) {
		return tok.getText().equals("open");
	}

	private boolean beginsDynalloyCheck(DocumentSection tok) {
		return tok.getText().equals("import");
	}

	private boolean beginsDynalloyAssertion(DocumentSection tok) {
		return tok.getText().equals("assertCorrectness");
	}

	private boolean beginsDynalloyProgram(DocumentSection tok) {
		return tok.getText().equals("program") || tok.getText().equals("prog");
	}

	private boolean beginsDynalloyAction(DocumentSection tok) {
		return tok.getText().equals("action") || tok.getText().equals("act");
	}

	private boolean beginsAlloySignature(DocumentSection tok) {
		return tok.getText().equals("sig") || tok.getText().equals("abstract") || tok.getText().equals("one");
	}

	private boolean beginsAlloyModule(DocumentSection tok) {
		return tok.getText().equals("module");
	}
}
