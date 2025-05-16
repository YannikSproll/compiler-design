package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class LivenessAnalysis {
    public LivenessAnalysisResult analyzeLiveness(NodeSequence nodeSequence) {
        HashMap<Node, Set<Node>> liveNodes = new HashMap<>();
        Set<Node> liveAtSuccessor = new HashSet<>();
        for (Node node : nodeSequence.getSequence().reversed()) {
            analyzeLivenessRecursive(node, liveNodes, liveAtSuccessor);
            liveAtSuccessor = liveNodes.get(node);
        }

        return new LivenessAnalysisResult(liveNodes);
    }

    private void analyzeLivenessRecursive(Node node, HashMap<Node, Set<Node>> liveNodes, Set<Node> liveAtSuccessor) {
        switch (node) {
            case ReturnNode r -> {
                Set<Node> currentlyLive = Set.of(predecessorSkipProj(r, ReturnNode.RESULT));
                liveNodes.putIfAbsent(r, currentlyLive);
            }
            case BinaryOperationNode b -> {
                Set<Node> currentlyLive = new HashSet<>();
                currentlyLive.add(predecessorSkipProj(b, BinaryOperationNode.LEFT));
                currentlyLive.add(predecessorSkipProj(b, BinaryOperationNode.RIGHT));
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
}
