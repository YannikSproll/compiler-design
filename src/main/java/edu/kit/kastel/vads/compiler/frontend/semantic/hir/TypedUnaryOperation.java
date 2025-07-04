package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedUnaryOperation(UnaryOperator operator, TypedExpression expression, Span span) implements TypedExpression {
    @Override
    public HirType type() {
        return expression.type();
    }

    @Override
    public TypedUnaryOperation asUnaryOperation() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }

    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context) {
        return visitor.visit(this, context);
    }
}
