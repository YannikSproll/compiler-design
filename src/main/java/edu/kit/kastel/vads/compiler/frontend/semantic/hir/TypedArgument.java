package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedArgument(TypedExpression expression, Span span) implements TypedExpression {

    @Override
    public HirType type() {
        return expression.type();
    }

    @Override
    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext tContext) {
        visitor.visit(this, tContext);
    }

    @Override
    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext tContext) {
        return visitor.visit(this, tContext);
    }
}
