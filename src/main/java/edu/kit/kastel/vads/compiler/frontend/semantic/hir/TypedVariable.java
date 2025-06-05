package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedVariable(Symbol symbol, Span span) implements TypedExpression, TypedLValue {

    @Override
    public HirType type() {
        return symbol.type();
    }

    @Override
    public TypedVariable asVariable() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }

    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context) {
        return visitor.visit(this, context);
    }
}
