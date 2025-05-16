package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.*;

public final class NodeSequence {
    private final List<Node> totallyOrderedNodes;
    private final Map<Node, NodeInformationItem> _nodeMetaInformation;

    public NodeSequence(List<Node> totallyOrderedNodes, Map<Node, NodeInformationItem> nodeMetaInformation) {
        this.totallyOrderedNodes = totallyOrderedNodes;
        this._nodeMetaInformation = nodeMetaInformation;
    }

    public List<Node> getSequence() {
        return Collections.unmodifiableList(totallyOrderedNodes);
    }

    public int getIndexOf(Node node) {
        failIfNodeNotInSequence(node);

        return _nodeMetaInformation.get(node).index();
    }

    public Optional<Node> getPredecessor(Node node) {
        failIfNodeNotInSequence(node);

        return _nodeMetaInformation.get(node).predecessor();
    }

    public Optional<Node> getSuccessor(Node node) {
        failIfNodeNotInSequence(node);

        return _nodeMetaInformation.get(node).successor();
    }

    private void failIfNodeNotInSequence(Node node) {
        if (!_nodeMetaInformation.containsKey(node)) {
            throw new IllegalArgumentException("Node " + node + " not found");
        }
    }

    private record NodeInformationItem(int index, Optional<Node> predecessor, Optional<Node> successor) { }

    public static NodeSequence createFrom(List<Node> totallyOrderedNodes) {
        Map<Node, NodeInformationItem> nodeMetaInformation = new HashMap<>();

        for (int i = 0; i < totallyOrderedNodes.size(); i++) {
            Node current = totallyOrderedNodes.get(i);
            Optional<Node> predecessor = i > 0 ? Optional.of(totallyOrderedNodes.get(i - 1)) : Optional.empty();
            Optional<Node> successor = i < totallyOrderedNodes.size() - 1 ? Optional.of(totallyOrderedNodes.get(i + 1)) : Optional.empty();

            NodeInformationItem infoItem = new NodeInformationItem(i, predecessor, successor);
            nodeMetaInformation.put(current, infoItem);
        }

        return new NodeSequence(totallyOrderedNodes, nodeMetaInformation);
    }
}
