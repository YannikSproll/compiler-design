package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAllocator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.data.IrFunction;
import edu.kit.kastel.vads.compiler.ir.data.SSAValue;
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
    public RegisterAllocationResult allocateRegisters(IrFunction irFunction) {
        LivenessAnalysisResult livenessAnalysisResult = livenessAnalysis.run(irFunction);

        InterferenceGraph interferenceGraph = InterferenceGraph.createFrom(irFunction.blocks(), livenessAnalysisResult);

        List<SSAValue> simplicialEliminationOrderedNodes = getSimplicialEliminationOrderedNodes(interferenceGraph);

        Map<SSAValue, Integer> coloring = colorInterferenceGraph(interferenceGraph, simplicialEliminationOrderedNodes);

        ColorToRegisterMappingResult mappingResult = mapColorsToRegisters(coloring);
        return new RegisterAllocationResult(livenessAnalysisResult, mappingResult.mapping(), mappingResult.tempRegister(), mappingResult.registers());
    }




    private List<SSAValue> getSimplicialEliminationOrderedNodes(
            InterferenceGraph interferenceGraphRef) {
        InterferenceGraph interferenceGraph = interferenceGraphRef.copy();

        List<SSAValue> simplicialEliminationOrderedNodes = new ArrayList<>();
        Map<SSAValue, Integer> nodeWeights = interferenceGraph
                .getNodes()
                .stream()
                .collect(Collectors.toMap(Function.identity(),_ -> 0));

        Optional<SSAValue> maximumCardinalityNode = interferenceGraph.getMaximumCardinalityNode(nodeWeights);

        while (maximumCardinalityNode.isPresent()) {
            simplicialEliminationOrderedNodes.add(maximumCardinalityNode.get());
            for (SSAValue neighbor : interferenceGraph.neighborsOf(maximumCardinalityNode.get())) {
                nodeWeights.put(neighbor, nodeWeights.get(neighbor) + 1);
            }
            interferenceGraph.removeNode(maximumCardinalityNode.get());

            maximumCardinalityNode = interferenceGraph.getMaximumCardinalityNode(nodeWeights);
        }

        return simplicialEliminationOrderedNodes;
    }

    private Map<SSAValue, Integer> colorInterferenceGraph(InterferenceGraph interferenceGraphRef, List<SSAValue> simplicialEliminationOrderedNodes) {
        Map<SSAValue, Optional<Integer>> coloring = simplicialEliminationOrderedNodes
                .stream()
                .collect(Collectors.toMap(x -> x, _ -> Optional.empty()));
        int maxUsedColor = 0;

        for (SSAValue node : simplicialEliminationOrderedNodes) {
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

    private ColorToRegisterMappingResult mapColorsToRegisters(Map<SSAValue, Integer> colors) {
        Map<Integer, HashSet<SSAValue>> invertedMap = colors.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey,
                                Collectors.toCollection(HashSet::new))));
        Map<SSAValue, Register> colorToRegisterMapping = new HashMap<>();

        Set<SSAValue> remainingNodes = new HashSet<>(colors.keySet());

        Set<Register> remainingRegisters = new HashSet<>(X86Register.getGeneralPurposeRegisters());

        Register tempRegister = remainingRegisters.stream().findFirst().get();
        remainingRegisters.remove(tempRegister); // Remove one register which can later be used as a temp register

        HashSet<Register> registers = new HashSet<>();

        int numberOfStackSlots = 0;
        // Get
        while (!remainingNodes.isEmpty()) {
            SSAValue node = remainingNodes.stream().findFirst().get();
            Optional<Register> nextRegister = remainingRegisters.stream().findFirst();

            Register nodeRegister;
            if (nextRegister.isPresent()) {
                nodeRegister = nextRegister.get();
            } else {
                nodeRegister = new StackSlot(numberOfStackSlots);
                numberOfStackSlots++;
            }

            registers.add(nodeRegister);

            Integer color = colors.get(node);
            HashSet<SSAValue> affectedNodes = invertedMap.get(color);
            for (SSAValue affectedNode : affectedNodes) {
                remainingNodes.remove(affectedNode);
                colorToRegisterMapping.put(affectedNode, nodeRegister);
            }

            remainingNodes.remove(node);
            remainingRegisters.remove(nodeRegister);
        }

        return new ColorToRegisterMappingResult(colorToRegisterMapping, tempRegister, registers);
    }

    private record ColorToRegisterMappingResult(Map<SSAValue, Register> mapping, Register tempRegister, Set<Register> registers) { }
}
