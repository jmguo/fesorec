package featureide.fm.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import featureide.fm.model.Constraint;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.Constraint.ConstraintType;

public class ConstraintChainValidator {

	private class Node {
		public String name;
		public boolean visited;

		public Node(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

	}

	private Map<String, HashSet<Node>> reqBy = new HashMap<String, HashSet<Node>>();
	private Map<String, HashSet<Node>> exBy = new HashMap<String, HashSet<Node>>();
	private Set<Node> nodeSet = new HashSet<Node>();

	private FeatureModel featureModel;
	public boolean Debug = false;

	public ConstraintChainValidator(FeatureModel fm) {
		featureModel = fm;
	}

	public void initGraphs() {
		List<Constraint> constraints = featureModel.getConstraints();
		for (Constraint constraint : constraints) {
			String nameA = constraint.getFeatureA();
			String nameB = constraint.getFeatureB();
			Map<String, HashSet<Node>> graph;
			if (constraint.getConstraintType() == ConstraintType.require) {
				graph = reqBy;
			} else {
				graph = exBy;
			}

			HashSet<Node> nodes = graph.get(nameB);
			if (nodes == null) {
				nodes = new HashSet<Node>();
				graph.put(nameB, nodes);
			}
			Node node = new Node(nameA);
			nodes.add(node);
			nodeSet.add(node);
		}
		debug();
	}

	/**
	 * First, try to add new node to graph.
	 * 
	 * Then find out whether containing chains by DFS traversing: Traverse the
	 * reqBy graph and exBy graph respectively to gather all the nodes depended
	 * by nameB(dependent is regarded as transitive).
	 * 
	 * Finally, check whether the two dependent set(require and exclude) has
	 * intersections, which means contain constraint chains.
	 * 
	 * If contains, remove new node from graph, return false; otherwise, return
	 * true.
	 * 
	 * @param constraint
	 * @return true if the constrain is added; otherwise false
	 */
	public boolean addConstraint(Constraint constraint) {
		Node newNode = null;

		String nameB = constraint.getFeatureB();
		ConstraintType type = constraint.getConstraintType();
		if (type == ConstraintType.require) {

			newNode = new Node(constraint.getFeatureA());

			HashSet<Node> nodes = reqBy.get(nameB);
			if (nodes == null) {
				nodes = new HashSet<Node>();
				reqBy.put(nameB, nodes);
			}
			reqBy.get(nameB).add(newNode);
			nodeSet.add(newNode);
			if (!exBy.containsKey(nameB)) {
				return true;
			}

		} else {
			newNode = new Node(constraint.getFeatureA());

			HashSet<Node> nodes = exBy.get(nameB);
			if (nodes == null) {
				nodes = new HashSet<Node>();
				exBy.put(nameB, nodes);
			}
			exBy.get(nameB).add(newNode);
			nodeSet.add(newNode);
			if (!reqBy.containsKey(nameB)) {
				return true;
			}
		}

		HashSet<Node> allReqByNodes = dfs(nameB, reqBy);
		HashSet<Node> allExNodes = dfs(nameB, exBy);
		resetVisit();

		boolean contains = false;
		for (Node node : allReqByNodes) {
			if (allExNodes.contains(node)) {
				contains = true;
				break;
			}
		}

		if (contains)
			if (type == ConstraintType.require) {
				reqBy.get(nameB).remove(newNode);
				nodeSet.remove(newNode);
			} else {
				exBy.get(nameB).remove(newNode);
				nodeSet.remove(newNode);
			}

		boolean added = !contains;
		debug();
		return added;
	}

	public void removeConstraint(Constraint constraint) {
		Node node = new Node(constraint.getFeatureA());
		nodeSet.remove(node);
		if (constraint.getConstraintType() == ConstraintType.require) {
			HashSet<Node> nodes = reqBy.get(constraint.getFeatureB());
			if (nodes != null) {
				nodes.remove(node);
			}
		} else {
			HashSet<Node> nodes = exBy.get(constraint.getFeatureB());
			if (nodes != null) {
				nodes.remove(node);
			}
		}

	}

	private HashSet<Node> dfs(String nameB, Map<String, HashSet<Node>> graph) {
		HashSet<Node> allNodes = new HashSet<Node>();

		HashSet<Node> nodes = graph.get(nameB);
		Stack<Node> stack = new Stack<Node>();
		for (Node n : nodes) {
			stack.push(n);
		}
		while (!stack.empty()) {
			Node popped = stack.pop();
			popped.visited = true;
			allNodes.add(popped);

			HashSet<Node> adjs = graph.get(popped.name);
			if (adjs != null) {
				for (Node n : adjs) {
					if (!n.visited) {
						stack.push(n);
					}
				}
			}
		}

		return allNodes;
	}

	private void resetVisit() {
		for (Node n : nodeSet) {
			n.visited = false;
		}
	}

	public void debug() {
		if (Debug) {
			System.out.println("Require: ");
			for (Entry<String, HashSet<Node>> entry : reqBy.entrySet()) {
				System.out.print(entry.getKey() + ": ");
				for (Node node : entry.getValue()) {
					System.out.print(node.name + ", ");
				}
				System.out.println();
			}
			System.out.println("Exclude: ");
			for (Entry<String, HashSet<Node>> entry : exBy.entrySet()) {
				System.out.print(entry.getKey() + ": ");
				for (Node node : entry.getValue()) {
					System.out.print(node.name + ", ");
				}
				System.out.println();
			}

		}
	}
}
