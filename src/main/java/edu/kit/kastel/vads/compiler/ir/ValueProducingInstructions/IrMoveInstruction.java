package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.SSAValue;

public final class IrMoveInstruction implements IrValueProducingInstruction {
    private final SSAValue target;
    private SSAValue source;

    public IrMoveInstruction(
            SSAValue target,
            SSAValue source) {
        this.target = target;
        this.source = source;
    }

    @Override
    public SSAValue target() {
        return target;
    }

    public SSAValue source() {
        return source;
    }

    public void replaceSource(SSAValue newSource) {
        this.source = newSource;
    }

}
