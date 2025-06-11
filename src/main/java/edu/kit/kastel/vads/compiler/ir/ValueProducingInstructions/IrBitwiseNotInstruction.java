package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrBitwiseNotInstruction(
        SSAValue target,
        SSAValue src) implements IrUnaryOperationInstruction {
}
