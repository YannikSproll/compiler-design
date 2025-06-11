package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.IrValueProducingInstruction;

public sealed interface IrInstruction permits IrValueProducingInstruction, IrJumpInstruction, IrBranchInstruction, IrReturnInstruction {
}
