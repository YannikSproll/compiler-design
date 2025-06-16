package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrBranchInstruction;
import edu.kit.kastel.vads.compiler.ir.IrJumpInstruction;
import edu.kit.kastel.vads.compiler.ir.IrReturnInstruction;
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
                BitSize.BIT_32);
    }

    @Override
    public void generateConstantInstruction(CodeGenerationContext generationContext, IrBoolConstantInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        instructionGenerator.generateIntConstInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                instruction.constValue() ? 1 : 0,
                BitSize.BIT_8);
    }

    @Override
    public void generateMove(CodeGenerationContext generationContext, IrMoveInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        generateMove(
                allocationResult,
                allocationResult.nodeToRegisterMapping().get(instruction.source()),
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                BitSize.BIT_32);
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

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateAdditionInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateAdditionInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateSub(CodeGenerationContext generationContext, IrSubInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateSubtractionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateSubtractionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateMult(CodeGenerationContext generationContext, IrMulInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMultiplicationInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateMultiplicationInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateDiv(CodeGenerationContext generationContext, IrDivInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(instruction.target()), BitSize.BIT_32);
    }

    @Override
    public void generateMod(CodeGenerationContext generationContext, IrModInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(instruction.target()), BitSize.BIT_32);
    }

    @Override
    public void generateLeftShift(CodeGenerationContext generationContext, IrLeftShiftInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register valueRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register shiftCountRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (shiftCountRegister != X86Register.REG_CX) {
            generateMove(allocationResult, shiftCountRegister, X86Register.REG_CX, BitSize.BIT_32);
        }

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(valueRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateLeftShiftInstruction(X86Register.REG_CX, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            generateMove(allocationResult, valueRegister, targetRegister, BitSize.BIT_32);
            instructionGenerator.generateLeftShiftInstruction(X86Register.REG_CX, targetRegister, BitSize.BIT_32);
        }
    }

    @Override
    public void generateRightShift(CodeGenerationContext generationContext, IrRightShiftInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register valueRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register shiftCountRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (shiftCountRegister != X86Register.REG_CX) {
            generateMove(allocationResult, shiftCountRegister, X86Register.REG_CX, BitSize.BIT_32);
        }

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(valueRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateRightShiftInstruction(X86Register.REG_CX, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            generateMove(allocationResult, valueRegister, targetRegister, BitSize.BIT_32);
            instructionGenerator.generateRightShiftInstruction(X86Register.REG_CX, targetRegister, BitSize.BIT_32);
        }
    }

    @Override
    public void generateBitwiseAnd(CodeGenerationContext generationContext, IrBitwiseAndInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateBitwiseAndInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateBitwiseAndInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseAndInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateBitwiseAndInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateBitwiseOr(CodeGenerationContext generationContext, IrBitwiseOrInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateBitwiseOrInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateBitwiseOrInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseOrInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateBitwiseOrInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateBitwiseNot(CodeGenerationContext generationContext, IrBitwiseNotInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (sourceValueRegister != targetValueRegister) {
            instructionGenerator.generateMoveInstruction(sourceValueRegister, targetValueRegister, BitSize.BIT_32);
        }

        instructionGenerator.generateBitwiseNotInstruction(targetValueRegister, BitSize.BIT_32);
    }

    @Override
    public void generateBitwiseXor(CodeGenerationContext generationContext, IrBitwiseXorInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateBitwiseXorInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateBitwiseXorInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                        .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateBitwiseXorInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateBitwiseXorInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    @Override
    public void generateEquals(CodeGenerationContext generationContext, IrEqualsInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
            && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(allocationResult.tempRegister(), rightOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(leftOperandRegister, rightOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.EQUAL);
        }

    }

    @Override
    public void generateUnequals(CodeGenerationContext generationContext, IrUnequalsInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(allocationResult.tempRegister(), rightOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.NOT_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(leftOperandRegister, rightOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.NOT_EQUAL);
        }
    }

    @Override
    public void generateGreaterThan(CodeGenerationContext generationContext, IrGreaterThanInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN);
        }

    }

    @Override
    public void generateLessThan(CodeGenerationContext generationContext, IrLessThanInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN);
        }
    }

    @Override
    public void generateGreaterThanOrEqual(CodeGenerationContext generationContext, IrGreaterThanOrEqualInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN_OR_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.GREATER_THAN_OR_EQUAL);
        }

    }

    @Override
    public void generateLessThanOrEqual(CodeGenerationContext generationContext, IrLessThanOrEqualInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.leftSrc());
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(instruction.rightSrc());
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (leftOperandRegister instanceof StackSlot
                && rightOperandRegister instanceof StackSlot) {
            instructionGenerator
                    .generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateComparisonInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN_OR_EQUAL);
        } else {
            instructionGenerator
                    .generateComparisonInstruction(rightOperandRegister, leftOperandRegister, BitSize.BIT_32)
                    .generateSetConditionCodeInstruction(targetRegister, X86ConditionCode.LESS_THAN_OR_EQUAL);
        }

    }

    @Override
    public void generateNegation(CodeGenerationContext generationContext, IrNegateInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        if (sourceValueRegister != targetValueRegister) {
            instructionGenerator.generateMoveInstruction(sourceValueRegister, targetValueRegister, BitSize.BIT_32);
        }

        instructionGenerator.generateNegationInstruction(targetValueRegister, BitSize.BIT_32);
    }

    @Override
    public void generateLogicalNot(CodeGenerationContext generationContext, IrLogicalNotInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register sourceValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        Register targetValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.target());

        generateMove(allocationResult, sourceValueRegister, targetValueRegister, BitSize.BIT_8);
        instructionGenerator.generateComparisonInstruction(new IntegerConstantParameter(0), targetValueRegister, BitSize.BIT_8)
                .generateSetConditionCodeInstruction(targetValueRegister, X86ConditionCode.EQUAL);
    }

    @Override
    public void generateReturn(CodeGenerationContext generationContext, IrReturnInstruction instruction) {
        RegisterAllocationResult allocationResult = generationContext.registerAllocationResult();

        Register returnValueRegister = allocationResult.nodeToRegisterMapping().get(instruction.src());
        if (returnValueRegister != X86Register.REG_AX) {
            instructionGenerator.generateMoveInstruction(returnValueRegister, X86Register.REG_AX, BitSize.BIT_32);
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
                    .generateComparisonInstruction(new IntegerConstantParameter(1), conditionValueRegister, BitSize.BIT_8)
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

    @Override
    public void generateJump(CodeGenerationContext generationContext, IrJumpInstruction instruction) {
        instructionGenerator.generateUnconditionalJumpInstruction(instruction.jumpTarget().name(), BitSize.BIT_64);
    }

    @Override
    public X86InstructionGenerator getX86InstructionGenerator() {
        return instructionGenerator;
    }
}
