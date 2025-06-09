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

    public X86InstructionGenerator generateGlobal(String label) {
        builder.append(".global ")
                .append(label)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateFile(int fileNumber, String filename) {
        builder.append(".file ")
                .append(fileNumber)
                .append(" \"")
                .append(filename)
                .append("\"")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateLOCAnnotation(int fileNumber, int lineNumber, int columnNumber) {
        generateIndentationSpace();
        builder.append(".loc ")
                .append(fileNumber)
                .append(" ")
                .append(lineNumber)
                .append(" ")
                .append(columnNumber)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateLabel(String label) {
        builder.append(label)
                .append(":")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateEmptyLine() {
        builder.append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateText() {
        builder.append(".text")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSyscall() {
        generateIndentationSpace();
        builder.append("syscall")
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateCall(String label) {
        generateIndentationSpace();
        builder.append("call ")
                .append(label)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generatePushInstruction(InstructionParameter registerToPush, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.PUSH, bitSize))
                .append(" ")
                .append(formatInstructionParameter(registerToPush, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generatePopInstruction(InstructionParameter registerToPop, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.POP, bitSize))
                .append(" ")
                .append(formatInstructionParameter(registerToPop, bitSize))
                .append(NEW_LINE);
        return this;
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

    public X86InstructionGenerator generateLeftShiftInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.LEFT_SHIFT, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateRightShiftInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.RIGHT_SHIFT, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateComparisonInstruction(InstructionParameter leftRegister, InstructionParameter rightRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.COMPARISON, bitSize))
                .append(" ")
                .append(formatInstructionParameter(leftRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(rightRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateSetConditionCodeInstruction(InstructionParameter destinationRegister, X86ConditionCode conditionCode, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.SET_CONDITION_CODE, bitSize))
                .append(formatter.formatConditionCode(conditionCode))
                .append(" ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateBitwiseAndInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.BITWISE_AND, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateBitwiseOrInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.BITWISE_OR, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateBitwiseXorInstruction(InstructionParameter sourceRegister, InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.BITWISE_XOR, bitSize))
                .append(" ")
                .append(formatInstructionParameter(sourceRegister, bitSize))
                .append(REGISTER_SEPARATOR)
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateBitwiseNotInstruction(InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.BITWISE_NOT, bitSize))
                .append(" ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateNegationInstruction(InstructionParameter destinationRegister, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.NEGATION, bitSize))
                .append(" ")
                .append(formatInstructionParameter(destinationRegister, bitSize))
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateUnconditionalJumpInstruction(String label, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.UNCONDITIONAL_JUMP, bitSize))
                .append(" ")
                .append(label)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateConditionalJumpInstruction(X86ConditionCode conditionCode, String label, BitSize bitSize) {
        generateIndentationSpace();
        builder.append(formatter.formatInstruction(X86Instruction.CONDITIONAL_JUMP, bitSize))
                .append(formatter.formatConditionCode(conditionCode))
                .append(" ")
                .append(label)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateFromString(String instruction) {
        generateIndentationSpace();
        builder.append(instruction)
                .append(NEW_LINE);
        return this;
    }

    public X86InstructionGenerator generateComment(String comment, boolean generateIndentationSpace) {
        if (generateIndentationSpace) {
            generateIndentationSpace();
        }

        builder.append("# ")
                .append(comment)
                .append(NEW_LINE);
        return this;
    }

    private String formatInstructionParameter(InstructionParameter parameter, BitSize bitSize) {
        String s = switch (parameter) {
            case X86Register reg -> formatter.formatRegisterName(reg, bitSize);
            case StackSlot ss -> "-" + (ss.getLocalIndex() + 1) * 8 + "(" + formatter.formatRegisterName(X86Register.REG_BP, BitSize.BIT_64) + ")";
            case IntegerConstantParameter ic -> "$" + ic.getValue();
        };
        return s;
    }

    private void generateIndentationSpace() {
        builder.repeat(" ", 2);
    }
}
