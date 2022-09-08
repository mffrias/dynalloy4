package ar.uba.dc.rfm.dynalloy.dataflow;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.dynalloy.ArgumentException;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;

public class PointsToDataflowTests {
	
	private static String path = "test/ar/uba/dc/rfm/dynalloy/dataflow/"; 
	
//	@Test
//	public void simpleProgramTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
//		Map<String, Set<String>> notAliased = MainPointsTo.getDataflowPointsTo("SimpleProgramForFlow", path + "SimpleProgramForPointsTo.dals");
//		System.out.println(notAliased);
//		
//		assertTrue("Not yet", false);
//	}
	
//	@Test
//	public void complexProgramTest() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound, ArgumentException {
//		Map<String, Set<String>> notAliased = MainPointsTo.getDataflowPointsTo("CacheList_remove_0", "C:\\Users\\Bruno\\Documents\\Facu\\Tesis\\InitialResults\\DataflowPrograms\\" + "CList_rem.dals");
//		System.out.println(notAliased);
//		
//		assertTrue("Not yet", false);
//	}
}
