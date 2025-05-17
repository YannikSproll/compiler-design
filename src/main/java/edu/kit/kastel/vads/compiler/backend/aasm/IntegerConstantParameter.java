package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionParameter;

public final class IntegerConstantParameter implements InstructionParameter {
    private final int value;

    public IntegerConstantParameter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
