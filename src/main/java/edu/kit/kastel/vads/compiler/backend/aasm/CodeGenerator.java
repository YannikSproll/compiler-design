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

    void generateConstantInstruction(CodeGenerationContext generationContext, IrIntConstantInstruction instruction);
    void generateConstantInstruction(CodeGenerationContext generationContext, IrBoolConstantInstruction instruction);

    void generateMove(CodeGenerationContext generationContext, IrMoveInstruction instruction);

    void generateAdd(CodeGenerationContext generationContext, IrAddInstruction instruction);
    void generateSub(CodeGenerationContext generationContext, IrSubInstruction instruction);
    void generateMult(CodeGenerationContext generationContext, IrMulInstruction instruction);
    void generateDiv(CodeGenerationContext generationContext, IrDivInstruction instruction);
    void generateMod(CodeGenerationContext generationContext, IrModInstruction instruction);
    void generateLeftShift(CodeGenerationContext generationContext, IrLeftShiftInstruction instruction);
    void generateRightShift(CodeGenerationContext generationContext, IrRightShiftInstruction instruction);

    void generateBitwiseAnd(CodeGenerationContext generationContext, IrBitwiseAndInstruction instruction);
    void generateBitwiseOr(CodeGenerationContext generationContext, IrBitwiseOrInstruction instruction);
    void generateBitwiseNot(CodeGenerationContext generationContext, IrBitwiseNotInstruction instruction);
    void generateBitwiseXor(CodeGenerationContext generationContext, IrBitwiseXorInstruction instruction);

    void generateEquals(CodeGenerationContext generationContext, IrEqualsInstruction instruction);
    void generateUnequals(CodeGenerationContext generationContext, IrUnequalsInstruction instruction);
    void generateGreaterThan(CodeGenerationContext generationContext, IrGreaterThanInstruction instruction);
    void generateLessThan(CodeGenerationContext generationContext, IrLessThanInstruction instruction);
    void generateGreaterThanOrEqual(CodeGenerationContext generationContext, IrGreaterThanOrEqualInstruction instruction);
    void generateLessThanOrEqual(CodeGenerationContext generationContext, IrLessThanOrEqualInstruction instruction);

    void generateNegation(CodeGenerationContext generationContext, IrNegateInstruction instruction);
    void generateLogicalNot(CodeGenerationContext generationContext, IrLogicalNotInstruction instruction);

    void generateReturn(CodeGenerationContext generationContext, IrReturnInstruction instruction);

    void generateBranch(CodeGenerationContext generationContext, IrBranchInstruction instruction);
    void generateJump(CodeGenerationContext generationContext, IrJumpInstruction instruction);

    X86InstructionGenerator getX86InstructionGenerator();
}
