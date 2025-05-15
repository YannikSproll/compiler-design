package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public record VirtualRegister(int id) implements Register {

    private static final String[] Registers = new String[] {
            "%rcx", "%rsi", "%rdi", "%r8", "%r9", "%r10", "%r11", "%rax", "%rdx"
    };

    @Override
    public String toString() {
        return Registers[id];
    }
}
