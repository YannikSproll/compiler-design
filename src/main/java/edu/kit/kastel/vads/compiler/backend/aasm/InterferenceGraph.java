package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public final class InterferenceGraph {
    private final Map<Node, HashSet<Node>> adjacencyList;

    public InterferenceGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(Node n) {
        if (!adjacencyList.containsKey(n)) {
            adjacencyList.put(n, new HashSet<>());
        }
    }

    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    public void addEdge(Node from, Node to) {
        if (from == to) {
            throw new IllegalArgumentException("Cannot add edge to the same node"); // Graph is irreflexive
        }

        if (!adjacencyList.containsKey(from)) {
            throw new IllegalArgumentException("Graph does not contain from node");
        }

        if (!adjacencyList.containsKey(to)) {
            throw new IllegalArgumentException("Graph does not contain to node");
        }

        adjacencyList.get(from).add(to);
        adjacencyList.get(to).add(from); // Undirected graph
    }

    public Set<Node> neighborsOf(Node n) {
        if (!adjacencyList.containsKey(n)) {
            throw new IllegalArgumentException("Graph does not contain node " + n);
        }

        return Collections.unmodifiableSet(adjacencyList.get(n));
    }

    public void removeNode(Node n) {
        for (Node nn : adjacencyList.get(n)) {
            HashSet<Node> nnEdges = adjacencyList.get(nn);
            nnEdges.remove(n);
        }
        adjacencyList.remove(n);
    }

    public Optional<Node> getMaximumCardinalityNode() {
        return adjacencyList
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(x -> x.getValue().size()))
                .map(Map.Entry::getKey);
    }

    private InterferenceGraph(Map<Node, HashSet<Node>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public InterferenceGraph copy() {
        Map<Node, HashSet<Node>> adjacencyListCopy = adjacencyList
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new HashSet<>(entry.getValue())));

        return new InterferenceGraph(adjacencyListCopy);
    }

    public static InterferenceGraph createFrom(NodeSequence nodeSequence, LivenessAnalysisResult livenessAnalysisResult) {
        InterferenceGraph interferenceGraph = new InterferenceGraph();
        nodeSequence.getSequence().forEach(interferenceGraph::addNode);

        Set<Node> liveAtSuccessor = Set.of();
        for (Node node : nodeSequence.getSequence().reversed()) {
            switch (node) {
                case BinaryOperationNode b -> {
                    liveAtSuccessor
                            .stream()
                            .filter(u -> node != u)
                            .forEach(u -> interferenceGraph.addEdge(node, u));
                }
                case ConstIntNode c -> {
                    liveAtSuccessor
                            .stream()
                            .filter(u -> node != u)
                            .forEach(u -> interferenceGraph.addEdge(node, u));
                }
                default -> {
                }
            }
            liveAtSuccessor = livenessAnalysisResult.getLiveNodesAt(node);
        }

        return interferenceGraph;
    }
}
