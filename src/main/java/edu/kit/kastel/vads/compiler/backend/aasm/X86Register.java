package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

import java.util.Set;

public enum X86Register implements Register {
    REG_AX("%eax"),
    REG_BX("%ebx"),
    REG_CX("%ecx"),
    REG_DX("%edx"),
    REG_SI("%esi"),
    REG_DI("%edi"),
    REG_BP("%ebp"),
    REG_SP("%esp"),
    REG_8("%r8d"),
    REG_9("%r9d"),
    REG_10("%r10d"),
    REG_11("%r11d"),
    REG_12("%r12d"),
    REG_13("%r13d"),
    REG_14("%r14d"),
    REG_15("%r15d");

    private final String name;
    public String getName() {
        return name;
    }

    X86Register(String name) {
        this.name = name;
    }

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

    public String toString() {
        return name;
    }
}
