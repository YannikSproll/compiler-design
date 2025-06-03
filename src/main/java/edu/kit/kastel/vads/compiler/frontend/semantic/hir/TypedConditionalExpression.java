package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedConditionalExpression(
        HirType type,
        TypedExpression conditionExpression,
        TypedExpression thenExpression,
        TypedExpression elseExpression,
        Span span) implements TypedExpression {

    @Override
    public TypedConditionalExpression asConditionalExpression() {
        return this;
    }
}
