package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.IrBranchInstruction;
import edu.kit.kastel.vads.compiler.ir.data.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.data.IrJumpInstruction;
import edu.kit.kastel.vads.compiler.ir.data.IrReturnInstruction;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;
import edu.kit.kastel.vads.compiler.ir.node.*;

public final class DebugCodeGeneratorDecorator implements CodeGenerator {

    private final CodeGenerator codeGenerator;

    public DebugCodeGeneratorDecorator(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    @Override
    public void generateStackPointerPush() {
        codeGenerator.generateStackPointerPush();
    }

    @Override
    public void generateStackPointerPop() {
        codeGenerator.generateStackPointerPop();
    }

    @Override
    public void generateStackAllocation(int numberOfStackSlots) {
        codeGenerator.generateStackAllocation(numberOfStackSlots);
    }

    @Override
    public void generateStackDeallocation(int numberOfStackSlots) {
        codeGenerator.generateStackDeallocation(numberOfStackSlots);
    }

    @Override
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, IrIntConstantInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateConstantInstruction(allocationResult, instruction);
    }

    @Override
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, IrBoolConstantInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateConstantInstruction(allocationResult, instruction);
    }

    @Override
    public void generateMove(RegisterAllocationResult allocationResult, IrMoveInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMove(allocationResult, instruction);
    }

    @Override
    public void generateAdd(RegisterAllocationResult allocationResult, IrAddInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateAdd(allocationResult, instruction);
    }

    @Override
    public void generateSub(RegisterAllocationResult allocationResult, IrSubInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateSub(allocationResult, instruction);
    }

    @Override
    public void generateMult(RegisterAllocationResult allocationResult, IrMulInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMult(allocationResult, instruction);
    }

    @Override
    public void generateDiv(RegisterAllocationResult allocationResult, IrDivInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateDiv(allocationResult, instruction);
    }

    @Override
    public void generateMod(RegisterAllocationResult allocationResult, IrModInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMod(allocationResult, instruction);
    }

    @Override
    public void generateLeftShift(RegisterAllocationResult allocationResult, IrLeftShiftInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLeftShift(allocationResult, instruction);
    }

    @Override
    public void generateRightShift(RegisterAllocationResult allocationResult, IrRightShiftInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateRightShift(allocationResult, instruction);
    }

    @Override
    public void generateBitwiseAnd(RegisterAllocationResult allocationResult, IrBitwiseAndInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseAnd(allocationResult, instruction);
    }

    @Override
    public void generateBitwiseOr(RegisterAllocationResult allocationResult, IrBitwiseOrInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseOr(allocationResult, instruction);
    }

    @Override
    public void generateBitwiseNot(RegisterAllocationResult allocationResult, IrBitwiseNotInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseNot(allocationResult, instruction);
    }

    @Override
    public void generateBitwiseXor(RegisterAllocationResult allocationResult, IrBitwiseXorInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseXor(allocationResult, instruction);
    }

    @Override
    public void generateEquals(RegisterAllocationResult allocationResult, IrEqualsInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateEquals(allocationResult, instruction);
    }

    @Override
    public void generateUnequals(RegisterAllocationResult allocationResult, IrUnequalsInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateUnequals(allocationResult, instruction);
    }

    @Override
    public void generateGreaterThan(RegisterAllocationResult allocationResult, IrGreaterThanInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateGreaterThan(allocationResult, instruction);
    }

    @Override
    public void generateLessThan(RegisterAllocationResult allocationResult, IrLessThanInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLessThan(allocationResult, instruction);
    }

    @Override
    public void generateGreaterThanOrEqual(RegisterAllocationResult allocationResult, IrGreaterThanOrEqualInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateGreaterThanOrEqual(allocationResult, instruction);
    }

    @Override
    public void generateLessThanOrEqual(RegisterAllocationResult allocationResult, IrLessThanOrEqualInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLessThanOrEqual(allocationResult, instruction);
    }

    @Override
    public void generateNegation(RegisterAllocationResult allocationResult, IrNegateInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateNegation(allocationResult, instruction);
    }

    @Override
    public void generateReturn(RegisterAllocationResult allocationResult, IrReturnInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateReturn(allocationResult, instruction);
    }

    @Override
    public void generateBranch(RegisterAllocationResult allocationResult, IrBranchInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBranch(allocationResult, instruction);
    }

    @Override
    public void generateJump(RegisterAllocationResult allocationResult, IrJumpInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateJump(allocationResult, instruction);
    }

    @Override
    public X86InstructionGenerator getX86InstructionGenerator() {
        return codeGenerator.getX86InstructionGenerator();
    }

    private void generateComment(Node node) {

    }

    private void generateLineDebugging(IrInstruction instruction) {
        /*if (node.debugInfo() instanceof DebugInfo.SourceInfo(edu.kit.kastel.vads.compiler.Span span)) {
            getX86InstructionGenerator()
                .generateLOCAnnotation(1, span.start().line(), span.start().column());
        }*/
    }
}
