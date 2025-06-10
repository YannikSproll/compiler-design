package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public sealed interface IrValueProducingInstruction extends IrInstruction
        permits IrPhi, IrBoolConstantInstruction, IrIntConstantInstruction, IrMoveInstruction, IrBinaryOperationInstruction, IrUnaryOperationInstruction
    {
    SSAValue target();
}
