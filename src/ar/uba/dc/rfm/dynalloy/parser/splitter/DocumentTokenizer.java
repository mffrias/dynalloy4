package ar.uba.dc.rfm.dynalloy.parser.splitter;



public class DocumentTokenizer {

	private final Document document;
	private Position currentPosition;
	
	public DocumentTokenizer(Document doc) {
		this.document = doc;
		this.currentPosition = null;
	}

	public boolean next(DocumentSection token) {
		token.setDoc(document);

		if (currentPosition == null)
			currentPosition = new Position(1,1);
		while (true){
			if (document.isEndOfDocument(currentPosition)){
				return false;
			}
			
			char c = document.getCharAt(currentPosition);
			Position c_pos = currentPosition;
			currentPosition = document.getNext(currentPosition);

			if (isWhite(c)) {
				continue;
			} else if (isWordChar(c)) {
				token.setFrom(c_pos);				
				while (aheadIsWordChar(c_pos)) {
					c_pos = document.getNext(c_pos);
				}
				token.setTo(c_pos);
				currentPosition = document.getNext(c_pos);
				return true;
				
			} else if ( isBrace(c) ){
				token.setFrom(c_pos);
				token.setTo(c_pos);
				return true;
			} else if (( c == '/' && aheadIs(c_pos, '/'))||( c == '-' && aheadIs(c_pos, '-'))) {
				token.setFrom(c_pos);
				Position endOfLine = new Position(c_pos.getLineNumber(), document.getLine(c_pos.getLineNumber()).length());
				token.setTo(endOfLine);				
				currentPosition = document.getNext(endOfLine);
				return true;
			} else if (c == '/' && aheadIs(c_pos, '*')) {
				token.setFrom(c_pos);

				Position endOfComment = document.getNext(c_pos);
				while (!document.isEndOfDocument(endOfComment) &&
						!(document.getCharAt(endOfComment) == '*' && 
						aheadIs(endOfComment, '/'))) {
					endOfComment = document.getNext(endOfComment);
				}
				
				if (!document.isEndOfDocument(endOfComment))
					endOfComment = document.getNext(endOfComment);
				else
					endOfComment = new Position(document.getLinesCount(), document.getLine(document.getLinesCount()).length());
				token.setTo(endOfComment);				
				currentPosition = document.getNext(endOfComment);
				return true;				
			} else {
				token.setFrom(c_pos);
				token.setTo(c_pos);
				return true;
			}
		}
	}

	private boolean aheadIsWordChar(Position pos) {
		return ( !document.isLastCharOfLine(pos) &&
				 isWordChar( document.getCharAt( pos.getLineNumber() , pos.getOffset() + 1 )));
	}

	private boolean aheadIs(Position pos, char c) {
		return ( !document.isLastCharOfLine(pos) &&
				 document.getCharAt( pos.getLineNumber() , pos.getOffset() + 1 ) == c );
	}

	private boolean isBrace(char c) {
		return c == '{' || c == '}';
	}

	private boolean isWordChar(char c) {
		return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_' || ('0' <= c && c<='9');
	}	

	private boolean isWhite(char c) {
		return c == ' ' || c == '\t' ;
	}

}
