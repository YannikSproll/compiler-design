package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public record IrIntConstantInstruction(SSAValue target, int constValue) implements IrValueProducingInstruction {
}
