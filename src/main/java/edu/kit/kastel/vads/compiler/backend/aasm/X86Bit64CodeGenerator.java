package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrBranchInstruction;
import edu.kit.kastel.vads.compiler.ir.IrJumpInstruction;
import edu.kit.kastel.vads.compiler.ir.IrReturnInstruction;
import edu.kit.kastel.vads.compiler.ir.IrType;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.*;

public class X86Bit64CodeGenerator implements CodeGenerator {
    private final X86InstructionGenerator instructionGenerator;

    public X86Bit64CodeGenerator(X86InstructionGenerator instructionGenerator) {
        this.instructionGenerator = instructionGenerator;
    }

    @Override
    public void generateStackPointerPush() {
        instructionGenerator.generatePushInstruction(X86Register.REG_BP, BitSize.BIT_64)
                .generateMoveInstruction(X86Register.REG_SP, X86Register.REG_BP, BitSize.BIT_64);
    }

    @Override
    public void generateStackPointerPop() {
        instructionGenerator.generatePopInstruction(X86Register.REG_BP, BitSize.BIT_64);
    }

    @Override
    public void generateStackAllocation(int numberOfStackSlots) {
        instructionGenerator.generateSubtractionInstruction(new IntegerConstantParameter((numberOfStackSlots + 1) * 8), X86Register.REG_SP, BitSize.BIT_64);
    }

    @Override
    public void generateStackDeallocation(int numberOfStackSlots) {
        instructionGenerator.generateAdditionInstruction(new IntegerConstantParameter((numberOfStackSlots + 1) * 8), X86Register.REG_SP, BitSize.BIT_64);
    }

    @Override
    public void generateConstantInstruction(CodeGenerationContext generationContext, IrIntConstantInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        instructionGenerator.generateIntConstInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                instruction.constValue(),
                getBitSize(instruction.target().type()));
    }

    @Override
    public void generateConstantInstruction(CodeGenerationContext generationContext, IrBoolConstantInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        instructionGenerator.generateIntConstInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                instruction.constValue() ? 1 : 0,
                getBitSize(instruction.target().type()));
    }

    @Override
    public void generateMove(CodeGenerationContext generationContext, IrMoveInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        generateMove(
                allocationResult,
                allocationResult.nodeToRegisterMapping().get(instruction.source()),
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                getBitSize(instruction.target().type()));
    }

    private void generateMove(RegisterAllocationResult allocationResult, Register sourceRegister, Register targetRegister, BitSize bitSize) {
        if (sourceRegister == targetRegister) {
            return;
        }

        if (sourceRegister instanceof StackSlot && targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(sourceRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            instructionGenerator.generateMoveInstruction(sourceRegister, targetRegister, bitSize);
        }
    }

    @Override
    public void generateAdd(CodeGenerationContext generationContext, IrAddInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateAdditionInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(leftOperandRegister, targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, bitSize)
                        .generateAdditionInstruction(leftOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateSub(CodeGenerationContext generationContext, IrSubInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateSubtractionInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, bitSize)
                        .generateSubtractionInstruction(rightOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateMult(CodeGenerationContext generationContext, IrMulInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMultiplicationInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(leftOperandRegister, targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, bitSize)
                        .generateMultiplicationInstruction(leftOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateDiv(CodeGenerationContext generationContext, IrDivInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        BitSize bitSize = getBitSize(instruction.target().type());
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, bitSize)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), bitSize)
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(instruction.target()), bitSize);
    }

    @Override
    public void generateMod(CodeGenerationContext generationContext, IrModInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        BitSize bitSize = getBitSize(instruction.target().type());
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, bitSize)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), bitSize)
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(instruction.target()), bitSize);
    }

    @Override
    public void generateLeftShift(CodeGenerationContext generationContext, IrLeftShiftInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register valueRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register shiftCountRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (shiftCountRegister != X86Register.REG_CX) {
            generateMove(allocationResult, shiftCountRegister, X86Register.REG_CX, bitSize);
        }

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(valueRegister, allocationResult.tempRegister(), bitSize)
                    .generateLeftShiftInstruction(X86Register.REG_CX, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            generateMove(allocationResult, valueRegister, targetRegister, bitSize);
            instructionGenerator.generateLeftShiftInstruction(X86Register.REG_CX, targetRegister, bitSize);
        }
    }

    @Override
    public void generateRightShift(CodeGenerationContext generationContext, IrRightShiftInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register valueRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register shiftCountRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (shiftCountRegister != X86Register.REG_CX) {
            generateMove(allocationResult, shiftCountRegister, X86Register.REG_CX, bitSize);
        }

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(valueRegister, allocationResult.tempRegister(), bitSize)
                    .generateRightShiftInstruction(X86Register.REG_CX, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            generateMove(allocationResult, valueRegister, targetRegister, bitSize);
            instructionGenerator.generateRightShiftInstruction(X86Register.REG_CX, targetRegister, bitSize);
        }
    }

    @Override
    public void generateBitwiseAnd(CodeGenerationContext generationContext, IrBitwiseAndInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateBitwiseAndInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateBitwiseAndInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseAndInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, bitSize)
                        .generateBitwiseAndInstruction(rightOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateBitwiseOr(CodeGenerationContext generationContext, IrBitwiseOrInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateBitwiseOrInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateBitwiseOrInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseOrInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, bitSize)
                        .generateBitwiseOrInstruction(rightOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateBitwiseNot(CodeGenerationContext generationContext, IrBitwiseNotInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (sourceValueRegister != targetValueRegister) {
            instructionGenerator.generateMoveInstruction(sourceValueRegister, targetValueRegister, bitSize);
        }

        instructionGenerator.generateBitwiseNotInstruction(targetValueRegister, bitSize);
    }

    @Override
    public void generateBitwiseXor(CodeGenerationContext generationContext, IrBitwiseXorInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateBitwiseXorInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateBitwiseXorInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, bitSize);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseXorInstruction(rightOperandRegister, targetRegister, bitSize);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, bitSize)
                        .generateBitwiseXorInstruction(rightOperandRegister, targetRegister, bitSize);
            }
        }
    }

    @Override
    public void generateEquals(CodeGenerationContext generationContext, IrEqualsInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
            && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(allocationResult.tempRegister(), rightOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(leftOperandRegister, rightOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.EQUAL);
        }

    }

    @Override
    public void generateUnequals(CodeGenerationContext generationContext, IrUnequalsInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(allocationResult.tempRegister(), rightOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.NOT_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(leftOperandRegister, rightOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.NOT_EQUAL);
        }
    }

    @Override
    public void generateGreaterThan(CodeGenerationContext generationContext, IrGreaterThanInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN);
        }

    }

    @Override
    public void generateLessThan(CodeGenerationContext generationContext, IrLessThanInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN);
        }
    }

    @Override
    public void generateGreaterThanOrEqual(CodeGenerationContext generationContext, IrGreaterThanOrEqualInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN_OR_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN_OR_EQUAL);
        }

    }

    @Override
    public void generateLessThanOrEqual(CodeGenerationContext generationContext, IrLessThanOrEqualInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN_OR_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, bitSize)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN_OR_EQUAL);
        }

    }

    @Override
    public void generateNegation(CodeGenerationContext generationContext, IrNegateInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        if (sourceValueRegister != targetValueRegister) {
            instructionGenerator.generateMoveInstruction(sourceValueRegister, targetValueRegister, bitSize);
        }

        instructionGenerator.generateNegationInstruction(targetValueRegister, bitSize);
    }

    @Override
    public void generateLogicalNot(CodeGenerationContext generationContext, IrLogicalNotInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        BitSize bitSize = getBitSize(instruction.target().type());
        generateMove(allocationResult, sourceValueRegister, targetValueRegister, bitSize);
        instructionGenerator.generateComparisonInstruction(new IntegerConstantParameter(0), targetValueRegister, bitSize)
                .generateSetConditionCodeInstruction(targetValueRegister, X86ConditionCode.EQUAL);
    }

    @Override
    public void generateReturn(CodeGenerationContext generationContext, IrReturnInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register returnValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        if (returnValueRegister != X86Register.REG_AX) {
            instructionGenerator.generateMoveInstruction(returnValueRegister, X86Register.REG_AX, getBitSize(instruction.src().type()));
        }

        int numberOfStackSlots = (int) allocationResult.registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            generateStackDeallocation(numberOfStackSlots);
        }

        generateStackPointerPop();

        instructionGenerator.generateReturnInstruction();
    }

    @Override
    public void generateBranch(CodeGenerationContext generationContext, IrBranchInstruction instruction) {
        IrValueProducingInstruction conditionProducingInstruction
                = generationContext.ssaValueByProducingInstructions().get(instruction.conditionValue());

        if (isComparisonInstruction(conditionProducingInstruction)) {
            // Instruction is a comparison -> use flags directly
            instructionGenerator
                    .generateConditionalJumpInstruction(
                            mapIrComparisonToX86ConditionCode(conditionProducingInstruction),
                            instruction.trueTarget().name(),
                            BitSize.BIT_64)
                    .generateUnconditionalJumpInstruction(
                            instruction.falseTarget().name(),
                            BitSize.BIT_64);
        } else {
            // Instruction is bool expression or successor of comparison
            Register conditionValueRegister = generationContext.registerAllocationResult()
                    .nodeToRegisterMapping().get(instruction.conditionValue());
            instructionGenerator
                    .generateComparisonInstruction(new IntegerConstantParameter(1), conditionValueRegister, getBitSize(instruction.conditionValue().type()))
                    .generateConditionalJumpInstruction(X86ConditionCode.EQUAL, instruction.trueTarget().name(), BitSize.BIT_64)
                    .generateUnconditionalJumpInstruction(instruction.falseTarget().name(), BitSize.BIT_64);
        }
    }

    private static boolean isComparisonInstruction(IrValueProducingInstruction instruction) {
        return switch (instruction) {
            case IrLessThanInstruction _, IrGreaterThanInstruction _, IrUnequalsInstruction _,
                 IrLessThanOrEqualInstruction _, IrGreaterThanOrEqualInstruction _, IrEqualsInstruction _ -> true;
            default -> false;
        };
    }

    private static X86ConditionCode mapIrComparisonToX86ConditionCode(IrValueProducingInstruction comparisonInstruction) {
        return switch (comparisonInstruction) {
            case IrEqualsInstruction _ -> X86ConditionCode.EQUAL;
            case IrUnequalsInstruction _ -> X86ConditionCode.NOT_EQUAL;
            case IrLessThanInstruction _ -> X86ConditionCode.LESS_THAN;
            case IrGreaterThanInstruction _ -> X86ConditionCode.GREATER_THAN;
            case IrLessThanOrEqualInstruction _ -> X86ConditionCode.LESS_THAN_OR_EQUAL;
            case IrGreaterThanOrEqualInstruction _ -> X86ConditionCode.GREATER_THAN_OR_EQUAL;
            default -> throw new IllegalStateException("Unexpected value: " + comparisonInstruction);
        };
    }

    private static BitSize getBitSize(IrType type) {
        return switch (type) {
            case BOOL -> BitSize.BIT_8;
            case I32 -> BitSize.BIT_32;
        };
    }

    @Override
    public void generateJump(CodeGenerationContext generationContext, IrJumpInstruction instruction) {
        instructionGenerator.generateUnconditionalJumpInstruction(instruction.jumpTarget().name(), BitSize.BIT_64);
    }

    @Override
    public X86InstructionGenerator getX86InstructionGenerator() {
        return instructionGenerator;
    }
}
