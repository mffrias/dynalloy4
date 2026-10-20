package ar.uba.dc.rfm.dynalloy.parser.splitter;

/**
 * A stretch of a {@link Document}, between two positions that are both included, with a kind.
 * {@link DocumentTokenizer} uses it for a token (with no kind), and {@link DocumentSplitter} for a
 * section, whose kind is one of {@link DynalloySections}; {@code DynalloyParser} parses or copies
 * {@link #getText()} by kind, and gives {@link #getFrom()} to the ANTLR parser to make positions
 * in the section absolute. The tokenizer and the splitter reuse one instance for every token or
 * section. No static state.
 */
public class DocumentSection {
	Position from, to;
	Document doc;
	String kind;

	public DocumentSection(Document doc, Position from, Position to) {
		this.doc = doc;
		this.from = from;
		this.to = to;
	}

	public DocumentSection() {
	}

	public Document getDoc() {
		return doc;
	}
	
	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public Position getFrom() {
		return from;
	}

	public Position getTo() {
		return to;
	}
	
	public void setFrom(Position from) {
		this.from = from;
	}
	
	public void setTo(Position to) {
		this.to = to;
	}

	public String getKind() {
		return kind;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * Returns the text from {@code from} to {@code to}, both included, with the lines joined by
	 * line breaks; comments are kept.
	 *
	 * @return the text
	 */
	public String getText() {
		if (from.getLineNumber() == to.getLineNumber()) {
			return doc.getLine(from.getLineNumber()).substring(from.offset - 1,
					to.offset);
		} else {
			StringBuilder buffer = new StringBuilder();
			
			// first line
			String firstLine = doc.getLine(from.getLineNumber()).substring(from.offset - 1);
			buffer.append(firstLine);
			
			// append complete lines between
			for(int i = from.getLineNumber() + 1; i < to.getLineNumber(); i++){
				buffer.append("\n");
				buffer.append(doc.getLine(i));				
			}
			
			// last line
			String lastLine = doc.getLine(to.getLineNumber()).substring(0, to.getOffset());
			buffer.append("\n");
			buffer.append(lastLine);
			return buffer.toString();
		}
	}
	
	@Override
	public String toString() {
		return getText();
	}
}
