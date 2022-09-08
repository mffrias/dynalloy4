package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.alloy.VariableId;
import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.dataflow.CfgVarCollector.GlobalVariable;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class MainBounds {

	private static DefaultBoundsCalculator defaultBounds;
	private static SpecContext ctx;
	private static ProgramDeclaration mainProgram;
	
	public static void main(String[] args) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		if (args.length < 4) {
			System.out.println("Must suply the programID to analyse, dynalloy file path, and both the initial bounds file path and the bounds specification file path.");
			return;
		}
		
		String dynalloyProgramId = args[0];
		String dynalloyFile = args[1];	
		String boundsFile = args[2];		
		String boundsSpecFile = args[3];
		
		if (!new File(dynalloyFile).exists()) {
			System.out.println(String.format("Dynalloy file %s does not exist.", dynalloyFile));
			return;
		}
		if (!new File(boundsFile).exists()) {
			System.out.println(String.format("Bounds file %s does not exist.", boundsFile));
			return;
		}
		if (!new File(boundsSpecFile).exists()) {
			System.out.println(String.format("Bounds spec file %s does not exist.", boundsSpecFile));
			return;
		}

		
		boolean cnfOutput = false; //cnf or alloy output
		String pvarsFile = null;
		String cnfFile = null;
		if (args.length > 4) {
			cnfOutput = true;
			pvarsFile = args[4];
			if (!new File(pvarsFile).exists()) {
				System.out.println(String.format("Pvars file %s does not exist.", pvarsFile));
				return;
			}
			cnfFile = pvarsFile.replace(".pvars", ".cnf");
			if (!new File(cnfFile).exists()) {
				System.out.println(String.format("CNF file %s does not exist.", cnfFile));
				return;
			}
		}
					
		Set<BoundedVariable> finalBounds = getDataflowBounds(dynalloyProgramId, dynalloyFile, boundsFile, boundsSpecFile);
		
		BoundedVariableFormatter formatter;
		if (cnfOutput)
			formatter = new CnfBoundedVariableFormatter(ctx, mainProgram, defaultBounds, pvarsFile, cnfFile);
		else
			//TODO: Really ugly that we need all of this for the formatter...
			formatter = new AlloyBoundedVariableFormatter(ctx, mainProgram, defaultBounds);
		
		formatter.output(formatter.format(finalBounds));
	}
	
	private static Set<BoundedVariable> getDataflowBounds(String programId, String dynalloyFilePath, String initialBoundsFilePath, String initialBoundsSpecFilePath) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {		
		ctx = MainCommon.translateModule(dynalloyFilePath);
		
		mainProgram = ctx.getProgram(null, programId);
		if (mainProgram == null)
			throw new IllegalStateException(String.format("Program with id '%s' not found", programId));
		
		long startTime = System.currentTimeMillis();
		ControlFlowGraph cfg = new ControlFlowGraph(mainProgram, ctx);
		BoundsDataFlow analysis = new BoundsDataFlow(cfg, initialBoundsFilePath, initialBoundsSpecFilePath, mainProgram, ctx.getMapping());
		analysis.performAnalysis();
		long endTime = System.currentTimeMillis();
		System.out.println("//Dataflow time: " + (endTime - startTime) + "ms");
		
		BoundsFileParser parser = new BoundsFileParser(initialBoundsFilePath, initialBoundsSpecFilePath, mainProgram.getParameterTyping());
		CfgVarCollector varCollector = new CfgVarCollector(cfg, ctx.getMapping());
		defaultBounds = new DefaultBoundsCalculator(parser, varCollector);
		
		Set<BoundedVariable> calculatedBounds = analysis.getFinalAlloyBounds();
		
		int removedPrimaryVars = 0;
		int exceptions = 0;
		HashMap<ExprVariable, String> varTypes = defaultBounds.getVarTypes();
		for (BoundedVariable bv : calculatedBounds) {
			String type = varTypes.get(bv.getVariable());
			if (type.contains("Throwable") || type.contains("Exception"))
				exceptions++;
			int original = parser.getDefaultBoundsForType(type).size();
			removedPrimaryVars += original - bv.getBounds().size();
		}
		System.out.println(String.format("//Removed primary vars: %d (%d exceptions)", removedPrimaryVars, exceptions));
		
		return calculatedBounds;
	}
	
	//For the tests...
	public static Set<BoundedVariable> getAlloyDataflowBounds(String programId, String dynalloyFilePath, String initialBoundsFilePath, String initialBoundsSpecFilePath) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		Set<BoundedVariable> bounds = getDataflowBounds(programId, dynalloyFilePath, initialBoundsFilePath, initialBoundsSpecFilePath);
		BoundedVariableFormatter formatter = new AlloyBoundedVariableFormatter(ctx, mainProgram, defaultBounds);
		Set<BoundedVariable> formattedBounds = formatter.format(bounds); 
		return formattedBounds;
	}
}
