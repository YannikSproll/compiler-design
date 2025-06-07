package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrBitwiseOrInstruction(
        SSAValue target,
        SSAValue leftSrc,
        SSAValue rightSrc) implements IrValueProducingInstruction {
}
