package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedVariable(Symbol symbol, Span span) implements TypedExpression, TypedLValue {

    @Override
    public HirType type() {
        return symbol.type();
    }

    @Override
    public TypedVariable asVariable() {
        return this;
    }
}
