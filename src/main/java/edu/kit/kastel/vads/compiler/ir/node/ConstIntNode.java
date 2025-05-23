package edu.kit.kastel.vads.compiler.ir.node;

public final class ConstIntNode extends Node {
    private final int value;

    public ConstIntNode(Block block, int value, int order) {
        super(block, order);
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConstIntNode c) {
            return this.block() == c.block() && c.value == this.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    protected String info() {
        return "[" + this.value + "]";
    }
}
