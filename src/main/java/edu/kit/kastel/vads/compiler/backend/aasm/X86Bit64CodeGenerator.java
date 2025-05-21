package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.node.*;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

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
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, ConstIntNode constIntNode) {
        instructionGenerator.generateIntConstInstruction(allocationResult.nodeToRegisterMapping().get(constIntNode), constIntNode.value(), BitSize.BIT_32);
    }

    @Override
    public void generateAdd(RegisterAllocationResult allocationResult, AddNode addNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(addNode);

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
    public void generateSub(RegisterAllocationResult allocationResult, SubNode subNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(subNode);

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
    public void generateMult(RegisterAllocationResult allocationResult, MulNode mulNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(mulNode);

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
    public void generateDiv(RegisterAllocationResult allocationResult, DivNode divNode) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.LEFT)), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.RIGHT)), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(divNode), BitSize.BIT_32);
    }

    @Override
    public void generateMod(RegisterAllocationResult allocationResult, ModNode modNode) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.LEFT)), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.RIGHT)), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(modNode), BitSize.BIT_32);
    }

    @Override
    public void generateReturn(RegisterAllocationResult allocationResult, ReturnNode returnNode) {
        Register returnValueRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(returnNode, ReturnNode.RESULT));
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
    public X86InstructionGenerator getX86InstructionGenerator() {
        return instructionGenerator;
    }
}
