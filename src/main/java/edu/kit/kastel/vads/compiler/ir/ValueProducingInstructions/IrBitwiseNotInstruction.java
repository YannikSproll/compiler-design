package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public final class IrBitwiseNotInstruction implements IrUnaryOperationInstruction {
    private final SSAValue target;
    private SSAValue src;

    public IrBitwiseNotInstruction(
            SSAValue target,
            SSAValue src) {
        this.target = target;
        this.src = src;
    }

    @Override
    public SSAValue target() {
        return target;
    }

    @Override
    public SSAValue src() {
        return src;
    }

    @Override
    public void replaceSrc(SSAValue newSrc) {
        this.src = newSrc;
    }
}
