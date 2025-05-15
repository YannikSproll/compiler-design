package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

import java.util.Set;

public enum X86Register implements Register {
    REG_AX("%rax"),
    REG_BX("%rbx"),
    REG_CX("%rcx"),
    REG_DX("%rdx"),
    REG_SI("%rsi"),
    REG_DI("%rdi"),
    REG_BP("%rbp"),
    REG_SP("%rsp"),
    REG_8("%r8"),
    REG_9("%r9"),
    REG_10("%r10"),
    REG_11("%r11"),
    REG_12("%r12"),
    REG_13("%r13"),
    REG_14("%r14"),
    REG_15("%r15");

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
