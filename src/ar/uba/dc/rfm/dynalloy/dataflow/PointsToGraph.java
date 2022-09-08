package ar.uba.dc.rfm.dynalloy.dataflow;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ar.uba.dc.rfm.alloy.ast.expressions.ExprVariable;

/**
 * A points-to graph is a tuple <L, N, E>, where L is the set of labels that point to nodes (vars),
 * N is the set of Nodes (objects), and E is the set of Edges (fields).
 */
public class PointsToGraph {

	public class Node implements Comparable<Node>{
		private String id;
		
		public Node(String id) {
			this.id = id;
		}

		@Override
		public int compareTo(Node o) {
			return id.compareTo(o.id);
		}
		
		@Override
		public String toString() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		private PointsToGraph getOuterType() {
			return PointsToGraph.this;
		}
		
		
	}
	
	public class Edge implements Comparable<Edge> {
		private String field;
		private Node from;
		private Node to;
		
		public Edge(String field, Node from, Node to) {
			this.field = field;
			this.from = from;
			this.to = to;
		}
		
		public String getField() {
			return field;
		}
		
		public Node getFrom() {
			return from;
		}
		
		public Node getTo() {
			return to;
		}

		@Override
		public int compareTo(Edge o) {
			int fromComparison = from.compareTo(o.from); 
			int fieldComparison = field.compareTo(o.field);
			int toComparison = to.compareTo(o.to);
			
			if (fromComparison != 0)
				return fromComparison;
			
			if (fieldComparison != 0)
				return fieldComparison;
			
			return toComparison;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("%s-'%s'->%s", from, field, to);
		}

		private PointsToGraph getOuterType() {
			return PointsToGraph.this;
		}
	}
	
	//Map nodes to out-edges from those nodes
	private Map<Node, Set<Edge>> localNodes;
	private Map<Node, Set<Edge>> paramNodes;
	//Also, keep a hashtable to access edges by name
	private Map<String, Set<Edge>> edgesByFieldName;
	//Maps Var names to the set of nodes pointed by those labels
	private Map<String, Set<Node>> labels;
	//Constant labels (null, true, false, ExceptionList, etc...)
	private Set<String> constantLabels;
	
	public PointsToGraph() {
		localNodes = new HashMap<PointsToGraph.Node, Set<Edge>>();
		paramNodes = new HashMap<PointsToGraph.Node, Set<Edge>>();
		labels = new HashMap<String, Set<Node>>();
		edgesByFieldName = new HashMap<String, Set<Edge>>();
		constantLabels = new HashSet<String>();
	}
	
	public void addParamNode(Node n) {
		addNodeTo(n, paramNodes);
	}
	
	public void addLocalNode(Node n) {
		addNodeTo(n, localNodes);
	}

	private void addNodeTo(Node n, Map<Node, Set<Edge>> nodeMap) {
		if (!nodeMap.containsKey(n))
			nodeMap.put(n, new HashSet<Edge>());
	}
	
	public boolean isParamNode(Node n) {
		if (paramNodes.containsKey(n))
			return true;
		else if (localNodes.containsKey(n))
			return false;
		else
			throw new IllegalArgumentException(String.format("Node %s is not part of the PTG", n));
	}
	
	public void addEdge(Edge e) {
		if (isParamNode(e.from))
			paramNodes.get(e.from).add(e);
		else
			localNodes.get(e.from).add(e);
		
		if (!edgesByFieldName.containsKey(e.getField())) {
			edgesByFieldName.put(e.getField(), new HashSet<PointsToGraph.Edge>());
		}
		edgesByFieldName.get(e.getField()).add(e);
		
		if (!localNodes.containsKey(e.to) && !paramNodes.containsKey(e.to))
			throw new IllegalArgumentException(String.format("Edge %s connects to a nonexisting node", e));
	}
	
	public void addEdges(Set<Edge> edges) {
		for (Edge e : edges) {
			addEdge(e);
		}
	}
	
	public void addLabel(ExprVariable var, Node n) {
		addLabel(getVarIdentifier(var), n);
	}
	
	public void addConstantLabel(String id, Node n) {
		constantLabels.add(id);
		addLabel(id, n);
	}

	public void addLabel(String var, Node n) {
		Map<String, Set<Node>> labels = new HashMap<String, Set<Node>>();
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(n);
		labels.put(var, nodes);
		addLabels(labels);
	}
	
	public void addLabels(Map<String, Set<Node>> newLabels) {
		for (Map.Entry<String, Set<Node>> e : newLabels.entrySet()) {
			if (labels.containsKey(e.getKey()))
				labels.get(e.getKey()).addAll(e.getValue());
			else
				labels.put(e.getKey(), e.getValue());
		}
	}
	
	public void redefineLabel(ExprVariable label, Node node) {
		Set<Node> nodes = new HashSet<PointsToGraph.Node>();
		nodes.add(node);
		redefineLabel(label, nodes);
	}
	
	public void redefineLabel(ExprVariable label, Set<Node> nodes) {
		labels.put(getVarIdentifier(label), nodes);
	}
	
	public Set<Node> getNodesReachedByLabel(String label) {
		return labels.get(label);
	}
	
	public Set<Node> getNodesReachedByLabel(ExprVariable var) {
		return getNodesReachedByLabel(getVarIdentifier(var));
	}
	
	public Set<Node> getNodesReachedFromNodeByField(Node n, ExprVariable field) {
		String fieldName = getVarIdentifier(field);
		Set<Node> reachedNodes = new HashSet<Node>();
		
		Map<Node, Set<Edge>> nodeSet;
		if (isParamNode(n))
			nodeSet = paramNodes;
		else
			nodeSet = localNodes;
		
		for (Edge e : nodeSet.get(n)) {
			if (e.field.equals(fieldName)) {
				reachedNodes.add(e.to);
			}
		}
		return reachedNodes;
	}
	
	public void merge(PointsToGraph ptg) {
		//It's necessary to add all the nodes first, and then the edges
		for (Node n : ptg.localNodes.keySet())
			addLocalNode(n);
		for (Node n : ptg.paramNodes.keySet())
			addParamNode(n);
		for (Set<Edge> e : ptg.localNodes.values())
			addEdges(e);
		for (Set<Edge> e : ptg.paramNodes.values())
			addEdges(e);
		
		addLabels(ptg.labels);
		for (String constantLabel : ptg.constantLabels)
			constantLabels.add(constantLabel);
		for (String fieldName : ptg.edgesByFieldName.keySet()) {
			edgesByFieldName.put(fieldName, new HashSet<PointsToGraph.Edge>());
			for (Edge edge : ptg.edgesByFieldName.get(fieldName)) {
				edgesByFieldName.get(fieldName).add(edge);
			}
		}
	}
	
	public void createAndAddEdge(ExprVariable field, Node from, Node to) {
		createAndAddEdges(field, Arrays.asList(from), Arrays.asList(to));
	}
	
	public void createAndAddEdges(ExprVariable field, Collection<Node> from, Collection<Node> to) {
		Set<Edge> edges = new HashSet<Edge>();
		for (Node n1 : from) {
			for (Node n2 : to) {
				edges.add(new Edge(getVarIdentifier(field), n1, n2));
			}
		}

		addEdges(edges);
	}
	
	private String getVarIdentifier(ExprVariable var) {
		return var.getVariable().toString();
	}
	
	public Set<Node> getLocalNodes() {
		return localNodes.keySet();
	}
	
	public Set<Node> getParamNodes() {
		return paramNodes.keySet();
	}
	
	public Map<String, Set<Node>> getLabels() {
		return labels;
	}
	
	public Set<Edge> getEdgesFromNode(Node n) {
		if (isParamNode(n))
			return paramNodes.get(n);
		else
			return localNodes.get(n);
	}
	
	public Set<Edge> getFieldEdges(ExprVariable field) {
		String fieldName = getVarIdentifier(field);
		if (!edgesByFieldName.containsKey(fieldName))
			return null;
		
		return edgesByFieldName.get(fieldName);
	}
	
	public Map<String, Set<String>> getNotAliasedLabels() {		
		//TODO: Hacer esto lineal en la cant de labels?
		
		Map<String, Set<String>> nonAliasedLabels = new LinkedHashMap<String, Set<String>>();
		for (String label : labels.keySet()) {
			if (constantLabels.contains(label))
				continue;
				
			Set<Node> pointedNodes = labels.get(label);
			
			Set<String> nonAliased = new LinkedHashSet<String>();
			for (String nextLabel : labels.keySet()) {
				if (constantLabels.contains(nextLabel))
					continue;
					
				Set<Node> nextPointedNodes = labels.get(nextLabel);
				
				if (isIntersectionEmpty(pointedNodes, nextPointedNodes))
					nonAliased.add(nextLabel);
			}
			nonAliasedLabels.put(label, nonAliased);
		}
		
		return nonAliasedLabels;
	}
	
	public Map<String, Set<String>> getNotAliasedFields() {
		//TODO: Hacer esto con mejor orden tambien?
		
		Map<String, Set<String>> nonAliasedFields = new LinkedHashMap<String, Set<String>>();
		for (String fieldName : edgesByFieldName.keySet()) {
			Set<Edge> edges = edgesByFieldName.get(fieldName);
			Set<Node> froms = new LinkedHashSet<PointsToGraph.Node>();
			Set<Node> tos = new LinkedHashSet<PointsToGraph.Node>();
			for (Edge e : edges) {
				froms.add(e.getFrom());
				tos.add(e.getTo());
			}
			
			Set<String> nonAliased = new LinkedHashSet<String>();
			for (String nextFieldName : edgesByFieldName.keySet()) {
				Set<Edge> nextEdges = edgesByFieldName.get(nextFieldName);
				Set<Node> nextFroms = new LinkedHashSet<PointsToGraph.Node>();
				Set<Node> nextTos = new LinkedHashSet<PointsToGraph.Node>();
				for (Edge e : nextEdges) {
					nextFroms.add(e.getFrom());
					nextTos.add(e.getTo());
				}
				
				boolean noAlias = isIntersectionEmpty(froms, nextFroms) && isIntersectionEmpty(tos, nextTos);
				if (noAlias)
					nonAliased.add(nextFieldName);
			}
			nonAliasedFields.put(fieldName, nonAliased);
		}
		
		return nonAliasedFields;
	}
	
	private boolean isIntersectionEmpty(Set<Node> set1, Set<Node> set2) {
		for (Node n : set1) {
			if (set2.contains(n)) {
				return false;
			}
		}
		return true;
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Total labels: %d; total param nodes: %d; total local nodes: %d\n", labels.size(), paramNodes.size(), localNodes.size()));
		
		builder.append("Labels:\n");
		for (Entry<String, Set<Node>> e : labels.entrySet()) {
			builder.append(String.format(" %s -> %s\n", e.getKey(), e.getValue()));
		}
		
		builder.append("Parameter nodes:\n");
		appendNodeSetStringRepresentation(paramNodes, builder);
		
		builder.append("Local nodes:\n");
		appendNodeSetStringRepresentation(localNodes, builder);
		
		return builder.toString();
	}
	
	private void appendNodeSetStringRepresentation(Map<Node, Set<Edge>> nodeSet, StringBuilder builder) {
		for (Node n : nodeSet.keySet()) {
			builder.append(String.format(" %s:\n", n));
			for (Edge e : nodeSet.get(n)) {
				builder.append(String.format("  %s\n", e));
			}
		}
	}
	
}
