package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.*;
import java.util.stream.Collectors;

public class DebugNodeSequenceAnalysis implements NodeSequenceAnalysis {
    @Override
    public NodeSequence sequenceNodes(IrGraph graph) {
        Node endBlock = graph.endBlock();
        List<Node> totallyOrderedNodes = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        sequenceNodesRecursive(endBlock, visited, totallyOrderedNodes);
        return NodeSequence.createFrom(totallyOrderedNodes.stream().sorted(Comparator.comparingInt(Node::order)).toList());
    }


    private void sequenceNodesRecursive(Node node, Set<Node> visited, List<Node> orderedNodes) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                sequenceNodesRecursive(predecessor, visited, orderedNodes);
            }
        }

        switch (node) {
            case ReturnNode _, BinaryOperationNode _, ConstIntNode _ -> {
                orderedNodes.add(node);
            }
            case Block _, ProjNode _, StartNode _, Phi _ -> {
            }
        };
    }
}
