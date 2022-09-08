package ar.uba.dc.rfm.dynalloy.xlator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.DynAlloyCompiler;
import ar.uba.dc.rfm.dynalloy.DynAlloyTranslator;
import ar.uba.dc.rfm.dynalloy.DynAlloyOptions;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;
import ar.uba.dc.rfm.dynalloy.util.DynalloyPrinter;

public class SwapTranslatorTest {

	@Before
	public void setUp() throws Exception {
		StringBuffer buff = new StringBuffer();
		buff.append("module mymodule\n");

		buff.append("sig A {}\n");

		buff.append("sig B {}\n");

		buff.append("pred TruePred[] {}\n");
		buff.append("pred equ[a,b: univ] {");
		buff.append("a = b ");
		buff.append("}\n");

		buff.append("pred checkSwapPre[a0: univ,b0: univ]{");
		buff.append("not equ[a0,b0] ");
		buff.append("}\n");

		buff.append("pred checkSwapPost[a0: univ,b0: univ, a1:univ, b1: univ] {");
		buff.append("a1 = b0 ");
		buff.append("b1 = a0 ");
		buff.append("}\n");

		buff.append("action updateVar[l:univ, r:univ] {");
		buff.append("pre { TruePred[] } ");
		buff.append("post { equ[l',r] } ");
		buff.append("}\n");

		buff.append("program swap[a: univ,b: univ]");
		buff.append("var [ temp: univ ] ");
		buff.append("{");
		buff.append("updateVar[temp,a]; ");
		buff.append("updateVar[a,b]; ");
		buff.append("updateVar[b,temp] ");
		buff.append("}\n");

		buff.append("assertCorrectness checkSwap[a: univ, b: univ]{");
		buff.append("pre = {checkSwapPre[a,b]} ");
		buff.append("program = { call swap[a,b] } ");
		buff.append("post = { checkSwapPost[a,b,a',b'] } ");
		buff.append("}\n");

		buff.append("check checkSwap\n");
		swapSpecDynalloyAST = buff.toString();
	}

	private String swapSpecDynalloyAST;

	@Test
	public void translateSwapAssertion() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		DynAlloyTranslator subject = new DynAlloyTranslator(false);
		
		DynalloyParser parser = new DynalloyParser();
		DynalloyModule dynalloyAST = parser.parse(swapSpecDynalloyAST);
		
		AlloyModule alloyAST = subject.translateDynAlloyAST(dynalloyAST , DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS, null, null, null, null);
		
		AlloyPrinter printer = new AlloyPrinter();
		String alloyStr = (String) alloyAST.accept(printer);
		
		System.out.println(alloyStr);
	}
}
