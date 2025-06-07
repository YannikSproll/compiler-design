package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrMoveInstruction(
        SSAValue target,
        SSAValue source) implements IrValueProducingInstruction {
}
