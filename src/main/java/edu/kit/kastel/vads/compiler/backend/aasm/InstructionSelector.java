package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.*;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
            CodeGenerationContext codeGenerationContext
                    = CodeGenerationContext.createForFunction(function, allocationResult);

            generateFunction(function, codeGenerator, codeGenerationContext);
        }

        instructionGenerator.generateFromString(NON_EXECUTABLE_STACK);
    }

    private void generateFunction(IrFunction function, CodeGenerator codeGenerator, CodeGenerationContext codeGenerationContext) {
        codeGenerator
                .getX86InstructionGenerator()
                .generateLabel(function.startBlock().name());

        codeGenerator.generateStackPointerPush();

        int numberOfStackSlots = (int) codeGenerationContext.registerAllocationResult()
                .registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            codeGenerator.generateStackAllocation(numberOfStackSlots);
        }

        Set<IrBlock> visitedBlocks = new HashSet<>();
        for (IrBlock block : function.blocks()) {
            generateInstructionsForBlock(block, visitedBlocks, codeGenerator, codeGenerationContext);
        }
    }

    private void generateInstructionsForBlock(IrBlock block, Set<IrBlock> processedBlocks, CodeGenerator codeGenerator, CodeGenerationContext codeGenerationContext) {
        for (IrInstruction inst : block.getInstructions()) {
            generateInstruction(inst, codeGenerator, codeGenerationContext);
        }

        processedBlocks.add(block);

        for (IrBlock successor : block.getSuccessorBlocks()) {
            if (processedBlocks.contains(successor)) {
                continue;
            }

            codeGenerator
                    .getX86InstructionGenerator()
                    .generateLabel(successor.name());

            generateInstructionsForBlock(successor, processedBlocks, codeGenerator, codeGenerationContext);
        }
    }

    private void generateInstruction(IrInstruction instruction, CodeGenerator codeGenerator, CodeGenerationContext codeGenerationContext) {
        switch (instruction) {
            case IrJumpInstruction jump:
                codeGenerator.generateJump(codeGenerationContext, jump);
                break;
            case IrReturnInstruction returnInstruction:
                codeGenerator.generateReturn(codeGenerationContext, returnInstruction);
                break;
            case IrBranchInstruction branchInstruction:
                codeGenerator.generateBranch(codeGenerationContext, branchInstruction);
                break;
            case IrAddInstruction addInstruction:
                codeGenerator.generateAdd(codeGenerationContext, addInstruction);
                break;
            case IrBitwiseAndInstruction irBitwiseAndInstruction:
                codeGenerator.generateBitwiseAnd(codeGenerationContext, irBitwiseAndInstruction);
                break;
            case IrBitwiseNotInstruction irBitwiseNotInstruction:
                codeGenerator.generateBitwiseNot(codeGenerationContext, irBitwiseNotInstruction);
                break;
            case IrBitwiseOrInstruction irBitwiseOrInstruction:
                codeGenerator.generateBitwiseOr(codeGenerationContext, irBitwiseOrInstruction);
                break;
            case IrBitwiseXorInstruction irBitwiseXorInstruction:
                codeGenerator.generateBitwiseXor(codeGenerationContext, irBitwiseXorInstruction);
                break;
            case IrBoolConstantInstruction irBoolConstantInstruction:
                codeGenerator.generateConstantInstruction(codeGenerationContext, irBoolConstantInstruction);
                break;
            case IrDivInstruction irDivInstruction:
                codeGenerator.generateDiv(codeGenerationContext, irDivInstruction);
                break;
            case IrEqualsInstruction irEqualsInstruction:
                codeGenerator.generateEquals(codeGenerationContext, irEqualsInstruction);
                break;
            case IrGreaterThanInstruction irGreaterThanInstruction:
                codeGenerator.generateGreaterThan(codeGenerationContext, irGreaterThanInstruction);
                break;
            case IrGreaterThanOrEqualInstruction irGreaterThanOrEqualInstruction:
                codeGenerator.generateGreaterThanOrEqual(codeGenerationContext, irGreaterThanOrEqualInstruction);
                break;
            case IrIntConstantInstruction irIntConstantInstruction:
                codeGenerator.generateConstantInstruction(codeGenerationContext, irIntConstantInstruction);
                break;
            case IrLeftShiftInstruction irLeftShiftInstruction:
                codeGenerator.generateLeftShift(codeGenerationContext, irLeftShiftInstruction);
                break;
            case IrLessThanInstruction irLessThanInstruction:
                codeGenerator.generateLessThan(codeGenerationContext, irLessThanInstruction);
                break;
            case IrLessThanOrEqualInstruction irLessThanOrEqualInstruction:
                codeGenerator.generateLessThanOrEqual(codeGenerationContext, irLessThanOrEqualInstruction);
                break;
            case IrLogicalNotInstruction irLogicalNotInstruction:
                codeGenerator.generateLogicalNot(codeGenerationContext, irLogicalNotInstruction);
                break;
            case IrModInstruction irModInstruction:
                codeGenerator.generateMod(codeGenerationContext, irModInstruction);
                break;
            case IrMoveInstruction irMoveInstruction:
                codeGenerator.generateMove(codeGenerationContext, irMoveInstruction);
                break;
            case IrMulInstruction irMulInstruction:
                codeGenerator.generateMult(codeGenerationContext, irMulInstruction);
                break;
            case IrNegateInstruction irNegateInstruction:
                codeGenerator.generateNegation(codeGenerationContext, irNegateInstruction);
                break;
            case IrRightShiftInstruction irRightShiftInstruction:
                codeGenerator.generateRightShift(codeGenerationContext, irRightShiftInstruction);
                break;
            case IrSubInstruction irSubInstruction:
                codeGenerator.generateSub(codeGenerationContext, irSubInstruction);
                break;
            case IrUnequalsInstruction irUnequalsInstruction:
                codeGenerator.generateUnequals(codeGenerationContext, irUnequalsInstruction);
                break;
            case IrPhi _:
                throw new IllegalArgumentException("Phi instruction is not supported");
        }
    }
}
