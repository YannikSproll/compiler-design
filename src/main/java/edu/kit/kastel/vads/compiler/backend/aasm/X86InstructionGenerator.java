package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.InstructionParameter;

public class X86InstructionGenerator {

    private static final String REGISTER_SEPARATOR = ", ";
    private static final String NEW_LINE = "\n";

    private final StringBuilder builder;
    private final X86InstructionLiteralFormatter formatter;

    public X86InstructionGenerator(StringBuilder builder) {
        this.builder = builder;
        this.formatter = new X86InstructionLiteralFormatter();
    }

    public X86InstructionGenerator generateIntConstInstruction(InstructionParameter targetRegister, int constant, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.MOV, bitSize))
                .append(" $")
                .append(constant)
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(targetRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateReturnInstruction() {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.RETURN, BitSize.BIT_64)) // Doesnt matter if 64 or 32. Return is equal for both
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateAdditionInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.ADD, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(", ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSubtractionInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.SUB, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(", ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateMultiplicationInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.MULT, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(", ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateIntegerDivisionInstruction(InstructionParameter divisorRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.DIV, bitSize))
                .append(" ")
                .append(formatInstructionParameter(divisorRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSignExtendInstruction(BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.SIGN_EXTEND, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateMoveInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.MOV, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    private String formatInstructionParameter(InstructionParameter parameter, BitSize bitSize) {
        String s = switch (parameter) {
            case X86Register reg -> formatter.formatRegisterName(reg, bitSize);
            case StackSlot ss -> "-" + ss.getLocalIndex() + "(" + formatter.formatRegisterName(X86Register.REG_BP, bitSize) + ")";
            case IntegerConstantParameter ic -> "$" + ic.getValue();
        };
        return s;
    }

    private void generateIndentationSpace() {
        builder.repeat(" ", 2);
    }
}
