package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.*;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record CodeGenerationContext(RegisterAllocationResult registerAllocationResult,
                                          Map<SSAValue, IrValueProducingInstruction> ssaValueByProducingInstructions,
                                          Map<SSAValue, Set<IrInstruction>> ssaValueByUsingInstructions) {

    public static CodeGenerationContext createForFunction(IrFunction function, RegisterAllocationResult registerAllocationResult) {
        Map<SSAValue, IrValueProducingInstruction> ssaValueByProducingInstructions = new HashMap<>();
        Map<SSAValue, Set<IrInstruction>> ssaValueByUsingInstructions = new HashMap<>();
        for (IrBlock block : function.blocks()) {
            for (IrInstruction instruction : block.getInstructions()) {
                if (instruction instanceof IrValueProducingInstruction valueProducingInstruction) {
                    ssaValueByProducingInstructions.put(valueProducingInstruction.target(), valueProducingInstruction);
                    ssaValueByUsingInstructions.put(valueProducingInstruction.target(), new HashSet<>());
                }
            }
        }

        for (IrBlock block : function.blocks()) {
            for (IrInstruction instruction : block.getInstructions()) {
                switch (instruction) {
                    case IrReturnInstruction returnInstruction:
                        ssaValueByUsingInstructions.get(returnInstruction.src()).add(instruction);
                        break;
                    case IrBranchInstruction branchInstruction:
                        ssaValueByUsingInstructions.get(branchInstruction.conditionValue()).add(instruction);
                        break;
                    case IrBinaryOperationInstruction binaryOperationInstruction:
                        ssaValueByUsingInstructions.get(binaryOperationInstruction.leftSrc()).add(instruction);
                        ssaValueByUsingInstructions.get(binaryOperationInstruction.rightSrc()).add(instruction);
                        break;
                    case IrUnaryOperationInstruction unaryOperationInstruction:
                        ssaValueByUsingInstructions.get(unaryOperationInstruction.src()).add(instruction);
                        break;
                    case IrJumpInstruction _, IrBoolConstantInstruction _, IrIntConstantInstruction _:
                        continue;
                    case IrMoveInstruction irMoveInstruction:
                        ssaValueByUsingInstructions.get(irMoveInstruction.source()).add(instruction);
                        break;
                    case IrPhi irPhi:
                        throw new IllegalArgumentException("Phi instruction is not supported");
                }
            }
        }

        return new CodeGenerationContext(registerAllocationResult, ssaValueByProducingInstructions, ssaValueByUsingInstructions);
    }
}
