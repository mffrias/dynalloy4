package ar.uba.dc.rfm.dynalloy.parser.splitter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class DocumentSectionTest {

	@Test
	public void testTextSingleCompleteLine() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 1),
				new Position(1, 10));
		
		assertEquals("1234567890", sec.getText() );
	}

	@Test
	public void testTextSinglePrefixLine() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 1),
				new Position(1, 5));
		
		assertEquals("12345", sec.getText() );
	}

	@Test
	public void testTextSingleTrailingLine() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 5),
				new Position(1, 10));
		
		assertEquals("567890", sec.getText() );
	}

	@Test
	public void testTextSinglePartialLine() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 2),
				new Position(1, 7));
		
		assertEquals("234567", sec.getText() );
	}

	@Test
	public void testTextTwoCompleteLine() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 1),
				new Position(2, 6));
		
		assertEquals("1234567890\nabcdef", sec.getText() );
	}

	@Test
	public void testTextTwoPartialLines() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 4),
				new Position(2, 3));
		
		assertEquals("4567890\nabc", sec.getText() );
	}

	@Test
	public void testTextManyCompleteLines() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef", "qwerty"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 1),
				new Position(3, 6));
		
		assertEquals("1234567890\nabcdef\nqwerty", sec.getText() );
	}

	@Test
	public void testTextManyLines() throws Exception {
		Document doc = new Document();
		doc.getLines().addAll(Arrays.<String>asList("1234567890", "abcdef", "qwerty"));

		DocumentSection sec = new DocumentSection(doc, 
				new Position(1, 9),
				new Position(3, 1));
		
		assertEquals("90\nabcdef\nq", sec.getText() );
	}

}
