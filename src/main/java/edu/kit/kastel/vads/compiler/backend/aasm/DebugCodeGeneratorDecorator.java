package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.node.*;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;

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
    public void generateConstantInstruction(RegisterAllocationResult allocationResult, ConstIntNode constIntNode) {
        generateLineDebugging(constIntNode);
        codeGenerator.generateConstantInstruction(allocationResult, constIntNode);
    }

    @Override
    public void generateAdd(RegisterAllocationResult allocationResult, AddNode addNode) {
        generateLineDebugging(addNode);
        codeGenerator.generateAdd(allocationResult, addNode);
    }

    @Override
    public void generateSub(RegisterAllocationResult allocationResult, SubNode subNode) {
        generateLineDebugging(subNode);
        codeGenerator.generateSub(allocationResult, subNode);
    }

    @Override
    public void generateMult(RegisterAllocationResult allocationResult, MulNode mulNode) {
        generateLineDebugging(mulNode);
        codeGenerator.generateMult(allocationResult, mulNode);
    }

    @Override
    public void generateDiv(RegisterAllocationResult allocationResult, DivNode divNode) {
        generateLineDebugging(divNode);
        codeGenerator.generateDiv(allocationResult, divNode);
    }

    @Override
    public void generateMod(RegisterAllocationResult allocationResult, ModNode modNode) {
        generateLineDebugging(modNode);
        codeGenerator.generateMod(allocationResult, modNode);
    }

    @Override
    public void generateReturn(RegisterAllocationResult allocationResult, ReturnNode returnNode) {
        generateLineDebugging(returnNode);
        codeGenerator.generateReturn(allocationResult, returnNode);
    }

    @Override
    public X86InstructionGenerator getX86InstructionGenerator() {
        return codeGenerator.getX86InstructionGenerator();
    }

    private void generateComment(Node node) {

    }

    private void generateLineDebugging(Node node) {
        if (node.debugInfo() instanceof DebugInfo.SourceInfo(edu.kit.kastel.vads.compiler.Span span)) {
            getX86InstructionGenerator()
                .generateLOCAnnotation(1, span.start().line(), span.start().column());
        }
    }
}
