package edu.kit.kastel.vads.compiler.ir.node;

public final class MulNode extends BinaryOperationNode {
    public MulNode(Block block, Node left, Node right, int order) {
        super(block, left, right, order);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass") // we do, but not here
    @Override
    public boolean equals(Object obj) {
        return commutativeEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return commutativeHashCode(this);
    }
}
