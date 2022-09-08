package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import ar.uba.dc.rfm.alloy.AlloyVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.parser.AssertionNotFound;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;

public class MainPointsTo {
	
	private static Map<String, Set<String>> notAliasedLabels;
	private static Map<String, Set<String>> notAliasedFields;

	public static void main(String[] args) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		if (args.length != 2) {
			System.out.println("Must suply the programID to analyse and the dynalloy file path.");
			return;
		}
		
		String dynalloyProgramId = args[0];
		String dynalloyFile = args[1];	
		
		if (!new File(dynalloyFile).exists()) {
			System.out.println(String.format("Dynalloy file %s does not exist.", dynalloyFile));
			return;
		}
		
		calculateDataflowPointsTo(dynalloyProgramId, dynalloyFile);
		
		System.out.println("//PointsTo");
		
		System.out.println("//Labels");
		System.out.println("fact {");
		for (String label : notAliasedLabels.keySet()) {
			for (String notAliasedLabel : notAliasedLabels.get(label)) {
				System.out.println(String.format("no (QF.%s & QF.%s)", label, notAliasedLabel));
			}
		}
		System.out.println("}");
		
		System.out.println("//Fields");
		System.out.println("fact {");
		for (String field : notAliasedFields.keySet()) {
			for (String notAliasedField : notAliasedFields.get(field)) {
				System.out.println(String.format("no (%s & %s)", getCorrectedField(field), getCorrectedField(notAliasedField)));
			}
		}
		System.out.println("}");
	}
	
	private static String getCorrectedField(String field) {
		if (field.equals("next_0") || field.equals("previous_0") ||
				field.equals("listNext_0") || field.equals("listPrevious_0") ||
				field.equals("right_0") || field.equals("left_0") ||
				field.equals("sibling_0") || field.equals("parent_0") || field.equals("child_0") ||
				field.equals("examples_bstree_Node_left_0") || field.equals("examples_bstree_Node_right_0"))
			return getBackwardAndForwardFieldUnion(field);
		else 
			return "QF." + field;
	}
	
	private static String getBackwardAndForwardFieldUnion(String field) {
		return String.format("(QF.b%s+QF.f%s)", field, field);
	}
	
	public static void calculateDataflowPointsTo(String programId, String dynalloyFilePath) throws RecognitionException, TokenStreamException, IOException, AssertionNotFound {
		SpecContext ctx = MainCommon.translateModule(dynalloyFilePath);
		
		ProgramDeclaration mainProgram = ctx.getProgram(null, programId);
		if (mainProgram == null)
			throw new IllegalStateException(String.format("Program with id '' not found", programId));
		
		ControlFlowGraph cfg = new ControlFlowGraph(mainProgram, ctx);
		PointsToDataFlow analysis = new PointsToDataFlow(cfg, ctx.getMapping());
		analysis.performAnalysis();
		
		PointsToGraph ptg = analysis.getFinalPointsTo();
		
		notAliasedLabels = prefixMap(ptg.getNotAliasedLabels(), ctx, mainProgram);
		notAliasedFields = prefixMap(ptg.getNotAliasedFields(), ctx, mainProgram);
	}
	
	private static Map<String, Set<String>> prefixMap(Map<String, Set<String>> notAliased, SpecContext ctx, ProgramDeclaration mainProgram) {
		if (ctx.getLocalVarIndex() != 0) {
			Map<String, Set<String>> notAliasedPrefixed = new HashMap<String, Set<String>>();	
			String prefix = String.format("l%d_", ctx.getLocalVarIndex() - 1);
			for (String label : notAliased.keySet()) {
				Set<String> prefixedSet = new HashSet<String>();
				for (String childLabel : notAliased.get(label)) {
					AlloyVariable var = BoundedVariable.createAlloyVar(childLabel);
					if (mainProgram.getParameters().contains(var.getVariableId())) {
						prefixedSet.add(childLabel);
					}
					else {
						prefixedSet.add(prefix + childLabel);
					}
				}
				AlloyVariable var = BoundedVariable.createAlloyVar(label);
				if (mainProgram.getParameters().contains(var.getVariableId())) {
					notAliasedPrefixed.put(label, prefixedSet);
				}
				else {
					notAliasedPrefixed.put(prefix + label, prefixedSet);
				}
			}
			
			return notAliasedPrefixed;
		}
		else
			return notAliased;
	}
	
}
