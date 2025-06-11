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
    public void generateConstantInstruction(CodeGenerationContext generationContext, IrIntConstantInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateConstantInstruction(generationContext, instruction);
    }

    @Override
    public void generateConstantInstruction(CodeGenerationContext generationContext, IrBoolConstantInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateConstantInstruction(generationContext, instruction);
    }

    @Override
    public void generateMove(CodeGenerationContext generationContext, IrMoveInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMove(generationContext, instruction);
    }

    @Override
    public void generateAdd(CodeGenerationContext generationContext, IrAddInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateAdd(generationContext, instruction);
    }

    @Override
    public void generateSub(CodeGenerationContext generationContext, IrSubInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateSub(generationContext, instruction);
    }

    @Override
    public void generateMult(CodeGenerationContext generationContext, IrMulInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMult(generationContext, instruction);
    }

    @Override
    public void generateDiv(CodeGenerationContext generationContext, IrDivInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateDiv(generationContext, instruction);
    }

    @Override
    public void generateMod(CodeGenerationContext generationContext, IrModInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateMod(generationContext, instruction);
    }

    @Override
    public void generateLeftShift(CodeGenerationContext generationContext, IrLeftShiftInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLeftShift(generationContext, instruction);
    }

    @Override
    public void generateRightShift(CodeGenerationContext generationContext, IrRightShiftInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateRightShift(generationContext, instruction);
    }

    @Override
    public void generateBitwiseAnd(CodeGenerationContext generationContext, IrBitwiseAndInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseAnd(generationContext, instruction);
    }

    @Override
    public void generateBitwiseOr(CodeGenerationContext generationContext, IrBitwiseOrInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseOr(generationContext, instruction);
    }

    @Override
    public void generateBitwiseNot(CodeGenerationContext generationContext, IrBitwiseNotInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseNot(generationContext, instruction);
    }

    @Override
    public void generateBitwiseXor(CodeGenerationContext generationContext, IrBitwiseXorInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBitwiseXor(generationContext, instruction);
    }

    @Override
    public void generateEquals(CodeGenerationContext generationContext, IrEqualsInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateEquals(generationContext, instruction);
    }

    @Override
    public void generateUnequals(CodeGenerationContext generationContext, IrUnequalsInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateUnequals(generationContext, instruction);
    }

    @Override
    public void generateGreaterThan(CodeGenerationContext generationContext, IrGreaterThanInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateGreaterThan(generationContext, instruction);
    }

    @Override
    public void generateLessThan(CodeGenerationContext generationContext, IrLessThanInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLessThan(generationContext, instruction);
    }

    @Override
    public void generateGreaterThanOrEqual(CodeGenerationContext generationContext, IrGreaterThanOrEqualInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateGreaterThanOrEqual(generationContext, instruction);
    }

    @Override
    public void generateLessThanOrEqual(CodeGenerationContext generationContext, IrLessThanOrEqualInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLessThanOrEqual(generationContext, instruction);
    }

    @Override
    public void generateNegation(CodeGenerationContext generationContext, IrNegateInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateNegation(generationContext, instruction);
    }

    @Override
    public void generateLogicalNot(CodeGenerationContext generationContext, IrLogicalNotInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateLogicalNot(generationContext, instruction);
    }

    @Override
    public void generateReturn(CodeGenerationContext generationContext, IrReturnInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateReturn(generationContext, instruction);
    }

    @Override
    public void generateBranch(CodeGenerationContext generationContext, IrBranchInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateBranch(generationContext, instruction);
    }

    @Override
    public void generateJump(CodeGenerationContext generationContext, IrJumpInstruction instruction) {
        generateLineDebugging(instruction);
        codeGenerator.generateJump(generationContext, instruction);
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
