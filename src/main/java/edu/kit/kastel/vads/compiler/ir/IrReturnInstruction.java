package edu.kit.kastel.vads.compiler.ir;

public final class IrReturnInstruction implements IrInstruction {
    private SSAValue src;

    public IrReturnInstruction(SSAValue src) {
        this.src = src;
    }

    public SSAValue src() { return src; }
    public void replaceSrc(SSAValue newSrc) {
        this.src = newSrc;
    }
}
