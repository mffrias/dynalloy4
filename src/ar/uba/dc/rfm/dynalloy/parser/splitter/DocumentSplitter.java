package ar.uba.dc.rfm.dynalloy.parser.splitter;

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
