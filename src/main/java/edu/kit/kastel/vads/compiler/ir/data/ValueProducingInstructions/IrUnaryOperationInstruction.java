package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public sealed interface IrUnaryOperationInstruction extends IrValueProducingInstruction permits
        IrBitwiseNotInstruction, IrLogicalNotInstruction, IrNegateInstruction {
    SSAValue src();
}
