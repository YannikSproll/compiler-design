package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.*;

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
                builder.append(INDENT).append("return").append(SPACE).append(returnInstruction.src().formatName());
                break;
            case IrBranchInstruction branchInstruction:
                builder.append(INDENT).append("branch ")
                        .append(branchInstruction.conditionValue().formatName()).append(SPACE)
                        .append(branchInstruction.trueTarget().name()).append(SPACE)
                        .append(branchInstruction.falseTarget().name());
                break;
            case IrAddInstruction addInstruction:
                builder.append(INDENT).append(addInstruction.target().formatName()).append(ASSIGN)
                        .append("add ").append(addInstruction.leftSrc().formatName())
                        .append(SPACE).append(addInstruction.rightSrc().formatName());
                break;
            case IrBitwiseAndInstruction irBitwiseAndInstruction:
                builder.append(INDENT).append(irBitwiseAndInstruction.target().formatName()).append(ASSIGN)
                        .append("bit_and ").append(irBitwiseAndInstruction.leftSrc().formatName())
                        .append(SPACE).append(irBitwiseAndInstruction.rightSrc().formatName());
                break;
            case IrBitwiseNotInstruction irBitwiseNotInstruction:
                builder.append(INDENT).append(irBitwiseNotInstruction.target().formatName()).append(ASSIGN)
                        .append("bit_not ").append(irBitwiseNotInstruction.src().formatName());
                break;
            case IrBitwiseOrInstruction irBitwiseOrInstruction:
                builder.append(INDENT).append(irBitwiseOrInstruction.target().formatName()).append(ASSIGN)
                        .append("bit_or ").append(irBitwiseOrInstruction.leftSrc().formatName())
                        .append(SPACE).append(irBitwiseOrInstruction.rightSrc().formatName());
                break;
            case IrBitwiseXorInstruction irBitwiseXorInstruction:
                builder.append(INDENT).append(irBitwiseXorInstruction.target().formatName()).append(ASSIGN)
                        .append("bit_xor ").append(irBitwiseXorInstruction.leftSrc().formatName())
                        .append(SPACE).append(irBitwiseXorInstruction.rightSrc().formatName());
                break;
            case IrBoolConstantInstruction irBoolConstantInstruction:
                builder.append(INDENT).append(irBoolConstantInstruction.target().formatName()).append(ASSIGN)
                        .append(irBoolConstantInstruction.constValue());
                break;
            case IrDivInstruction irDivInstruction:
                builder.append(INDENT).append(irDivInstruction.target().formatName()).append(ASSIGN)
                        .append("bit_xor ").append(irDivInstruction.leftSrc().formatName())
                        .append(SPACE).append(irDivInstruction.rightSrc().formatName());
                break;
            case IrEqualsInstruction irEqualsInstruction:
                builder.append(INDENT).append(irEqualsInstruction.target().formatName()).append(ASSIGN)
                        .append("equal ").append(irEqualsInstruction.leftSrc().formatName())
                        .append(SPACE).append(irEqualsInstruction.rightSrc().formatName());
                break;
            case IrGreaterThanInstruction irGreaterThanInstruction:
                builder.append(INDENT).append(irGreaterThanInstruction.target().formatName()).append(ASSIGN)
                        .append("greater ").append(irGreaterThanInstruction.leftSrc().formatName())
                        .append(SPACE).append(irGreaterThanInstruction.rightSrc().formatName());
                break;
            case IrGreaterThanOrEqualInstruction irGreaterThanOrEqualInstruction:
                builder.append(INDENT).append(irGreaterThanOrEqualInstruction.target().formatName()).append(ASSIGN)
                        .append("greater_equal ").append(irGreaterThanOrEqualInstruction.leftSrc().formatName())
                        .append(SPACE).append(irGreaterThanOrEqualInstruction.rightSrc().formatName());
                break;
            case IrIntConstantInstruction irIntConstantInstruction:
                builder.append(INDENT).append(irIntConstantInstruction.target().formatName()).append(ASSIGN)
                        .append(irIntConstantInstruction.constValue());
                break;
            case IrLeftShiftInstruction irLeftShiftInstruction:
                builder.append(INDENT).append(irLeftShiftInstruction.target().formatName()).append(ASSIGN)
                        .append("left_shift ").append(irLeftShiftInstruction.leftSrc().formatName())
                        .append(SPACE).append(irLeftShiftInstruction.rightSrc().formatName());
                break;
            case IrLessThanInstruction irLessThanInstruction:
                builder.append(INDENT).append(irLessThanInstruction.target().formatName()).append(ASSIGN)
                        .append("less ").append(irLessThanInstruction.leftSrc().formatName())
                        .append(SPACE).append(irLessThanInstruction.rightSrc().formatName());
                break;
            case IrLessThanOrEqualInstruction irLessThanOrEqualInstruction:
                builder.append(INDENT).append(irLessThanOrEqualInstruction.target().formatName()).append(ASSIGN)
                        .append("less_equal ").append(irLessThanOrEqualInstruction.leftSrc().formatName())
                        .append(SPACE).append(irLessThanOrEqualInstruction.rightSrc().formatName());
                break;
            case IrLogicalNotInstruction irLogicalNotInstruction:
                builder.append(INDENT).append(irLogicalNotInstruction.target().formatName()).append(ASSIGN)
                        .append("not ").append(irLogicalNotInstruction.src().formatName());
                break;
            case IrModInstruction irModInstruction:
                builder.append(INDENT).append(irModInstruction.target().formatName()).append(ASSIGN)
                        .append("mod ").append(irModInstruction.leftSrc().formatName())
                        .append(SPACE).append(irModInstruction.rightSrc().formatName());
                break;
            case IrMoveInstruction irMoveInstruction:
                builder.append(INDENT).append(irMoveInstruction.target().formatName()).append(ASSIGN)
                        .append(irMoveInstruction.source().formatName());
                break;
            case IrMulInstruction irMulInstruction:
                builder.append(INDENT).append(irMulInstruction.target().formatName()).append(ASSIGN)
                        .append("mul ").append(irMulInstruction.leftSrc().formatName())
                        .append(SPACE).append(irMulInstruction.rightSrc().formatName());
                break;
            case IrNegateInstruction irNegateInstruction:
                builder.append(INDENT).append(irNegateInstruction.target().formatName()).append(ASSIGN)
                        .append("negate ").append(irNegateInstruction.src().formatName());
                break;
            case IrPhi irPhi:
                builder.append(INDENT).append(irPhi.target().formatName()).append(ASSIGN).append("phi").append(SPACE);
                for (IrPhi.IrPhiItem item : irPhi.sources()) {
                    builder.append("[").append(item.value().formatName()).append(", ")
                            .append(item.block().name()).append("]").append(", ");
                }
                if (!irPhi.sources().isEmpty()) {
                    builder.delete(builder.length() - 2, builder.length());
                }
                break;
            case IrRightShiftInstruction irRightShiftInstruction:
                builder.append(INDENT).append(irRightShiftInstruction.target().formatName()).append(ASSIGN)
                        .append("right_shift ").append(irRightShiftInstruction.leftSrc().formatName())
                        .append(SPACE).append(irRightShiftInstruction.rightSrc().formatName());
                break;
            case IrSubInstruction irSubInstruction:
                builder.append(INDENT).append(irSubInstruction.target().formatName()).append(ASSIGN)
                        .append("sub ").append(irSubInstruction.leftSrc().formatName())
                        .append(SPACE).append(irSubInstruction.rightSrc().formatName());
                break;
            case IrUnequalsInstruction irUnequalsInstruction:
                builder.append(INDENT).append(irUnequalsInstruction.target().formatName()).append(ASSIGN)
                        .append("not_equal ").append(irUnequalsInstruction.leftSrc().formatName())
                        .append(SPACE).append(irUnequalsInstruction.rightSrc().formatName());
                break;
        }
        builder.append("\n");
    }
}
