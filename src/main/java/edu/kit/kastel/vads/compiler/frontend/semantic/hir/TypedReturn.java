package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedReturn(TypedExpression returnExpression, Span span) implements TypedStatement {
    @Override
    public TypedReturn asReturn() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }
}
