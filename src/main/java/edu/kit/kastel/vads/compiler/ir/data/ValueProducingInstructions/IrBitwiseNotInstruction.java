package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrBitwiseNotInstruction(
        SSAValue target,
        SSAValue src) implements IrUnaryOperationInstruction {
}
