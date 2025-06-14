package edu.kit.kastel.vads.compiler.ir;

public final class SSAValueGenerator {
    private int ssaValueNameCounter = 0;

    public void reset() {
        ssaValueNameCounter = 0;
    }

    public SSAValue generateNewSSAValue() {
        return new SSAValue("%" + ssaValueNameCounter++);
    }
}
