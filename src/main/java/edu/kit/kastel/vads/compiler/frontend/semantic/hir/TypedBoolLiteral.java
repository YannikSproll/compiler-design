package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedBoolLiteral(boolean value, HirType type, Span span) implements TypedExpression {

    @Override
    public TypedBoolLiteral asBoolLiteral() {
        return this;
    }
}
