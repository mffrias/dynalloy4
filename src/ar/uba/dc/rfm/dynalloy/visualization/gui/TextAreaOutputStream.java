package ar.uba.dc.rfm.dynalloy.visualization.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * Simple way to "print" to a JTextArea; just say
 * PrintStream out = new PrintStream(new TextAreaOutputStream(myTextArea));
 * Then out.println() et all will all appear in the TextArea.
 * From: http://javacook.darwinsys.com/new_recipes/14.9betterTextToTextArea.jsp
 */
public final class TextAreaOutputStream extends OutputStream {

	private final JTextArea textArea;
	private final StringBuilder sb = new StringBuilder();

	public TextAreaOutputStream(final JTextArea textArea) {
		this.textArea = textArea;
	}

    @Override
    public void flush(){ }
    
    @Override
    public void close(){ }

	@Override
	public void write(int b) throws IOException {

		if (b == '\r')
			return;
		
		if (b == '\n') {
			textArea.append(sb.toString());
			sb.setLength(0);
		}
		
		sb.append((char)b);
	}
}