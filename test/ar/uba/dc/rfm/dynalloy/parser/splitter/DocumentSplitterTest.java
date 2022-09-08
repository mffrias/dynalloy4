package ar.uba.dc.rfm.dynalloy.parser.splitter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;


public class DocumentSplitterTest {

	@Test
	public void testAlloySingleLineComment() throws Exception {
		DocumentSplitter ds = split("// hola manola");
		List<String> expected = Arrays.<String>asList(DynalloySections.ALLOY,"// hola manola");
		assertSections(expected, ds);		
	}
	
	@Test
	public void testAlloySingleLineCommentDashDash() throws Exception {
		DocumentSplitter ds = split("-- hola manola");
		List<String> expected = Arrays.<String>asList(DynalloySections.ALLOY,"-- hola manola");
		assertSections(expected, ds);		
	}

	@Test
	public void testAlloyMultipleComments() throws Exception {
		DocumentSplitter ds = split("/* hola \n manola*///hi");
		List<String> expected = Arrays.<String>asList(DynalloySections.ALLOY,"/* hola \n manola*///hi");
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyAction() throws Exception {
		String actionDeclaration = "action Act1[a:A, b:B] {\n  pre = { (all t:T) | a = t } \n  post = { some_other func[a,b.c] < (d) }\n}";
		DocumentSplitter ds = split(actionDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_ACTION,actionDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void AlloyPredicateAndDynAlloyActions() throws Exception {
		String alloyTruePred = "pred TruePred[] { univ = univ }";
		String actionDeclaration = "action Act1[a:A, b:B] {\n  pre = { (all t:T) | a = t } \n  post = { some_other func[a,b.c] < (d) }\n}";
		String alloyFalsePred = "pred FalsePred[] { univ != univ }";
		DocumentSplitter ds = split(alloyTruePred + "\n" + actionDeclaration + "\n" + alloyFalsePred);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY,alloyTruePred,
				DynalloySections.DYNALLOY_ACTION,actionDeclaration,
				DynalloySections.ALLOY,alloyFalsePred);
		assertSections(expected, ds);		
	}

	@Test
	public void AlloyPredicateAndDynAlloyAct() throws Exception {
		String alloyTruePred = "pred TruePred[] { univ = univ }";
		String actionDeclaration = "act Act1[a:A, b:B] {\n  pre = { (all t:T) | a = t } \n  post = { some_other func[a,b.c] < (d) }\n}";
		String alloyFalsePred = "pred FalsePred[] { univ != univ }";
		DocumentSplitter ds = split(alloyTruePred + "\n" + actionDeclaration + "\n" + alloyFalsePred);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY,alloyTruePred,
				DynalloySections.DYNALLOY_ACTION,actionDeclaration,
				DynalloySections.ALLOY,alloyFalsePred);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyActionWrongClosed1() throws Exception {
		String actionDeclaration = "action Act1[a:A, b:B] {\n  pre";
		DocumentSplitter ds = split(actionDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_ACTION,actionDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyActionWrongClosed2() throws Exception {
		String actionDeclaration = "action Act1[a:A, b:B] {";
		DocumentSplitter ds = split(actionDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_ACTION,actionDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyActionWrongClosed3() throws Exception {
		String actionDeclaration = "action Act1[a:A, b:B]";
		DocumentSplitter ds = split(actionDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_ACTION,actionDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyProgram() throws Exception {
		String progDeclaration = "program Prg1[a:A, b:B] {\n act1[a,b];act2[a,b]+act1[a,b]*;skip  \n}";
		DocumentSplitter ds = split(progDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_PROGRAM,progDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyProg() throws Exception {
		String progDeclaration = "prog Prg1[a:A, b:B] {\n act1[a,b];act2[a,b]+act1[a,b]*;skip // } \n}";
		DocumentSplitter ds = split(progDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_PROGRAM,progDeclaration);
		assertSections(expected, ds);		
	}
	
	@Test
	public void DynalloyProgDashCommentHiddingBrace() throws Exception {
		String progDeclaration = "prog Prg1[a:A, b:B] {\n act1[a,b];act2[a,b]+act1[a,b]*;skip -- } \n}";
		DocumentSplitter ds = split(progDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_PROGRAM,progDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void DynalloyAssert() throws Exception {
		String assertDeclaration = "assertCorrectness amgas[v0: List] { \n pre = { ... } \n program = { test8[]; } \n post = { amgasPost... } \n } ";
		DocumentSplitter ds = split(assertDeclaration);
		List<String> expected = Arrays.<String>asList(DynalloySections.DYNALLOY_ASSERTION,assertDeclaration);
		assertSections(expected, ds);		
	}

	@Test
	public void testRealDynalloySpec() throws Exception {
		String module = "module removeAll";
		String sig1 = "abstract sig BooleanValue    {   }";
		String sig2 = "sig FalseValue  extends BooleanValue   {   }";
		String sig3 = "one sig TrueValue  extends BooleanValue   {   }";
		String sig4 = "one sig NullValue {   }";
		String sig5 = "one sig List {\n  H0: NullValue+Node // representa el campo \"head\"(v8) en el momento inicial\n}\n";
		String pred1 = "pred test5Pre[n: univ] {\n  n = NullValue\n}\n\npred TruePred[] {}";
		String act1 ="act init[v0: List, v1: Node+NullValue, v2: Node+NullValue, v3:  Char, v4: Char, v5: Char, v6: Node -> one Char, v7: Node -> one (Node+NullValue), v8: List -> one (Node+NullValue)] {\n   pre { TruePred[] }\n   post { TruePred[] }\n}";
		String alloy2 = "pred state43 [v0: List, v1: Node+NullValue, v2: Node+NullValue, v3: Char, v4: Char, v5: Char, v6: Node -> one Char, v7: Node -> one (Node+NullValue), v8: List -> one (Node+NullValue)] {\n  privInvUno[v0, v8, v7, v6]\n &&   not BranchUno[v0, v8]\n &&   BranchDos[v1]\n &&   not WPINCPREV07[v0, v1, v2, v3, v4, v5, v6, v7, v8]\n}\n\npred amgasPre[v0: List, v1: Node+NullValue, v2: Node+NullValue, v3: Char, v4: Char, v5: Char, v6: Node -> one Char, v7: Node -> one (Node+NullValue), v8: List -> one (Node+NullValue)] {\n  removeAllPre[v0, v6, v7, v8] &&  state0[v0, v1, v2, v3, v4, v5, v6, v7, v8]\n}";
		String assert1 ="assertCorrectness amgas[v0: List, v1: Node+NullValue, v2: Node+NullValue, v3: Char, v4: Char, v5: Char, v6: Node -> one Char, v7: Node -> one (Node+NullValue), v8: List -> one (Node+NullValue)] { \n    pre = { amgasPre[v0, v1, v2, v3, v4, v5, v6, v7, v8] }\n    program = { test8[v0, v1, v2, v3, v4, v5, v6, v7, v8];test8[v0, v1, v2, v3, v4, v5, v6, v7, v8];test8[v0, v1, v2, v3, v4, v5, v6, v7, v8];test8[v0, v1, v2, v3, v4, v5, v6, v7, v8];test8[v0, v1, v2, v3, v4, v5, v6, v7, v8] }\n    post = { amgasPost[v0', v1', v2', v3', v4', v5', v6', v7', v8']  }\n  }";
		String check1 = "check amgas for 1 Node, 1 Char";
		DocumentSplitter ds = split(module + "\n\n" + sig1 + "\n\n" + sig2 + "\n\n" + sig3 + "\n\n" + sig4 + "\n\n" + sig5 
				+ "\n\n" + pred1 + "\n\n" + act1 + "\n\n" + alloy2+ "\n\n" +assert1+ "\n\n" +check1);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY_MODULE, module,
				DynalloySections.ALLOY_SIGNATURE, sig1,
				DynalloySections.ALLOY_SIGNATURE, sig2,
				DynalloySections.ALLOY_SIGNATURE, sig3,
				DynalloySections.ALLOY_SIGNATURE, sig4,
				DynalloySections.ALLOY_SIGNATURE, sig5,
				DynalloySections.ALLOY, pred1,
				DynalloySections.DYNALLOY_ACTION, act1,
				DynalloySections.ALLOY, alloy2,
				DynalloySections.DYNALLOY_ASSERTION, assert1,
				DynalloySections.ALLOY, check1
				);
		assertSections(expected, ds);				
	}

	private DocumentSplitter split(String text) throws IOException {
		Document doc = new Document();
		doc.Load(text);
		DocumentSplitter ds = new DocumentSplitter(doc);
		return ds;
	}
	
	private void assertSections(List<String> sectionAndTexts, DocumentSplitter splitter) {
		DocumentSection section = new DocumentSection();
		int i=0;
		while(splitter.next(section)){
			assertEquals(sectionAndTexts.get(i++), section.getKind());
			assertEquals(sectionAndTexts.get(i++).trim(), section.getText().trim());			
		}
		assertEquals(i, sectionAndTexts.size());
	}
	
	private void assertSectionsKind(List<String> sections, DocumentSplitter splitter) {
		DocumentSection section = new DocumentSection();
		int i=0;
		while(splitter.next(section)){
			assertEquals(sections.get(i++), section.getKind());			
		}
		assertEquals(i, sections.size());
	}

	@Test
	public void alloySignature() throws Exception {
		String module = "module myModule\n\nsig A {} \n\n assertCorrectness amgas[a:univ] { pre={TruePred[]} program={Skip} post={TruePred}} ";
		DocumentSplitter ds = split(module);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY_MODULE,"module myModule",
				DynalloySections.ALLOY_SIGNATURE,"sig A {} ",
				DynalloySections.DYNALLOY_ASSERTION,"assertCorrectness amgas[a:univ] { pre={TruePred[]} program={Skip} post={TruePred}} ");
		assertSections(expected, ds);		
	}
	
	@Test
	public void splitSignatures() throws RecognitionException, TokenStreamException, IOException {
		StringBuffer input = new StringBuffer ();
		input.append("module moduleId\n");
		input.append("open util/integer\n");
		input.append("sig A {}\n");
		input.append("sig B { r: A }\n");
		input.append("program P[] { skip }\n");
		input.append("pred equ[l:univ,r:univ] {l=r }\n");
		input.append("action Q[a: A, b:B] { pre { TruePred[] } post { equ[b.r',a]} }\n");
		input.append("sig C { r: A }\n");
		input.append("pred TruePred[] { }\n");
		input.append("assert C { assertCorrectness[a:A,b:B] { pre={TruePred[]} program={call P[];Q[a,b]} post={TruePred[]}}}\n");
		input.append("check C\n");
		
		Document doc = new Document();
		doc.Load(input.toString());
		DocumentSplitter sp = new DocumentSplitter(doc);
		List<String> expectedSections = Arrays.<String>asList(DynalloySections.ALLOY_MODULE,
				DynalloySections.ALLOY_OPEN,
				DynalloySections.ALLOY_SIGNATURE,
				DynalloySections.ALLOY_SIGNATURE,
				DynalloySections.DYNALLOY_PROGRAM,
				DynalloySections.ALLOY,
				DynalloySections.DYNALLOY_ACTION,
				DynalloySections.ALLOY_SIGNATURE,
				DynalloySections.ALLOY,
				DynalloySections.DYNALLOY_ASSERTION,
				DynalloySections.ALLOY);
		assertSectionsKind( expectedSections, sp );				
	}
	
	@Test
	public void alloyModuleFirstLine() throws Exception {
		String moduleDecl = "module removeAll";
		String predDecl ="pred A[] { }";
		DocumentSplitter ds = split(moduleDecl + "\n\n" + predDecl);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY_MODULE, moduleDecl,
				DynalloySections.ALLOY, predDecl 
				);
		assertSections(expected, ds);				
	}
	
	@Test
	public void alloyModuleWithSlashFirstLine() throws Exception {
		String moduleDecl = "module foo/bar/baz";
		String predDecl ="pred A[] { }";
		DocumentSplitter ds = split(moduleDecl + "\n\n" + predDecl);
		List<String> expected = Arrays.<String>asList(
				DynalloySections.ALLOY_MODULE, moduleDecl,
				DynalloySections.ALLOY, predDecl 
				);
		assertSections(expected, ds);				
	}
	
	@Test
	public void AbstractSignatureAsAlloySignature() throws Exception {
		String text = "abstract sig A { }";
		DocumentSplitter ds = split(text);
		List<String> expected = Arrays.<String>asList(DynalloySections.ALLOY_SIGNATURE,text);
		assertSections(expected, ds);		
	}
	
	@Test
	public void OneSignatureAsAlloySignature() throws Exception {
		String text = "one sig A { }";
		DocumentSplitter ds = split(text);
		List<String> expected = Arrays.<String>asList(DynalloySections.ALLOY_SIGNATURE,text);
		assertSections(expected, ds);		
	}
}
