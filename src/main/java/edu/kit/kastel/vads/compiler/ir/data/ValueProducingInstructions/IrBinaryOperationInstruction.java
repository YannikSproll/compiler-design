package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

public sealed interface IrBinaryOperationInstruction extends IrValueProducingInstruction permits
    IrAddInstruction, IrBitwiseAndInstruction, IrBitwiseOrInstruction, IrBitwiseXorInstruction, IrDivInstruction, IrEqualsInstruction, IrGreaterThanInstruction, IrGreaterThanOrEqualInstruction, IrLeftShiftInstruction, IrLessThanInstruction, IrLessThanOrEqualInstruction, IrModInstruction, IrMulInstruction, IrRightShiftInstruction, IrSubInstruction, IrUnequalsInstruction
{
    SSAValue leftSrc();
    SSAValue rightSrc();
}
