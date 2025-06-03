package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedIntLiteral(int value, HirType type, Span span) implements TypedExpression {

    @Override
    public TypedIntLiteral asIntLiteral() {
        return this;
    }
}
