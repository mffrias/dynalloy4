package ar.uba.dc.rfm.dynalloy.parser.splitter;

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
