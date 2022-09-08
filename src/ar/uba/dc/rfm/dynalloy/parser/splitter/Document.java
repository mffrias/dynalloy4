package ar.uba.dc.rfm.dynalloy.parser.splitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

public class Document {
	
	List<String> lines;
	
	public Document() {
		lines = new Vector<String>();
	}
	
	public void Load(String spec) throws IOException {
		Load(new StringReader(spec));
	}

	public void Load(Reader specReader) throws IOException {
		lines.clear();
		BufferedReader reader = new BufferedReader(specReader);
		String line;
		while ((line = reader.readLine()) != null){
			lines.add(line);
		}
	}

	public List<String> getLines() {
		return lines;
	}
	
	public String getLine(int lineNumber){
		return lines.get(lineNumber - 1);
	}

	public int getLinesCount() {
		return lines.size();
	}
	
	public char getCharAt(Position p){
		return getCharAt(p.getLineNumber(), p.getOffset());
	}

	public char getCharAt(int lineNumber, int offset) {
		return getLine(lineNumber).charAt(offset-1);
	}

	public boolean isLastCharOfLine(Position pos) {
		return pos.getOffset() == this.getLine(pos.getLineNumber()).length();
	}

	public boolean isEndOfLine(Position pos) {
		return pos.getOffset() > this.getLine(pos.getLineNumber()).length();
	}
	
	public boolean isEndOfDocument(Position pos) {
		return pos.getLineNumber() == getLinesCount() && isEndOfLine(pos);
	}
	
	public Position getNext(Position pos) {
		pos = new Position(pos.getLineNumber(), pos.getOffset()+1);
		while (pos.getLineNumber() != getLinesCount() && isEndOfLine(pos)){
			pos = new Position(pos.getLineNumber()+1, 1);
		}
		return pos;
	}
}
