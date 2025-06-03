package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public record TypedIf(
        TypedExpression conditionExpression,
        TypedBlock thenBlock,
        Optional<TypedBlock> elseBlock,
        Span span) implements TypedStatement {
    @Override
    public TypedIf AsIf() {
        return this;
    }
}
