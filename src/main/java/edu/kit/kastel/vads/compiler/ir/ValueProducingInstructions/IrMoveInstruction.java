package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrMoveInstruction(
        SSAValue target,
        SSAValue source) implements IrValueProducingInstruction {
}
