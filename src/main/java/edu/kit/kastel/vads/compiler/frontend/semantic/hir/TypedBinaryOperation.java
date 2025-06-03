package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedBinaryOperation(
        HirType type,
        BinaryOperator operator,
        TypedExpression lhsExpression,
        TypedExpression rhsExpression,
        Span span) implements TypedExpression {

    @Override
    public TypedBinaryOperation asBinaryOperation() {
        return this;
    }
}
