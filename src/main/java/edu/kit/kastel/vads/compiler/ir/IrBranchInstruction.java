package edu.kit.kastel.vads.compiler.ir;

public record IrBranchInstruction(
        SSAValue conditionValue,
        IrBlock trueTarget,
        IrBlock falseTarget) implements IrInstruction {
}
