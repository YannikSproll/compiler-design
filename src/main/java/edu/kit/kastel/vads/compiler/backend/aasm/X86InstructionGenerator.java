package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class X86InstructionGenerator {

    private static final String REGISTER_SEPARATOR = ", ";
    private static final String NEW_LINE = "\n";

    private final StringBuilder builder;

    public X86InstructionGenerator(StringBuilder builder) {
        this.builder = builder;
    }

    public X86InstructionGenerator generateIntConstInstruction(Register targetRegister, int constant) {
        generateIndentationSpace();
        builder.append("movq")
                .append(" $")
                .append(constant)
                .append(REGISTER_SEPARATOR)
                .append(targetRegister)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateReturnInstruction() {
        generateIndentationSpace();
        builder.append("ret")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateAdditionInstruction(Register sourceRegister, Register destinationRegister) {
        generateIndentationSpace();
        builder.append("addq")
                .append(" ")
                .append(sourceRegister)
                .append(", ")
                .append(destinationRegister)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSubtractionInstruction(Register sourceRegister, Register destinationRegister) {
        generateIndentationSpace();
        builder.append("subq")
                .append(" ")
                .append(sourceRegister)
                .append(", ")
                .append(destinationRegister)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateMultiplicationInstruction(Register sourceRegister, Register destinationRegister) {
        generateIndentationSpace();
        builder.append("multq")
                .append(" ")
                .append(sourceRegister)
                .append(", ")
                .append(destinationRegister)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateIntegerDivisionInstruction(Register divisorRegister) {
        generateIndentationSpace();
        builder.append("idivq")
                .append(" ")
                .append(divisorRegister)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSignExtendInstruction() {
        generateIndentationSpace();
        builder.append("CLTD")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateMoveInstruction(Register sourceRegister, Register destinationRegister) {
        generateIndentationSpace();
        builder.append("movq")
                .append(" ")
                .append(sourceRegister)
                .append(REGISTER_SEPARATOR)
                .append(destinationRegister)
                .append(NEW_LINE);
        return this;
    }

    private void generateIndentationSpace() {
        builder.repeat(" ", 2);
    }
}
