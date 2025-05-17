package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

import java.util.Set;

public enum X86Register implements Register {
    REG_AX,
    REG_BX,
    REG_CX,
    REG_DX,
    REG_SI,
    REG_DI,
    REG_BP,
    REG_SP,
    REG_8,
    REG_9,
    REG_10,
    REG_11,
    REG_12,
    REG_13,
    REG_14,
    REG_15;

    public static Set<Register> getGeneralPurposeRegisters() {
        return Set.of(
                REG_BX,
                REG_CX,
                REG_SI,
                REG_DI,
                REG_8,
                REG_9,
                REG_10,
                REG_11,
                REG_12,
                REG_13,
                REG_14,
                REG_15);
    }
}
