package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedLoop(
        TypedBlock body,
        Span span) implements TypedStatement {
    @Override
    public TypedLoop asLoop() {
        return this;
    }
}
