package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.SSAValue;

public sealed interface IrValueProducingInstruction extends IrInstruction
        permits IrPhi, IrBoolConstantInstruction, IrIntConstantInstruction, IrMoveInstruction, IrBinaryOperationInstruction, IrUnaryOperationInstruction
    {
    SSAValue target();
}
