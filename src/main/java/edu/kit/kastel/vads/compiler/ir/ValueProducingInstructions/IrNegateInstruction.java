package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrNegateInstruction(
        SSAValue target,
        SSAValue src) implements IrUnaryOperationInstruction {
}
