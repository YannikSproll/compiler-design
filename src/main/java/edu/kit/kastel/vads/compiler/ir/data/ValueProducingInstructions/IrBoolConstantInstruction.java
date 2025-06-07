package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrBoolConstantInstruction(
        SSAValue target,
        boolean constValue) implements IrValueProducingInstruction {
}
