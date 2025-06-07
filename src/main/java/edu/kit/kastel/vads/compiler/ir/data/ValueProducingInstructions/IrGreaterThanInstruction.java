package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrGreaterThanInstruction(
        SSAValue target,
        SSAValue leftSrc,
        SSAValue rightSrc) implements IrValueProducingInstruction {
}
