package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;



public class AasmRegisterAllocator implements RegisterAllocator {
    private int id;

    private final Map<Node, Register> registers = new HashMap<>();

    @Override
    public Map<Node, Register> allocateRegisters(List<Node> totallyOrderedNodes) {
        HashMap<Node, Set<Node>> liveNodes = analyzeLivenessAbsolute(totallyOrderedNodes);

        InterferenceGraph interferenceGraph = buildInterferenceGraph(totallyOrderedNodes, liveNodes);

        List<Node> simplicialEliminationOrderedNodes = getSimplicialEliminationOrderedNodes(interferenceGraph);

        Map<Node, Integer> coloring = colorInterferenceGraph(interferenceGraph, simplicialEliminationOrderedNodes);

        return mapColorsToRegisters(coloring);
    }

    private InterferenceGraph buildInterferenceGraph(List<Node> totallyOrderedNodes, HashMap<Node, Set<Node>> liveNodes) {
        InterferenceGraph interferenceGraph = new InterferenceGraph();
        totallyOrderedNodes.forEach(interferenceGraph::addNode);

        Set<Node> liveAtSuccessor = Set.of();
        for (Node node : totallyOrderedNodes.reversed()) {
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
            liveAtSuccessor = liveNodes.get(node);
        }

        return interferenceGraph;
    }


    private HashMap<Node, Set<Node>> analyzeLivenessAbsolute(List<Node> totallyOrderedNodes) {
        HashMap<Node, Set<Node>> liveNodes = new HashMap<>();
        Set<Node> liveAtSuccessor = new HashSet<>();
        for (Node node : totallyOrderedNodes.reversed()) {
            analyze_liveness_recursive_absolute(node, liveNodes, liveAtSuccessor);
            liveAtSuccessor = liveNodes.get(node);
        }
        return liveNodes;
    }

    private List<Node> getSimplicialEliminationOrderedNodes(
            InterferenceGraph interferenceGraphRef) {
        InterferenceGraph interferenceGraph = interferenceGraphRef.copy();

        List<Node> simplicialEliminationOrderedNodes = new ArrayList<>();
        Map<Node, Integer> nodeWeights = interferenceGraph
                .getNodes()
                .stream()
                .collect(Collectors.toMap(Function.identity(),_ -> 0));
        Optional<Node> maximumCardinalityNode = interferenceGraph.getMaximumCardinalityNode();

        while (maximumCardinalityNode.isPresent()) {
            simplicialEliminationOrderedNodes.add(maximumCardinalityNode.get());
            for (Node neighbor : interferenceGraph.neighborsOf(maximumCardinalityNode.get())) {
                nodeWeights.put(neighbor, nodeWeights.get(neighbor) + 1);
            }
            interferenceGraph.removeNode(maximumCardinalityNode.get());

            maximumCardinalityNode = interferenceGraph.getMaximumCardinalityNode();
        }

        return simplicialEliminationOrderedNodes;
    }

    private Map<Node, Integer> colorInterferenceGraph(InterferenceGraph interferenceGraphRef, List<Node> simplicialEliminationOrderedNodes) {
        Map<Node, Optional<Integer>> coloring = simplicialEliminationOrderedNodes
                .stream()
                .collect(Collectors.toMap(x -> x, _ -> Optional.empty()));
        int maxUsedColor = 0;

        for (Node node : simplicialEliminationOrderedNodes) {
            Set<Integer> usedNeighborColors = interferenceGraphRef
                    .neighborsOf(node)
                    .stream()
                    .map(x -> coloring.getOrDefault(x, Optional.empty()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            boolean colorFound = false;
            for (int i = 0; i <= maxUsedColor; i++) {
                if (!usedNeighborColors.contains(i)) {
                    colorFound = true;
                    coloring.put(node, Optional.of(i));
                    break;
                }
            }

            if (!colorFound) {
                maxUsedColor++;
                coloring.put(node, Optional.of(maxUsedColor));
            }
        }

        return coloring
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    private Map<Node, Register> mapColorsToRegisters(Map<Node, Integer> colors) {
        Map<Integer, HashSet<Node>> invertedMap = colors.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey,
                                Collectors.toCollection(HashSet::new))));
        Map<Node, Register> registers = new HashMap<>();

        Set<Node> remainingNodes = colors.keySet().stream().collect(Collectors.toSet());

        // Do precoloring for return node
        for (Node node : colors.keySet()) {
            switch (node) {
                case ReturnNode r -> {
                    Node resultPredecessor = r.predecessor(ReturnNode.RESULT);
                    Integer color = colors.get(resultPredecessor);
                    HashSet<Node> affectedNodes = invertedMap.get(color);
                    for (Node affectedNode : affectedNodes) {
                        remainingNodes.remove(affectedNode);
                        registers.put(affectedNode, X86Register.REG_AX);
                    }
                    remainingNodes.remove(node);
                    registers.put(node, X86Register.REG_AX);
                }
                default -> {}
            }
        }

        Set<Register> remainingRegisters = new HashSet<>(X86Register.getGeneralPurposeRegisters());
        while (!remainingNodes.isEmpty()) {
            Node node = remainingNodes.stream().findFirst().get();
            Register nodeRegister = remainingRegisters.stream().findFirst().get();

            Integer color = colors.get(node);
            HashSet<Node> affectedNodes = invertedMap.get(color);
            for (Node affectedNode : affectedNodes) {
                remainingNodes.remove(affectedNode);
                registers.put(affectedNode, nodeRegister);
            }

            remainingNodes.remove(node);
            remainingRegisters.remove(nodeRegister);
        }

        return registers;
    }

    private void analyze_liveness_recursive_absolute(Node node, HashMap<Node, Set<Node>> liveNodes, Set<Node> liveAtSuccessor) {
        switch (node) {
            case ReturnNode r -> {
                Set<Node> currentlyLive = Set.of(r.predecessor(ReturnNode.RESULT));
                liveNodes.putIfAbsent(r, currentlyLive);
            }
            case BinaryOperationNode b -> {
                Set<Node> currentlyLive = new HashSet<>();
                currentlyLive.add(b.predecessor(BinaryOperationNode.LEFT));
                currentlyLive.add(b.predecessor(BinaryOperationNode.RIGHT));
                currentlyLive.addAll(liveAtSuccessor
                        .stream()
                        .filter(u -> u != node)
                        .collect(Collectors.toSet()));
                liveNodes.put(node, currentlyLive);
            }
            case ConstIntNode c -> {
                Set<Node> currentlyLive = liveAtSuccessor
                        .stream()
                        .filter(u -> u != node)
                        .collect(Collectors.toSet());

                liveNodes.put(node, currentlyLive);
            }
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        };
    }



    private void scan(Node node, Set<Node> visited) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited);
            }
        }
        if (needsRegister(node)) {
            this.registers.put(node, new VirtualRegister(this.id++));
        }
    }

    private static boolean needsRegister(Node node) {
        return !(node instanceof ProjNode || node instanceof StartNode || node instanceof Block || node instanceof ReturnNode);
    }

    private void analyze_liveness_relative(IrGraph graph) {
        Node endBlock = graph.endBlock();
        HashMap<Node, Set<Node>> liveNodes = new HashMap<>();
        analyze_liveness_recursive_relative(endBlock.predecessor(0), liveNodes, new HashSet<>()); // Start with return statement of block
        int i = 5;
    }

    private void analyze_liveness_recursive_relative(Node node, HashMap<Node, Set<Node>> liveNodes, Set<Node> liveAtSuccessor) {
        switch (node) {
            case ReturnNode r -> {
                Set<Node> currentlyLive = Set.of(r.predecessor(ReturnNode.RESULT));
                liveNodes.putIfAbsent(r, currentlyLive);
                analyze_liveness_recursive_relative(r.predecessor(ReturnNode.RESULT), liveNodes, currentlyLive);
            }
            case BinaryOperationNode b -> {
                Set<Node> currentlyLive = liveNodes.getOrDefault(node, new HashSet<>());
                Node left = b.predecessor(BinaryOperationNode.LEFT);
                Node right = b.predecessor(BinaryOperationNode.RIGHT);
                currentlyLive.add(left);
                currentlyLive.add(right);
                currentlyLive.addAll(liveAtSuccessor
                        .stream()
                        .filter(u -> u != node)
                        .collect(Collectors.toSet()));
                liveNodes.put(node, currentlyLive);

                analyze_liveness_recursive_relative(left, liveNodes, currentlyLive);
                analyze_liveness_recursive_relative(right, liveNodes, currentlyLive);
            }
            case ConstIntNode c -> {
                Set<Node> currentlyLive = liveNodes.getOrDefault(node, new HashSet<>());
                currentlyLive.addAll(liveAtSuccessor
                        .stream()
                        .filter(u -> u != node)
                        .collect(Collectors.toSet()));
                liveNodes.put(node, currentlyLive);
            }
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        };
    }
}
