package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrBoolConstantInstruction(
        SSAValue target,
        boolean constValue) implements IrValueProducingInstruction {
}
