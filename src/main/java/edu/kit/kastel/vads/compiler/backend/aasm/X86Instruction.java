package edu.kit.kastel.vads.compiler.backend.aasm;

public enum X86Instruction {
    MOV,
    ADD,
    SUB,
    MULT,
    DIV,
    SIGN_EXTEND,
    RETURN,
    PUSH,
    POP,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    COMPARISON,
    SET_CONDITION_CODE,
    BITWISE_AND,
    BITWISE_OR,
    BITWISE_XOR,
    BITWISE_NOT,
    NEGATION,
    UNCONDITIONAL_JUMP,
    CONDITIONAL_JUMP
}
