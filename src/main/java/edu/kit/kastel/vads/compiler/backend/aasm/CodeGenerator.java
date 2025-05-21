package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.*;

public interface CodeGenerator {

    void generateStackPointerPush();
    void generateStackPointerPop();
    void generateStackAllocation(int numberOfStackSlots);
    void generateStackDeallocation(int numberOfStackSlots);
    void generateConstantInstruction(RegisterAllocationResult allocationResult, ConstIntNode constIntNode);
    void generateAdd(RegisterAllocationResult allocationResult, AddNode addNode);
    void generateSub(RegisterAllocationResult allocationResult, SubNode subNode);
    void generateMult(RegisterAllocationResult allocationResult, MulNode mulNode);
    void generateDiv(RegisterAllocationResult allocationResult, DivNode divNode);
    void generateMod(RegisterAllocationResult allocationResult, ModNode modNode);
    void generateReturn(RegisterAllocationResult allocationResult, ReturnNode returnNode);

    X86InstructionGenerator getX86InstructionGenerator();
}
