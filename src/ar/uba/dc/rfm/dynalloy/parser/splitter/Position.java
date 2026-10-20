package ar.uba.dc.rfm.dynalloy.parser.splitter;

/**
 * A position in a {@link Document}: a 1-based line number and a 1-based offset in the line (the
 * column). The splitter uses it for tokens and sections, and the ANTLR grammar for the positions
 * of the DynAlloy program constructs, made absolute with {@link #add(Position)}. No static state.
 */
public class Position {
	int offset;
	int lineNumber;

	public Position(int lineNumber, int offset) {
		this.lineNumber = lineNumber;
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Moves this position down by the lines that precede {@code other}, turning a position
	 * relative to a section that starts at {@code other} into one in the whole document. Only the
	 * line changes, not the offset, so on the first line of a section that does not start at
	 * offset 1 the offset stays relative to the section. Changes this position and returns it; the
	 * ANTLR grammar calls it with the start of the section it parses.
	 *
	 * @param other the start of the section, or null to leave the position as it is
	 * @return this position
	 */
	public Position add(Position other) {
		if (other != null) {
			this.lineNumber += other.lineNumber - 1;
		}
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position other = (Position) obj;
			return (this.lineNumber == other.lineNumber && this.offset == other.offset);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "[line:" + Integer.toString(lineNumber) + ",column:"
				+ Integer.toString(offset) + "]";
	}
}
