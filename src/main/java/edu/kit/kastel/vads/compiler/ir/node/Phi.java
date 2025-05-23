package edu.kit.kastel.vads.compiler.ir.node;

public final class Phi extends Node {
    public Phi(Block block, int order) {
        super(block, order);
    }

    public void appendOperand(Node node) {
        addPredecessor(node);
    }
}
