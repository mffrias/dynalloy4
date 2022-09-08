package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;
import ar.uba.dc.rfm.dynalloy.ast.ProgramDeclaration;
import ar.uba.dc.rfm.dynalloy.xlator.SpecContext;


public class CnfBoundedVariableFormatter extends BoundedVariableFormatter {

	private class InvalidPVarsLineException extends Exception {
		public InvalidPVarsLineException(String line) {
			super("Invalid line: " + line);
		}
	}
	
	private DefaultBoundsCalculator defaultBounds;
	private String pvarsFilePath;
	private String cnfFilePath;
	private Map<ExprVariable, Set<Bound>> originalBounds;

	public CnfBoundedVariableFormatter(SpecContext context, ProgramDeclaration program, DefaultBoundsCalculator defaultBounds, String pvarsFilePath, String cnfFilePath) {
		super(context, program);
		this.defaultBounds = defaultBounds;
		this.pvarsFilePath = pvarsFilePath;
		this.cnfFilePath = cnfFilePath;
	}
	
	@Override
	public Set<BoundedVariable> format(Set<BoundedVariable> bounds) {
		//Keep the original prefixed bounds
		originalBounds = createMap(prefixNonParameters(bounds));
		
		//We need to get all the variables that are going to be eliminated, not the possible values
		Set<BoundedVariable> complement = defaultBounds.getBoundsComplement(bounds);
		return prefixNonParameters(complement);
	}
	
	@Override
	public void output(Set<BoundedVariable> bounds) {
		Map<String, Integer> pvarsMap;
		try {
			 pvarsMap = createPVarsMap();
		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}
		
		List<String> keysNotFound = new LinkedList<String>();
		List<String> clausesToAdd = new LinkedList<String>();
		for (BoundedVariable bv : bounds) {
			for (Bound bound : bv.getBounds()) {
				String key = getRelationBoundKey(bv.getVariable().toString(), bound);
				if (!pvarsMap.containsKey(key))
					keysNotFound.add(key);
				else
					clausesToAdd.add(String.format("-%d 0", pvarsMap.get(key)));
			}
		}
		
		System.out.println(String.format("Pvars eliminated: %d", clausesToAdd.size()));
		if (keysNotFound.size() != 0) {
			System.out.println("WARNING: The following keys couldn't be matched:");
			for (String key : keysNotFound)
				System.out.println(String.format("'%s' not found in pvars", key));
		}
		
		try {
			outputToCnfFile(clausesToAdd);
			System.out.println("DONE.");
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private Map<ExprVariable, Set<Bound>> createMap(Set<BoundedVariable> bounds) {
		Map<ExprVariable, Set<Bound>> map = new HashMap<ExprVariable, Set<Bound>>();
		
		for (BoundedVariable bv : bounds)
			map.put(bv.getVariable(), bv.getBounds());
		
		return map;
	}
	
//	private boolean validMissingRelation(BoundedVariable bv, Bound bound) {
//		//TODO: Document this
//		boolean isFirstIncarnation = bv.getVariable().getVariable().getIndex() == 1;
//		boolean hasOnlyOneValue = originalBounds.get(bv.getVariable()).size() == 1;
//		
//		if (isFirstIncarnation && hasOnlyOneValue)
//			return true;
//		
//		//TODO: Ojo con el java_lang_prefix
//		Set<Bound> defaultExceptionBounds = defaultBounds.getDefaultBoundsForType("Exception");
//		boolean isExceptionMissing = defaultExceptionBounds.contains(bound);
//		if (isExceptionMissing)
//			return true;
//		
//		//Set<Bound> objectDefaultBounds = defaultBounds.getDefaultBoundsForType("Object");
//		//if
//		
//		return false;
//	}

	private void outputToCnfFile(List<String> clausesToAdd) throws IOException {
		if (clausesToAdd.size() == 0)
			return;
		
		BufferedReader reader = new BufferedReader(new FileReader(cnfFilePath));
		BufferedWriter writer = new BufferedWriter(new FileWriter(cnfFilePath + ".out"));
		
		modifyFirstLine(reader, writer, clausesToAdd.size());
		copyRestOfFile(reader, writer);
		appendClauses(reader, writer, clausesToAdd);
		
		reader.close();
		writer.close();
	}

	private void modifyFirstLine(BufferedReader reader, BufferedWriter writer, int clausesToAdd) throws IOException {
		//Modify first line, which has format "p cnf <vars> <clauses>"
		String firstLine = reader.readLine();
		String[] tokens = splitLine(firstLine);
		int clauses = Integer.parseInt(tokens[3]);
		String newFirstLine = String.format("%s %s %s %d", tokens[0], tokens[1], tokens[2], clauses + clausesToAdd);
		writer.write(newFirstLine);
		writer.newLine();
	}
	
	private void copyRestOfFile(BufferedReader reader, BufferedWriter writer) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			writer.write(line);
			writer.newLine();
		}
	}
	
	private void appendClauses(BufferedReader reader, BufferedWriter writer, List<String> clausesToAdd) throws IOException {
		for (String clause : clausesToAdd) {
			writer.write(clause);
			writer.newLine();
		}
	}
	
	private Map<String, Integer> createPVarsMap() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(pvarsFilePath));
		String line;
		
		Map<String, Integer> pvarsMap = new HashMap<String, Integer>();
		while((line = br.readLine()) != null) {
			try {
				String relName = getRelationNameFromPvarsLine(line);
				Bound atom = getBoundFromPvarsLine(line);
				Integer varNumber = getPropVarNumberFromPvarsLine(line);
				pvarsMap.put(getRelationBoundKey(relName, atom), varNumber);
			}
			catch (InvalidPVarsLineException e) {
				//Swallow the exception to keep processing the file
			}
		}
	
		return pvarsMap;
	}
	
	private String getRelationBoundKey(String relation, Bound bound) {
		return relation + "," + bound.toString();
	}
	
	
	private String getRelationNameFromPvarsLine(String line) {
		String relName = splitLine(line)[0];
		if (relName.startsWith("this/"))
			relName = relName.substring(5);
		if (relName.startsWith("QF."))
			relName = relName.substring(3);
		
		return relName;
	}
	
	private Bound getBoundFromPvarsLine(String line) throws InvalidPVarsLineException {
		String atoms = splitLine(line)[1];
		String noBrackets = atoms.substring(1, atoms.length() - 1);
		String noInstance = noBrackets.replaceAll("\\$0", "");
		String[] tokens = noInstance.split(",");
		if (tokens.length > 2)
			throw new InvalidPVarsLineException(line);
		
		if (tokens.length == 1) {
			if (isInteger(tokens[0]))
				return Bound.buildBound(Integer.parseInt(tokens[0]));
			else
				return Bound.buildBound(tokens[0]);
		}
		else {
			if (isInteger(tokens[1]))
				return Bound.buildBound(tokens[0], Integer.parseInt(tokens[1]));
			else
				return Bound.buildBound(tokens[0], tokens[1]);
		}
	}
	
	private boolean isInteger(String str) {
		return str.matches("-?\\d+");
	}
	
	private int getPropVarNumberFromPvarsLine(String line) {
		String varNumber = splitLine(line)[2];
		return Integer.parseInt(varNumber);
	}
	
	private String[] splitLine(String line) {
		//Splits matching all the occurrences of whitespaces
		return line.split("\\s+");
	}

}
