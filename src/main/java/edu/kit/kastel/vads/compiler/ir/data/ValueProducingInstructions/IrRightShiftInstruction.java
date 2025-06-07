package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrRightShiftInstruction(
        SSAValue target,
        SSAValue leftSrc,
        SSAValue rightSrc) implements IrValueProducingInstruction {
}
