package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.data.IrReturnInstruction;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;
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
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, IrIntConstantInstruction instruction) {
        instructionGenerator.generateIntConstInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                instruction.constValue(),
                BitSize.BIT_32);
    }

    @Override
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, IrBoolConstantInstruction instruction) {
        instructionGenerator.generateIntConstInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                instruction.constValue() ? 1 : 0,
                BitSize.BIT_8);
    }

    @Override
    public void generateMove(RegisterAllocationResult allocationResult, IrMoveInstruction instruction) {
        instructionGenerator.generateMoveInstruction(
                allocationResult.nodeToRegisterMapping().get(instruction.source()),
                allocationResult.nodeToRegisterMapping().get(instruction.target()),
                BitSize.BIT_32);
    }

    @Override
    public void generateAdd(RegisterAllocationResult allocationResult, IrAddInstruction instruction) {
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
    public void generateSub(RegisterAllocationResult allocationResult, IrSubInstruction instruction) {
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
    public void generateMult(RegisterAllocationResult allocationResult, IrMulInstruction instruction) {
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
    public void generateDiv(RegisterAllocationResult allocationResult, IrDivInstruction instruction) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(instruction.target()), BitSize.BIT_32);
    }

    @Override
    public void generateMod(RegisterAllocationResult allocationResult, IrModInstruction instruction) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(instruction.leftSrc()), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(instruction.rightSrc()), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(instruction.target()), BitSize.BIT_32);
    }

    @Override
    public void generateReturn(RegisterAllocationResult allocationResult, IrReturnInstruction instruction) {
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
    public X86InstructionGenerator getX86InstructionGenerator() {
        return instructionGenerator;
    }
}
