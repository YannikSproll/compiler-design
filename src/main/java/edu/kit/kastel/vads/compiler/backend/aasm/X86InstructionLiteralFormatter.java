package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.HashMap;
import java.util.Map;

public final class X86InstructionLiteralFormatter {
    private static final Map<X86Register, String> BIT_64_REGISTER_NAME_MAP;
    static {
        BIT_64_REGISTER_NAME_MAP = new HashMap<>();
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_AX, "%rax");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_BX, "%rbx");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_CX, "%rcx");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_DX, "%rdx");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_SI, "%rsi");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_DI, "%rdi");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_BP, "%rbp");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_SP, "%rsp");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_8, "%r8");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_9, "%r9");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_10, "%r10");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_11, "%r11");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_12, "%r12");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_13, "%r13");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_14, "%r14");
        BIT_64_REGISTER_NAME_MAP.put(X86Register.REG_15, "%r15");
    }

    private static final Map<X86Register, String> BIT_32_REGISTER_NAME_MAP;

    static {
        BIT_32_REGISTER_NAME_MAP = new HashMap<>();
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_AX, "%eax");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_BX, "%ebx");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_CX, "%ecx");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_DX, "%edx");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_SI, "%esi");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_DI, "%edi");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_BP, "%ebp");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_SP, "%esp");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_8, "%r8d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_9, "%r9d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_10, "%r10d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_11, "%r11d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_12, "%r12d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_13, "%r13d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_14, "%r14d");
        BIT_32_REGISTER_NAME_MAP.put(X86Register.REG_15, "%r15d");
    }

    public String formatRegisterName(X86Register register, BitSize bitSize) {
        return switch (bitSize) {
            case BIT_32 -> BIT_32_REGISTER_NAME_MAP.get(register);
            case BIT_64 -> BIT_64_REGISTER_NAME_MAP.get(register);
        };
    }

    private static final Map<X86Instruction, String> BIT_32_INSTRUCTION_NAME_MAP;
    static {
        BIT_32_INSTRUCTION_NAME_MAP = new HashMap<>();
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.MOV, "movl");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.ADD, "addl");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.SUB, "subl");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.MULT, "imull");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.DIV, "idivl");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.SIGN_EXTEND, "cltd");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.RETURN, "ret");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.PUSH, "push");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.POP, "pop");
    }

    private static final Map<X86Instruction, String> BIT_64_INSTRUCTION_NAME_MAP;
    static {
        BIT_64_INSTRUCTION_NAME_MAP = new HashMap<>();
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.MOV, "movq");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.ADD, "addq");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.SUB, "subq");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.MULT, "imulq");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.DIV, "idivq");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.SIGN_EXTEND, "cqo");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.RETURN, "ret");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.PUSH, "push");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.POP, "pop");
    }

    public String formatInstruction(X86Instruction instruction, BitSize bitSize) {
        return switch (bitSize) {
            case BIT_32 -> BIT_32_INSTRUCTION_NAME_MAP.get(instruction);
            case BIT_64 -> BIT_64_INSTRUCTION_NAME_MAP.get(instruction);
        };
    }

     public X86InstructionLiteralFormatter() {}
}
