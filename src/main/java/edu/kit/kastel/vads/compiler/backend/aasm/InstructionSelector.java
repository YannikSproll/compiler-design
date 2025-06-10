package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.*;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.HashSet;
import java.util.Set;

public class InstructionSelector {

    private static String NON_EXECUTABLE_STACK =
            ".section .note.GNU-stack,\"\",@progbits\n";

    public void generateCode(IrFile file, CodeGenerator codeGenerator, String sourceFileName) {
        X86InstructionGenerator instructionGenerator = codeGenerator.getX86InstructionGenerator();

        instructionGenerator.generateFile(1, sourceFileName)
                .generateGlobal("main");

        // Export function labels
        for (IrFunction function : file.functions()) {
            instructionGenerator.generateGlobal(function.startBlock().name());
        }

        // Existence of entry point is ensured earlier
        IrFunction entryPointFunction = file.functions().stream().filter(IrFunction::isEntryPoint).findFirst().get();

        instructionGenerator.generateText()
                .generateLabel("main")
                .generateCall(entryPointFunction.startBlock().name())
                .generateMoveInstruction(X86Register.REG_AX, X86Register.REG_DI, BitSize.BIT_64)
                .generateMoveInstruction(new IntegerConstantParameter(0x3C), X86Register.REG_AX, BitSize.BIT_64)
                .generateSyscall();

        for (IrFunction function : file.functions()) {

            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            AasmRegisterAllocator allocator = new AasmRegisterAllocator(livenessAnalysis);

            RegisterAllocationResult allocationResult = allocator.allocateRegisters(function);

            // TODO: De-SSA
            generateFunction(function, codeGenerator, allocationResult);
        }

        instructionGenerator.generateFromString(NON_EXECUTABLE_STACK);
    }

    private void generateFunction(IrFunction function, CodeGenerator codeGenerator, RegisterAllocationResult allocationResult) {
        codeGenerator
                .getX86InstructionGenerator()
                .generateLabel(function.startBlock().name());

        codeGenerator.generateStackPointerPush();

        int numberOfStackSlots = (int) allocationResult.registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            codeGenerator.generateStackAllocation(numberOfStackSlots);
        }

        Set<IrBlock> visitedBlocks = new HashSet<>();
        for (IrBlock block : function.blocks()) {
            generateInstructionsForBlock(block, visitedBlocks, codeGenerator, allocationResult);
        }
    }

    private void generateInstructionsForBlock(IrBlock block, Set<IrBlock> processedBlocks, CodeGenerator codeGenerator, RegisterAllocationResult allocationResult) {
        if (processedBlocks.contains(block)) {
            return;
        }

        for (IrInstruction inst : block.getInstructions()) {
            generateInstruction(inst, codeGenerator, allocationResult);
        }

        processedBlocks.add(block);

        for (IrBlock successor : block.getSuccessorBlocks()) {
            codeGenerator
                    .getX86InstructionGenerator()
                    .generateLabel(block.name());

            generateInstructionsForBlock(successor, processedBlocks, codeGenerator, allocationResult);
        }
    }

    private void generateInstruction(IrInstruction instruction, CodeGenerator codeGenerator, RegisterAllocationResult allocationResult) {
        switch (instruction) {
            case IrJumpInstruction jump:
                codeGenerator.generateJump(allocationResult, jump);
                break;
            case IrReturnInstruction returnInstruction:
                codeGenerator.generateReturn(allocationResult, returnInstruction);
                break;
            case IrBranchInstruction branchInstruction:
                break;
            case IrAddInstruction addInstruction:
                codeGenerator.generateAdd(allocationResult, addInstruction);
                break;
            case IrBitwiseAndInstruction irBitwiseAndInstruction:
                codeGenerator.generateBitwiseAnd(allocationResult, irBitwiseAndInstruction);
                break;
            case IrBitwiseNotInstruction irBitwiseNotInstruction:
                codeGenerator.generateBitwiseNot(allocationResult, irBitwiseNotInstruction);
                break;
            case IrBitwiseOrInstruction irBitwiseOrInstruction:
                codeGenerator.generateBitwiseOr(allocationResult, irBitwiseOrInstruction);
                break;
            case IrBitwiseXorInstruction irBitwiseXorInstruction:
                codeGenerator.generateBitwiseXor(allocationResult, irBitwiseXorInstruction);
                break;
            case IrBoolConstantInstruction irBoolConstantInstruction:
                codeGenerator.generateConstantInstruction(allocationResult, irBoolConstantInstruction);
                break;
            case IrDivInstruction irDivInstruction:
                codeGenerator.generateDiv(allocationResult, irDivInstruction);
                break;
            case IrEqualsInstruction irEqualsInstruction:
                codeGenerator.generateEquals(allocationResult, irEqualsInstruction);
                break;
            case IrGreaterThanInstruction irGreaterThanInstruction:
                codeGenerator.generateGreaterThan(allocationResult, irGreaterThanInstruction);
                break;
            case IrGreaterThanOrEqualInstruction irGreaterThanOrEqualInstruction:
                codeGenerator.generateGreaterThanOrEqual(allocationResult, irGreaterThanOrEqualInstruction);
                break;
            case IrIntConstantInstruction irIntConstantInstruction:
                codeGenerator.generateConstantInstruction(allocationResult, irIntConstantInstruction);
                break;
            case IrLeftShiftInstruction irLeftShiftInstruction:
                codeGenerator.generateLeftShift(allocationResult, irLeftShiftInstruction);
                break;
            case IrLessThanInstruction irLessThanInstruction:
                codeGenerator.generateLessThan(allocationResult, irLessThanInstruction);
                break;
            case IrLessThanOrEqualInstruction irLessThanOrEqualInstruction:
                codeGenerator.generateLessThanOrEqual(allocationResult, irLessThanOrEqualInstruction);
                break;
            case IrLogicalNotInstruction irLogicalNotInstruction:
                break;
            case IrModInstruction irModInstruction:
                codeGenerator.generateMod(allocationResult, irModInstruction);
                break;
            case IrMoveInstruction irMoveInstruction:
                codeGenerator.generateMove(allocationResult, irMoveInstruction);
                break;
            case IrMulInstruction irMulInstruction:
                codeGenerator.generateMult(allocationResult, irMulInstruction);
                break;
            case IrNegateInstruction irNegateInstruction:
                codeGenerator.generateNegation(allocationResult, irNegateInstruction);
                break;
            case IrRightShiftInstruction irRightShiftInstruction:
                codeGenerator.generateRightShift(allocationResult, irRightShiftInstruction);
                break;
            case IrSubInstruction irSubInstruction:
                codeGenerator.generateSub(allocationResult, irSubInstruction);
                break;
            case IrUnequalsInstruction irUnequalsInstruction:
                codeGenerator.generateUnequals(allocationResult, irUnequalsInstruction);
                break;
            case IrPhi irPhi:
                throw new IllegalArgumentException("Phi instruction is not supported");
        }
    }
}
