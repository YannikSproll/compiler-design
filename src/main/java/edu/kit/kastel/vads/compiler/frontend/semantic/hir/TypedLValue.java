package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public sealed interface TypedLValue permits TypedVariable {
    HirType type();
    Span span();

    default TypedVariable asVariable() {
        throw new UnsupportedOperationException("Expression is not a variable");
    }

    <TContext> void accept(TypedVisitor<TContext> visitor, TContext context);
    <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context);
}
