package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public sealed interface IrValueProducingInstruction extends IrInstruction
        permits IrPhi, IrAddInstruction, IrBitwiseAndInstruction, IrBitwiseNotInstruction, IrBitwiseOrInstruction, IrBitwiseXorInstruction, IrBoolConstantInstruction, IrDivInstruction, IrEqualsInstruction, IrGreaterThanInstruction, IrGreaterThanOrEqualInstruction, IrIntConstantInstruction, IrLeftShiftInstruction, IrLessThanInstruction, IrLessThanOrEqualInstruction, IrLogicalNotInstruction, IrModInstruction, IrMoveInstruction, IrMulInstruction, IrNegateInstruction, IrRightShiftInstruction, IrSubInstruction, IrUnequalsInstruction
    {
    SSAValue target();
}
