package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public sealed interface IrUnaryOperationInstruction extends IrValueProducingInstruction permits
        IrBitwiseNotInstruction, IrLogicalNotInstruction, IrNegateInstruction {
    SSAValue src();

    void replaceSrc(SSAValue newSrc);
}
