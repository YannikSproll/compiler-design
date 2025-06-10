package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.IrBranchInstruction;
import edu.kit.kastel.vads.compiler.ir.data.IrJumpInstruction;
import edu.kit.kastel.vads.compiler.ir.data.IrReturnInstruction;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;

public interface CodeGenerator {

    void generateStackPointerPush();
    void generateStackPointerPop();
    void generateStackAllocation(int numberOfStackSlots);
    void generateStackDeallocation(int numberOfStackSlots);

    void generateConstantInstruction(RegisterAllocationResult allocationResult, IrIntConstantInstruction instruction);
    void generateConstantInstruction(RegisterAllocationResult allocationResult, IrBoolConstantInstruction instruction);

    void generateMove(RegisterAllocationResult allocationResult, IrMoveInstruction instruction);

    void generateAdd(RegisterAllocationResult allocationResult, IrAddInstruction instruction);
    void generateSub(RegisterAllocationResult allocationResult, IrSubInstruction instruction);
    void generateMult(RegisterAllocationResult allocationResult, IrMulInstruction instruction);
    void generateDiv(RegisterAllocationResult allocationResult, IrDivInstruction instruction);
    void generateMod(RegisterAllocationResult allocationResult, IrModInstruction instruction);
    void generateLeftShift(RegisterAllocationResult allocationResult, IrLeftShiftInstruction instruction);
    void generateRightShift(RegisterAllocationResult allocationResult, IrRightShiftInstruction instruction);

    void generateBitwiseAnd(RegisterAllocationResult allocationResult, IrBitwiseAndInstruction instruction);
    void generateBitwiseOr(RegisterAllocationResult allocationResult, IrBitwiseOrInstruction instruction);
    void generateBitwiseNot(RegisterAllocationResult allocationResult, IrBitwiseNotInstruction instruction);
    void generateBitwiseXor(RegisterAllocationResult allocationResult, IrBitwiseXorInstruction instruction);

    void generateEquals(RegisterAllocationResult allocationResult, IrEqualsInstruction instruction);
    void generateUnequals(RegisterAllocationResult allocationResult, IrUnequalsInstruction instruction);
    void generateGreaterThan(RegisterAllocationResult allocationResult, IrGreaterThanInstruction instruction);
    void generateLessThan(RegisterAllocationResult allocationResult, IrLessThanInstruction instruction);
    void generateGreaterThanOrEqual(RegisterAllocationResult allocationResult, IrGreaterThanOrEqualInstruction instruction);
    void generateLessThanOrEqual(RegisterAllocationResult allocationResult, IrLessThanOrEqualInstruction instruction);

    void generateNegation(RegisterAllocationResult allocationResult, IrNegateInstruction instruction);

    void generateReturn(RegisterAllocationResult allocationResult, IrReturnInstruction instruction);

    void generateBranch(RegisterAllocationResult allocationResult, IrBranchInstruction instruction);
    void generateJump(RegisterAllocationResult allocationResult, IrJumpInstruction instruction);

    X86InstructionGenerator getX86InstructionGenerator();
}
