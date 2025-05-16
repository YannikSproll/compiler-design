package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;


public class AasmRegisterAllocator implements RegisterAllocator {

    private final LivenessAnalysis livenessAnalysis;

    public AasmRegisterAllocator(LivenessAnalysis livenessAnalysis) {
        this.livenessAnalysis = livenessAnalysis;
    }

    @Override
    public RegisterAllocationResult allocateRegisters(NodeSequence nodeSequence) {
        LivenessAnalysisResult livenessAnalysisResult = livenessAnalysis.analyzeLiveness(nodeSequence);

        InterferenceGraph interferenceGraph = InterferenceGraph.createFrom(nodeSequence, livenessAnalysisResult);

        List<Node> simplicialEliminationOrderedNodes = getSimplicialEliminationOrderedNodes(interferenceGraph);

        Map<Node, Integer> coloring = colorInterferenceGraph(interferenceGraph, simplicialEliminationOrderedNodes);

        return new RegisterAllocationResult(livenessAnalysisResult, mapColorsToRegisters(coloring));
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
        /*for (Node node : colors.keySet()) {
            switch (node) {
                case ReturnNode r -> {
                    Node resultPredecessor = predecessorSkipProj(r, ReturnNode.RESULT);
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
        }*/

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
}
