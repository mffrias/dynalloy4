package ar.uba.dc.rfm.dynalloy.parser.splitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

/**
 * The text of a {@code .dals} file as a list of lines, for {@link DocumentTokenizer} and
 * {@link DocumentSplitter}; {@code DynalloyParser} loads one for each of its two passes. A
 * {@link Position} is 1-based: line 1 is the first line, offset 1 its first character. Line ends
 * are not characters of the document; {@link #getNext(Position)} skips them. No static state.
 */
public class Document {
	
	List<String> lines;
	
	public Document() {
		lines = new Vector<String>();
	}
	
	public void Load(String spec) throws IOException {
		Load(new StringReader(spec));
	}

	/**
	 * Replaces the lines of the document with those read from the reader, without their line
	 * terminators. Reads the reader to the end; does not close it.
	 *
	 * @param specReader the text
	 * @throws IOException if the reader fails
	 */
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
	
	/**
	 * Returns the position after {@code pos}: the next character of its line or, past the end of
	 * the line, the first character of the next line that is not empty. Past the end of the last
	 * line it stops, at a position that {@link #isEndOfDocument(Position)} recognizes.
	 *
	 * @param pos a position in the document
	 * @return a new position
	 */
	public Position getNext(Position pos) {
		pos = new Position(pos.getLineNumber(), pos.getOffset()+1);
		while (pos.getLineNumber() != getLinesCount() && isEndOfLine(pos)){
			pos = new Position(pos.getLineNumber()+1, 1);
		}
		return pos;
	}
}
