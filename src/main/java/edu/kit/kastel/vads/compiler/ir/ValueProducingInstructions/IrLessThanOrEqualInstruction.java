package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public final class IrLessThanOrEqualInstruction implements IrBinaryOperationInstruction {
    private final SSAValue target;
    private SSAValue leftSrc;
    private SSAValue rightSrc;

    public IrLessThanOrEqualInstruction(
            SSAValue target,
            SSAValue leftSrc,
            SSAValue rightSrc) {
        this.target = target;
        this.leftSrc = leftSrc;
        this.rightSrc = rightSrc;
    }

    @Override
    public SSAValue target() {
        return target;
    }

    @Override
    public SSAValue leftSrc() {
        return leftSrc;
    }

    @Override
    public SSAValue rightSrc() {
        return rightSrc;
    }

    @Override
    public void replaceLeftSrc(SSAValue newLeftSrc) {
        leftSrc = newLeftSrc;
    }

    @Override
    public void replaceRightSrc(SSAValue newRightSrc) {
        rightSrc = newRightSrc;
    }

}
