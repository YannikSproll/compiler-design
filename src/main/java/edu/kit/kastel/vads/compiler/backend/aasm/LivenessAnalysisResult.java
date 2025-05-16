package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public final class LivenessAnalysisResult {
    private final HashMap<Node, Set<Node>> livenessInformation;

    public LivenessAnalysisResult(HashMap<Node, Set<Node>> livenessInformation) {
        this.livenessInformation = livenessInformation;
    }

    public boolean isLiveAt(Node node, Node checkIfLiveNode) {
        if (!livenessInformation.containsKey(node)) {
            throw new IllegalArgumentException("Node " + node + " is not in liveness information");
        }

        return livenessInformation.get(node).contains(checkIfLiveNode);
    }

    public Set<Node> getLiveNodesAt(Node node) {
        if (!livenessInformation.containsKey(node)) {
            throw new IllegalArgumentException("Node " + node + " is not in liveness information");
        }
        return Collections.unmodifiableSet(livenessInformation.get(node));
    }
}
