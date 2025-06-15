package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.Optional;

public final class SSAValueGenerator {
    private int ssaValueNameCounter = 0;

    public void reset() {
        ssaValueNameCounter = 0;
    }

    public SSAValue generateNewSSAValue(Optional<Symbol> symbol) {
        return new SSAValue("%" + ssaValueNameCounter++, symbol);
    }
}
