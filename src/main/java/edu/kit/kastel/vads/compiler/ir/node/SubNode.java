package edu.kit.kastel.vads.compiler.ir.node;

public final class SubNode extends BinaryOperationNode {
    public SubNode(Block block, Node left, Node right, int order) {
        super(block, left, right, order);
    }
}
