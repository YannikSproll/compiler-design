package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedBoolLiteral(boolean value, HirType type, Span span) implements TypedExpression {

    @Override
    public TypedBoolLiteral asBoolLiteral() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }

    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context) {
        return visitor.visit(this, context);
    }
}
