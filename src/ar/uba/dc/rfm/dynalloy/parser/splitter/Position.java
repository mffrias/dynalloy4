package ar.uba.dc.rfm.dynalloy.parser.splitter;

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
