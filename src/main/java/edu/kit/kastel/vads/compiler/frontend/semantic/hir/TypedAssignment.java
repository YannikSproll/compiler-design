package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public record TypedAssignment(
        TypedLValue lValue,
        TypedExpression initializer,
        Span span) implements TypedStatement {

    @Override
    public TypedAssignment asAssignment() {
        return this;
    }
}
