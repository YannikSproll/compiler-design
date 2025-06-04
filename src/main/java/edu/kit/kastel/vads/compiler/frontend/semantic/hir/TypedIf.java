package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public record TypedIf(
        TypedExpression conditionExpression,
        TypedStatement thenStatement,
        Optional<TypedStatement> elseStatement,
        Span span) implements TypedStatement {
    @Override
    public TypedIf AsIf() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }
}
