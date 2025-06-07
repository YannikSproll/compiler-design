package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public record IrIntConstantInstruction(SSAValue target, int constValue) implements IrValueProducingInstruction {
}
