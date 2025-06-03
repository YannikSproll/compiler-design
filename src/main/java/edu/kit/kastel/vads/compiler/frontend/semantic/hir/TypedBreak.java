package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedBreak(Span span ) implements TypedStatement{

    @Override
    public TypedBreak asBreak() {
        return this;
    }
}
