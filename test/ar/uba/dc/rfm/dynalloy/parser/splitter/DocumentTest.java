package ar.uba.dc.rfm.dynalloy.parser.splitter;

import static org.junit.Assert.*;
import org.junit.Test;


public class DocumentTest {
	
	@Test
	public void testLoadFromString() throws Exception {
		Document doc = new Document();
		doc.Load("1234\nqwerty");
		assertEquals(2, doc.getLinesCount());
		assertEquals("1234", doc.getLine(1));
		assertEquals("qwerty", doc.getLine(2));
	}

	@Test
	public void testLoadFromStringWithBlankLine() throws Exception {
		Document doc = new Document();
		doc.Load("1234\n\nqwerty");
		assertEquals(3, doc.getLinesCount());
		assertEquals("1234", doc.getLine(1));
		assertEquals("", doc.getLine(2));
		assertEquals("qwerty", doc.getLine(3));		
	}
	
	@Test
	public void testNextAtEndOfLineGoesToNextLine() throws Exception {
		Document doc = new Document();
		doc.Load("1\n2\n3");
		assertEquals(new Position(2,1), doc.getNext(new Position(1,1)));		
	}

	@Test
	public void testNextSkipBlankLines() throws Exception {
		Document doc = new Document();
		doc.Load("1\n\n\n 3");
		assertEquals(new Position(4,1), doc.getNext(new Position(1,1)));		
	}

	@Test
	public void testNextNotExceedLastLine() throws Exception {
		Document doc = new Document();
		doc.Load("1\n3");
		assertEquals(new Position(2,2), doc.getNext(new Position(2,1)));		
	}

}
