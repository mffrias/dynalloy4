package ar.uba.dc.rfm.dynalloy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.ast.AlloyModule;
import ar.uba.dc.rfm.alloy.util.AlloyPrinter;
import ar.uba.dc.rfm.dynalloy.ast.DynalloyModule;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.parser.DynalloyParser;

public class LocalVariableTest {

	private String buildModStr() {
		StringBuffer buff = new StringBuffer();
		buff.append("module moduleId \n");
		buff.append("sig A {} \n");
		buff.append("sig B { f: A } \n");
		buff.append("pred equ[l,r:univ] {l=r} \n");
		buff.append("pred true[] {} \n");
		buff.append("action change[l,r:univ] { \n");
		buff.append("pre {true[]} \n");
		buff.append("post {equ[l',r]} \n");
		buff.append("} \n");
		buff.append("program swap[l,r:univ] var [t:univ]{ \n");
		buff.append(" change[t,l]; \n");
		buff.append(" change[l,r]; \n");
		buff.append(" change[r,t] \n");
		buff.append("} \n");
		buff.append("program swapCaller[l,r:univ] { \n");
		buff.append(" call swap[l,r] \n");
		buff.append("} \n");
		return buff.toString();
	}

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void parse() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		String inputDynAlloyStr = buildModStr();
		DynalloyParser parser = new DynalloyParser();
		DynalloyModule dynalloyAST = parser.parse(inputDynAlloyStr);
	}

	@Test
	public void translate() throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		String dynalloyStr = buildModStr();

		DynalloyParser parser = new DynalloyParser();
		DynalloyModule dynalloyAST = parser.parse(dynalloyStr);

		DynAlloyTranslator xlator = new DynAlloyTranslator(false);
		AlloyModule alloyAST = xlator.translateDynAlloyAST(dynalloyAST, DynAlloyOptions.DEFAULT_DYNALLOY_OPTIONS, null, null, null, null);

		AlloyPrinter printer = new AlloyPrinter();
		String alloyStr = (String) alloyAST.accept(printer);
		System.out.println(alloyStr);
	}

}
