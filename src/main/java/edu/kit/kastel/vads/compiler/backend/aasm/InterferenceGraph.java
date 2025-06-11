package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.IrBlock;
import edu.kit.kastel.vads.compiler.ir.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.SSAValue;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.IrValueProducingInstruction;

import java.util.*;
import java.util.stream.Collectors;

public final class InterferenceGraph {
    private final Map<SSAValue, HashSet<SSAValue>> adjacencyList;

    public InterferenceGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(SSAValue n) {
        if (!adjacencyList.containsKey(n)) {
            adjacencyList.put(n, new HashSet<>());
        }
    }

    public Set<SSAValue> getNodes() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    public void addEdge(SSAValue from, SSAValue to) {
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

    public Set<SSAValue> neighborsOf(SSAValue n) {
        if (!adjacencyList.containsKey(n)) {
            throw new IllegalArgumentException("Graph does not contain node " + n);
        }

        return Collections.unmodifiableSet(adjacencyList.get(n));
    }

    public void removeNode(SSAValue n) {
        for (SSAValue nn : adjacencyList.get(n)) {
            HashSet<SSAValue> nnEdges = adjacencyList.get(nn);
            nnEdges.remove(n);
        }
        adjacencyList.remove(n);
    }

    public Optional<SSAValue> getMaximumCardinalityNode(Map<SSAValue, Integer> cardinalities) {
        return adjacencyList
                .entrySet()
                .stream()
                .max(Comparator.comparingInt(x -> cardinalities.get(x.getKey())))
                .map(Map.Entry::getKey);
    }

    private InterferenceGraph(Map<SSAValue, HashSet<SSAValue>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public InterferenceGraph copy() {
        Map<SSAValue, HashSet<SSAValue>> adjacencyListCopy = adjacencyList
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new HashSet<>(entry.getValue())));

        return new InterferenceGraph(adjacencyListCopy);
    }

    public static InterferenceGraph createFrom(Collection<IrBlock> blocks, LivenessAnalysisResult livenessAnalysisResult) {
        InterferenceGraph interferenceGraph = new InterferenceGraph();

        // Add all nodes
        for (IrBlock block : blocks) {
            for (IrInstruction instruction : block.getInstructions()) {
                if (instruction instanceof IrValueProducingInstruction valueProducingInstruction) {
                    interferenceGraph.addNode(valueProducingInstruction.target());
                }
            }
        }

        // Add edges
        for (IrBlock block : blocks) {
            Set<SSAValue> liveAtSuccessor = livenessAnalysisResult.getBlockLiveOut(block);
            for (IrInstruction instruction : block.getInstructions().reversed()) {
                if (instruction instanceof IrValueProducingInstruction valueProducingInstruction) {
                    liveAtSuccessor
                            .stream()
                            .filter(x -> valueProducingInstruction.target() != x)
                            .forEach(x -> interferenceGraph.addEdge(x, valueProducingInstruction.target()));
                }

                liveAtSuccessor = livenessAnalysisResult.getLiveNodesAt(instruction);
            }
        }

        return interferenceGraph;
    }
}
