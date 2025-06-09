package edu.kit.kastel.vads.compiler.backend.aasm;

import java.util.HashMap;
import java.util.Map;

public final class X86InstructionLiteralFormatter {
    private static final Map<X86Register, String> BIT_8_REGISTER_NAME_MAP;
    static {
        BIT_8_REGISTER_NAME_MAP = new HashMap<>();
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_AX, "%al");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_BX, "%bl");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_CX, "%cl");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_DX, "%dl");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_SI, "%sil");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_DI, "%dil");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_BP, "%bpl");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_SP, "%spl");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_8, "%r8b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_9, "%r9b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_10, "%r10b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_11, "%r11b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_12, "%r12b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_13, "%r13b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_14, "%r14b");
        BIT_8_REGISTER_NAME_MAP.put(X86Register.REG_15, "%r15b");
    }

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
            case BIT_8 -> BIT_8_REGISTER_NAME_MAP.get(register);
            case BIT_32 -> BIT_32_REGISTER_NAME_MAP.get(register);
            case BIT_64 -> BIT_64_REGISTER_NAME_MAP.get(register);
        };
    }

    private static final Map<X86Instruction, String> BIT_8_INSTRUCTION_NAME_MAP;
    static {
        BIT_8_INSTRUCTION_NAME_MAP = new HashMap<>();
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.MOV, "movb");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.ADD, "addb");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.SUB, "subb");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.MULT, "imulb");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.DIV, "idivb");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.SIGN_EXTEND, "cbw");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.RETURN, "ret");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.PUSH, "push");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.POP, "pop");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.LEFT_SHIFT, "sal");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.RIGHT_SHIFT, "sar");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.COMPARISON, "cmp");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.SET_CONDITION_CODE, "set");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_AND, "and");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_OR, "or");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_XOR, "xor");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_NOT, "not");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.NEGATION, "neg");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.UNCONDITIONAL_JUMP, "jmp");
        BIT_8_INSTRUCTION_NAME_MAP.put(X86Instruction.CONDITIONAL_JUMP, "j");
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
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.LEFT_SHIFT, "sal");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.RIGHT_SHIFT, "sar");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.COMPARISON, "cmp");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.SET_CONDITION_CODE, "set");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_AND, "and");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_OR, "or");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_XOR, "xor");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_NOT, "not");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.NEGATION, "neg");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.UNCONDITIONAL_JUMP, "jmp");
        BIT_32_INSTRUCTION_NAME_MAP.put(X86Instruction.CONDITIONAL_JUMP, "j");
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
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.LEFT_SHIFT, "sal");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.RIGHT_SHIFT, "sar");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.COMPARISON, "cmp");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.SET_CONDITION_CODE, "set");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_AND, "and");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_OR, "or");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_XOR, "xor");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.BITWISE_NOT, "not");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.NEGATION, "neg");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.UNCONDITIONAL_JUMP, "jmp");
        BIT_64_INSTRUCTION_NAME_MAP.put(X86Instruction.CONDITIONAL_JUMP, "j");
    }

    public String formatInstruction(X86Instruction instruction, BitSize bitSize) {
        return switch (bitSize) {
            case BIT_8 -> BIT_8_INSTRUCTION_NAME_MAP.get(instruction);
            case BIT_32 -> BIT_32_INSTRUCTION_NAME_MAP.get(instruction);
            case BIT_64 -> BIT_64_INSTRUCTION_NAME_MAP.get(instruction);
        };
    }

    private static final Map<X86ConditionCode, String> CONDITION_CODE_NAME_MAP;
    static {
        CONDITION_CODE_NAME_MAP = new HashMap<>();
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.EQUAL, "e");
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.NOT_EQUAL, "ne");
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.LESS_THAN, "l");
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.LESS_THAN_OR_EQUAL, "le");
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.GREATER_THAN, "g");
        CONDITION_CODE_NAME_MAP.put(X86ConditionCode.GREATER_THAN_OR_EQUAL, "ge");
    }

    public String formatConditionCode(X86ConditionCode conditionCode) {
        return CONDITION_CODE_NAME_MAP.get(conditionCode);
    }

     public X86InstructionLiteralFormatter() {}
}
