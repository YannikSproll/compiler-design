package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.IrValueProducingInstruction;

public sealed interface IrInstruction permits IrValueProducingInstruction, IrJumpInstruction, IrBranchInstruction, IrReturnInstruction {
}
