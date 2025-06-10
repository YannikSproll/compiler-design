package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;

import java.util.HashSet;

public class IrFunctionPrinter {

    public void print(IrFile file) {
        for (IrFunction function : file.functions()) {
            print(function);
        }
    }

    public void print(IrFunction function) {
        HashSet<IrBlock> processedBlocks = new HashSet<>();
        StringBuilder builder = new StringBuilder();

        printBlock(function.startBlock(), processedBlocks, builder);

        String result = builder.toString();
        System.out.println(result);
    }

    private void printBlock(IrBlock block, HashSet<IrBlock> processedBlocks, StringBuilder builder) {
        if (processedBlocks.contains(block)) {
            return;
        }

        builder.append(block.name())
                .append(": ")
                .append("\n");
        for (IrInstruction inst : block.getInstructions()) {
            appendInstruction(inst, builder);
        }

        processedBlocks.add(block);

        for (IrBlock successor : block.getSuccessorBlocks()) {
            printBlock(successor, processedBlocks, builder);
        }
    }

    private final static String INDENT = "  ";
    private final static String SPACE = " ";
    private final static String ASSIGN = " = ";

    private void appendInstruction(IrInstruction instruction, StringBuilder builder) {
        switch (instruction) {
            case IrJumpInstruction jump:
                builder.append(INDENT).append("jump ").append(jump.jumpTarget().name());
                break;
            case IrReturnInstruction returnInstruction:
                builder.append(INDENT).append("return").append(SPACE).append(returnInstruction.src().name());
                break;
            case IrBranchInstruction branchInstruction:
                builder.append(INDENT).append("branch ")
                        .append(branchInstruction.conditionValue().name()).append(SPACE)
                        .append(branchInstruction.trueTarget().name()).append(SPACE)
                        .append(branchInstruction.falseTarget().name());
                break;
            case IrAddInstruction addInstruction:
                builder.append(INDENT).append(addInstruction.target().name()).append(ASSIGN)
                        .append("add ").append(addInstruction.leftSrc().name())
                        .append(SPACE).append(addInstruction.rightSrc().name());
                break;
            case IrBitwiseAndInstruction irBitwiseAndInstruction:
                builder.append(INDENT).append(irBitwiseAndInstruction.target().name()).append(ASSIGN)
                        .append("bit_and ").append(irBitwiseAndInstruction.leftSrc().name())
                        .append(SPACE).append(irBitwiseAndInstruction.rightSrc().name());
                break;
            case IrBitwiseNotInstruction irBitwiseNotInstruction:
                builder.append(INDENT).append(irBitwiseNotInstruction.target().name()).append(ASSIGN)
                        .append("bit_not ").append(irBitwiseNotInstruction.src().name());
                break;
            case IrBitwiseOrInstruction irBitwiseOrInstruction:
                builder.append(INDENT).append(irBitwiseOrInstruction.target().name()).append(ASSIGN)
                        .append("bit_or ").append(irBitwiseOrInstruction.leftSrc().name())
                        .append(SPACE).append(irBitwiseOrInstruction.rightSrc().name());
                break;
            case IrBitwiseXorInstruction irBitwiseXorInstruction:
                builder.append(INDENT).append(irBitwiseXorInstruction.target().name()).append(ASSIGN)
                        .append("bit_xor ").append(irBitwiseXorInstruction.leftSrc().name())
                        .append(SPACE).append(irBitwiseXorInstruction.rightSrc().name());
                break;
            case IrBoolConstantInstruction irBoolConstantInstruction:
                builder.append(INDENT).append(irBoolConstantInstruction.target().name()).append(ASSIGN)
                        .append(irBoolConstantInstruction.constValue());
                break;
            case IrDivInstruction irDivInstruction:
                builder.append(INDENT).append(irDivInstruction.target().name()).append(ASSIGN)
                        .append("bit_xor ").append(irDivInstruction.leftSrc().name())
                        .append(SPACE).append(irDivInstruction.rightSrc().name());
                break;
            case IrEqualsInstruction irEqualsInstruction:
                builder.append(INDENT).append(irEqualsInstruction.target().name()).append(ASSIGN)
                        .append("equal ").append(irEqualsInstruction.leftSrc().name())
                        .append(SPACE).append(irEqualsInstruction.rightSrc().name());
                break;
            case IrGreaterThanInstruction irGreaterThanInstruction:
                builder.append(INDENT).append(irGreaterThanInstruction.target().name()).append(ASSIGN)
                        .append("greater ").append(irGreaterThanInstruction.leftSrc().name())
                        .append(SPACE).append(irGreaterThanInstruction.rightSrc().name());
                break;
            case IrGreaterThanOrEqualInstruction irGreaterThanOrEqualInstruction:
                builder.append(INDENT).append(irGreaterThanOrEqualInstruction.target().name()).append(ASSIGN)
                        .append("greater_equal ").append(irGreaterThanOrEqualInstruction.leftSrc().name())
                        .append(SPACE).append(irGreaterThanOrEqualInstruction.rightSrc().name());
                break;
            case IrIntConstantInstruction irIntConstantInstruction:
                builder.append(INDENT).append(irIntConstantInstruction.target().name()).append(ASSIGN)
                        .append(irIntConstantInstruction.constValue());
                break;
            case IrLeftShiftInstruction irLeftShiftInstruction:
                builder.append(INDENT).append(irLeftShiftInstruction.target().name()).append(ASSIGN)
                        .append("left_shift ").append(irLeftShiftInstruction.leftSrc().name())
                        .append(SPACE).append(irLeftShiftInstruction.rightSrc().name());
                break;
            case IrLessThanInstruction irLessThanInstruction:
                builder.append(INDENT).append(irLessThanInstruction.target().name()).append(ASSIGN)
                        .append("less ").append(irLessThanInstruction.leftSrc().name())
                        .append(SPACE).append(irLessThanInstruction.rightSrc().name());
                break;
            case IrLessThanOrEqualInstruction irLessThanOrEqualInstruction:
                builder.append(INDENT).append(irLessThanOrEqualInstruction.target().name()).append(ASSIGN)
                        .append("less_equal ").append(irLessThanOrEqualInstruction.leftSrc().name())
                        .append(SPACE).append(irLessThanOrEqualInstruction.rightSrc().name());
                break;
            case IrLogicalNotInstruction irLogicalNotInstruction:
                builder.append(INDENT).append(irLogicalNotInstruction.target().name()).append(ASSIGN)
                        .append("not ").append(irLogicalNotInstruction.src().name());
                break;
            case IrModInstruction irModInstruction:
                builder.append(INDENT).append(irModInstruction.target().name()).append(ASSIGN)
                        .append("less_equal ").append(irModInstruction.leftSrc().name())
                        .append(SPACE).append(irModInstruction.rightSrc().name());
                break;
            case IrMoveInstruction irMoveInstruction:
                builder.append(INDENT).append(irMoveInstruction.target().name()).append(ASSIGN)
                        .append(irMoveInstruction.source().name());
                break;
            case IrMulInstruction irMulInstruction:
                builder.append(INDENT).append(irMulInstruction.target().name()).append(ASSIGN)
                        .append("mul ").append(irMulInstruction.leftSrc().name())
                        .append(SPACE).append(irMulInstruction.rightSrc().name());
                break;
            case IrNegateInstruction irNegateInstruction:
                builder.append(INDENT).append(irNegateInstruction.target().name()).append(ASSIGN)
                        .append("negate ").append(irNegateInstruction.src().name());
                break;
            case IrPhi irPhi:
                builder.append(INDENT).append(irPhi.target().name()).append(ASSIGN).append("phi").append(SPACE);
                for (IrPhi.IrPhiItem item : irPhi.sources()) {
                    builder.append("[").append(item.value().name()).append(", ")
                            .append(item.block().name()).append("]").append(", ");
                }
                if (!irPhi.sources().isEmpty()) {
                    builder.delete(builder.length() - 2, builder.length());
                }
                break;
            case IrRightShiftInstruction irRightShiftInstruction:
                builder.append(INDENT).append(irRightShiftInstruction.target().name()).append(ASSIGN)
                        .append("right_shift ").append(irRightShiftInstruction.leftSrc().name())
                        .append(SPACE).append(irRightShiftInstruction.rightSrc().name());
                break;
            case IrSubInstruction irSubInstruction:
                builder.append(INDENT).append(irSubInstruction.target().name()).append(ASSIGN)
                        .append("sub ").append(irSubInstruction.leftSrc().name())
                        .append(SPACE).append(irSubInstruction.rightSrc().name());
                break;
            case IrUnequalsInstruction irUnequalsInstruction:
                builder.append(INDENT).append(irUnequalsInstruction.target().name()).append(ASSIGN)
                        .append("not_equal ").append(irUnequalsInstruction.leftSrc().name())
                        .append(SPACE).append(irUnequalsInstruction.rightSrc().name());
                break;
        }
        builder.append("\n");
    }
}
