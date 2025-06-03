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
}
