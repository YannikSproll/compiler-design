package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrLessThanOrEqualInstruction(
        SSAValue target,
        SSAValue leftSrc,
        SSAValue rightSrc) implements IrBinaryOperationInstruction {
}
