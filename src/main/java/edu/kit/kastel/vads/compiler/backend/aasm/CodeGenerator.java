package edu.kit.kastel.vads.compiler.backend.aasm;

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

    void generateReturn(RegisterAllocationResult allocationResult, IrReturnInstruction instruction);

    X86InstructionGenerator getX86InstructionGenerator();
}
