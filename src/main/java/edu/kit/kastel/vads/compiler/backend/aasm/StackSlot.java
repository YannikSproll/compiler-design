package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public final class StackSlot implements Register {
    private int localIndex;

    public StackSlot(int localIndex) {
        this.localIndex = localIndex;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public String toString() {
        return "StackSlot_" + localIndex;
    }
}
