package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.IrInstruction;
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
