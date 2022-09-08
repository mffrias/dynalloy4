package ar.uba.dc.rfm.dynalloy.dataflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ar.uba.dc.rfm.alloy.AlloyTyping;
import ar.uba.dc.rfm.alloy.AlloyVariable;

public class BoundsFileParser {
	private final String delimiter = ",";
	private String boundsFilePath;
	private AlloyTyping typing;
	private Map<String, String> typesPrefixes;
	private Map<String, Integer> typesNumberOfAtoms;
	private Set<String> twoDimensionalFields;
	private Map<String, Set<Bound>> typesDefaultBounds;
	
	//TODO: Cambiar esto si los tipos default vienen prefijados o no!
	private static boolean JAVA_LANG_PREFIX = false; 
	
	public BoundsFileParser(String boundsFilePath, String boundsSpecFilePath, AlloyTyping typing) throws IOException {
		this.boundsFilePath = boundsFilePath;
		this.typing = typing;
		typesPrefixes = new HashMap<String, String>();
		typesNumberOfAtoms = new HashMap<String, Integer>();
		twoDimensionalFields = new LinkedHashSet<String>();
		typesDefaultBounds = new HashMap<String, Set<Bound>>();
		
		parseAndLoadSpecification(boundsSpecFilePath);
		findTwoDimensionalFields();
		createDefaultBoundsForBuiltInTypes();
		createDefaultBoundsForUserDefinedTypes();
		createDefaultBoundsForObjectType();
	}
	
	private void findTwoDimensionalFields() {
		Iterator<AlloyVariable> it = typing.iterator();
		while(it.hasNext()) {
			AlloyVariable var = it.next();
			String type = typing.get(var);
			if (type.contains("->"))
				twoDimensionalFields.add(var.getVariableId().getString());
		}		
	}

	public Set<BoundedVariable> getBoundsFromFile() throws IOException {		
		Map<String, BoundedVariable> bounds = new LinkedHashMap<String, BoundedVariable>();
		BufferedReader br = new BufferedReader(new FileReader(boundsFilePath));
		String line;
		while((line = br.readLine()) != null) {
			String[] tokens = line.split(delimiter);
			if (tokens.length != 4)
				throw new IllegalStateException("Bounds file format not supported");
			
			if (!tokens[3].equalsIgnoreCase("SAT"))
				continue;
			
			String fieldName = getFinalFieldName(tokens[0]);

			BoundedVariable bv;
			if (bounds.containsKey(fieldName))
				bv = bounds.get(fieldName);
			else {
				bv = new BoundedVariable(fieldName);
				bounds.put(fieldName, bv);
			}

			//TODO: HACK: Horrible!!! Esto es un parche feo para arreglar los archivos de cotas
			//de BSTree, que tienen null en un campo que va de (examples_bstree_Node)->one(Int)
			tokens[2] = fixNullWhenFieldIsSuppossedToBeInt(fieldName, tokens[1], tokens[2]);
			Bound b = createBoundForField(fieldName, tokens[1], tokens[2]);
			bv.getBounds().add(b);
		}

		return new LinkedHashSet<BoundedVariable>(bounds.values());
	}
	
	private String fixNullWhenFieldIsSuppossedToBeInt(String fieldName, String from, String to) {
		if (!fieldName.contains("examples_bstree_Node_key"))
			return to;
		
		if (!to.equals("null"))
			return to;
		
		//Queremos devolver un cero, que esta representado por -min...
		String zero = String.format("%d", Bound.getMinIntBound() * -1);
		return zero;
	}

	private void createDefaultBoundsForBuiltInTypes() {
		Set<Bound> nullBounds = new LinkedHashSet<Bound>();
		nullBounds.add(Bound.buildBound("null"));
		typesDefaultBounds.put("null", nullBounds);
		
		Set<Bound> booleanBounds = new LinkedHashSet<Bound>();
		booleanBounds.add(Bound.buildBound("false"));
		booleanBounds.add(Bound.buildBound("true"));
		typesDefaultBounds.put("boolean", booleanBounds);

		Set<Bound> intBounds = new LinkedHashSet<Bound>();
		for (int i = Bound.getMinIntBound(); i <= Bound.getMaxIntBound(); i++)
			intBounds.add(Bound.buildBound(i));
		typesDefaultBounds.put("Int", intBounds);
		
		Set<Bound> throwableBounds = new LinkedHashSet<Bound>();
		addThrowableBounds(throwableBounds);
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_Throwable", throwableBounds);
		else
			typesDefaultBounds.put("Throwable", throwableBounds);
		
		Set<Bound> exceptionBounds = new LinkedHashSet<Bound>();
		addExceptionBounds(exceptionBounds);
		
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_Exception", exceptionBounds);
		else
			typesDefaultBounds.put("Exception", exceptionBounds);
		
		Set<Bound> runtimeExceptionBounds = new LinkedHashSet<Bound>();
		addRuntimeExceptionBounds(runtimeExceptionBounds);
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_RuntimeException", runtimeExceptionBounds);
		else
			typesDefaultBounds.put("RuntimeException", runtimeExceptionBounds);

		Set<Bound> indexOutOfBoundsExceptionBounds = new LinkedHashSet<Bound>();
		addIndexOutOfBoundsExceptionBounds(indexOutOfBoundsExceptionBounds);
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_IndexOutOfBoundsException", indexOutOfBoundsExceptionBounds);
		else
			typesDefaultBounds.put("IndexOutOfBoundsException", indexOutOfBoundsExceptionBounds);
	
		Set<Bound> nullPointerExceptionBounds = new LinkedHashSet<Bound>();
		addNullPointerExceptionBounds(nullPointerExceptionBounds);
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_NullPointerException", nullPointerExceptionBounds);
		else
			typesDefaultBounds.put("NullPointerException", nullPointerExceptionBounds);
	}
	
	private void createDefaultBoundsForUserDefinedTypes() {
		for (String type : typesPrefixes.keySet()) {
			String prefix = typesPrefixes.get(type);
			int instances = typesNumberOfAtoms.get(type);
			
			Set<Bound> bounds = new LinkedHashSet<Bound>();
			for (int i = 0; i < instances; i++)
				bounds.add(Bound.buildBound(prefix + i));
			
			typesDefaultBounds.put(type, bounds);
		}
	}
	
	private void createDefaultBoundsForObjectType() {
		//This requires all the default bounds for the other types to be already calculated
		Set<Bound> objectBounds = new LinkedHashSet<Bound>();
		
		for (Set<Bound> bounds : typesDefaultBounds.values())
			objectBounds.addAll(bounds);
		
		if (JAVA_LANG_PREFIX)
			typesDefaultBounds.put("java_lang_Object", objectBounds);
		else
			typesDefaultBounds.put("Object", objectBounds);		
	}
	
	//TODO: All the "DefaultBounds" operations should be refactored to some other class (DefaultBoundsCalculator?)
	public Set<Bound> getDefaultBoundsForType(String type) {		
		type = removeTypeUnusedTokens(type);
		
		if (typesDefaultBounds.containsKey(type))
			return typesDefaultBounds.get(type);
		
		if (type.contains("->")) {
			String[] types = type.split("->");
			String type1 = types[0];
			String type2 = types[1];
			
			Set<Bound> boundsForType1 = getDefaultBoundsForType(type1);
			Set<Bound> boundsForType2 = getDefaultBoundsForType(type2);
			Set<Bound> productBounds = Bound.cartesianProduct(boundsForType1, boundsForType2);
			typesDefaultBounds.put(type, productBounds);
			return productBounds;
		}
		
		if (type.contains("+")) {
			String[] types = type.split("\\+");
			String type1 = types[0];
			String type2 = types[1];
			
			Set<Bound> unionBounds = getDefaultBoundsForType(type1);
			unionBounds.addAll(getDefaultBoundsForType(type2));
			typesDefaultBounds.put(type, unionBounds);
			return unionBounds;
		}
		
		if (!typesDefaultBounds.containsKey(type))
			throw new IllegalStateException(String.format("Type '%s' not defined in bounds specification file", type));
		
		return typesDefaultBounds.get(type);
	}
	
	public Bound getDefaultBoundForTypeInitialization(String type) {
		type = removeTypeUnusedTokens(type);
		
		if (type.contains("null"))
			return Bound.buildBound("null");
		
		if (type.equals("Object") || type.equals("java_lang_Object"))
			return Bound.buildBound("null");
		
		if (type.equals("Int"))
			return Bound.buildBound(0);
		
		return getDefaultBoundsForType(type).iterator().next();
	}
	
	private String removeTypeUnusedTokens(String type) {
		type = type.replace("lone", "");
		type = type.replace("one", "");
		type = type.replace("(", "");
		type = type.replace(")", "");
		type = type.replace("seq", "");
		type = type.replace("set", "");
		type = type.trim();
		
		return type;
	}
	
	private void addThrowableBounds(Set<Bound> bounds) {
		addExceptionBounds(bounds);
	}
	
	private void addExceptionBounds(Set<Bound> bounds) {
		if (JAVA_LANG_PREFIX)
			bounds.add(Bound.buildBound("java_lang_ExceptionLit"));
		else
			bounds.add(Bound.buildBound("ExceptionLit"));
		addRuntimeExceptionBounds(bounds);
	}
	
	private void addRuntimeExceptionBounds(Set<Bound> bounds) {
		if (JAVA_LANG_PREFIX)
			bounds.add(Bound.buildBound("java_lang_RuntimeExceptionLit"));
		else
			bounds.add(Bound.buildBound("RuntimeExceptionLit"));
		addIndexOutOfBoundsExceptionBounds(bounds);
		addNullPointerExceptionBounds(bounds);
	}
	
	private void addIndexOutOfBoundsExceptionBounds(Set<Bound> bounds) {
		if (JAVA_LANG_PREFIX)
			bounds.add(Bound.buildBound("java_lang_IndexOutOfBoundsExceptionLit"));
		else
			bounds.add(Bound.buildBound("IndexOutOfBoundsExceptionLit"));
	}
	
	private void addNullPointerExceptionBounds(Set<Bound> bounds) {
		if (JAVA_LANG_PREFIX)
			bounds.add(Bound.buildBound("java_lang_NullPointerExceptionLit"));
		else
			bounds.add(Bound.buildBound("NullPointerExceptionLit"));
	}
	
	private void parseAndLoadSpecification(String boundsSpecFilePath) {
		File file = new File(boundsSpecFilePath);
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		doc.getDocumentElement().normalize();
		
		Node typeDefinitionsRoot = doc.getElementsByTagName("typeDefinitions").item(0);
		NodeList typeDefinitions = typeDefinitionsRoot.getChildNodes();
		for (int i = 0; i < typeDefinitions.getLength(); i++) {
			Node node = typeDefinitions.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			Element typeDefinition = (Element)node;
			String name = getTagValue(typeDefinition, "name");
			String prefix = getTagValue(typeDefinition, "prefix");
			String instances = getTagValue(typeDefinition, "numberOfAtoms");
			typesPrefixes.put(name, prefix);
			typesNumberOfAtoms.put(name, Integer.parseInt(instances));
		}
		
//		Node variableTypesRoot = doc.getElementsByTagName("variableTypes").item(0);
//		NodeList variableTypes = variableTypesRoot.getChildNodes();
//		for (int i = 0; i < variableTypes.getLength(); i++) {
//			Node node = variableTypes.item(i);
//			if (node.getNodeType() != Node.ELEMENT_NODE)
//				continue;
//			Element variableType = (Element)node;
//			//String variable = removeAlloyIncarnation(getTagValue(variableType, "variable"));
//			String variable = getTagValue(variableType, "variable");
//			String type = getTagValue(variableType, "type");
//			varTypes.put(variable, type);
//			if (type.contains("->"))
//				twoDimensionalFields.add(variable);
//		}
		
		int bitwidth = Integer.parseInt(getTagValue(doc.getDocumentElement(), "bitwidth"));
		Bound.setBitwidth(bitwidth);
	}
	
	private String getTagValue(Element element, String tag) {
	    NodeList childs = element.getElementsByTagName(tag).item(0).getChildNodes();
	    return childs.item(0).getNodeValue();    
	}
	
	private String getFinalFieldName(String fieldNameFromBoundsFile) {
		//If it's a recursive function field, the field is split in two: bfield and ffield, so we fix that
		if (twoDimensionalFields.contains(removeAlloyIncarnation(fieldNameFromBoundsFile).substring(1)))
			return fieldNameFromBoundsFile.substring(1);
		else
			return fieldNameFromBoundsFile;
	}
	
	private String removeAlloyIncarnation(String varId) {
		int underscoreLastIndex = varId.lastIndexOf('_');
		//In case there is no underscore, there is nothing to do
		if (underscoreLastIndex == -1)
			return varId;
		
		String suffix = varId.substring(underscoreLastIndex + 1);
		try {
			Integer.parseInt(suffix);
			//If the last underscore is actually preceding the numeric incarnation, we return
			//only the name of the variable
			return varId.substring(0, underscoreLastIndex);
		}
		catch (NumberFormatException ex) {
			//In case the suffix after the underscore is not numeric, it means it was part of the name
			//of the variable, so we return it intact
			return varId;
		}
	}
	
	private Bound createBoundForField(String fieldName, String from, String to) {
		if (twoDimensionalFields.contains(removeAlloyIncarnation(fieldName))) {
			String[] types = getTypesForFunction(fieldName);
			String fromInstance = getInstanceName(from, types[0]);
			if (types[1].equals("Int"))
				return Bound.buildBound(fromInstance, Integer.parseInt(to) + Bound.getMinIntBound());
			else {
				String toInstance = getInstanceName(to, types[1]);
				return Bound.buildBound(fromInstance, toInstance);
			}
		}
		else {
			String type = getTypeForVariable(fieldName);
			if (type.equals("Int"))
				return Bound.buildBound(Integer.parseInt(to) + Bound.getMinIntBound());
			else {
				String toInstance = getInstanceName(to, type);
				return Bound.buildBound(toInstance);
			}
		}
	}
	
	private String getInstanceName(String instanceId, String type) {
		if (instanceId.equals("null"))
			return instanceId;
		
		return getTypePrefix(type) + instanceId;
	}
	
	private String[] getTypesForFunction(String fieldName) {	
		String[] types = getTypeForVariable(fieldName).split("->");
		if (types.length != 2)
			throw new IllegalStateException(String.format("Type for variable '%s' should be a function", fieldName));

		return new String[] { types[0].trim(), types[1].trim() };
	}
	
	private String getTypeForVariable(String variableId) {
		String varIdWithoutIncarnation = removeAlloyIncarnation(variableId);
		AlloyVariable alloyVar = new AlloyVariable(varIdWithoutIncarnation);
		
		if (!typing.contains(alloyVar))
			throw new IllegalStateException(String.format("No typing information found for variable '%s'", variableId));
	
		String type = typing.get(alloyVar);
		if (type.contains("+"))
			type = type.split("\\+")[0];
		return removeTypeUnusedTokens(type);
	}
	
	private String getTypePrefix(String type) {
		if (!typesPrefixes.containsKey(type))
			throw new IllegalStateException(String.format("Type '%s' not defined in bounds specification file", type));
		
		return typesPrefixes.get(type);
	}
	
}
