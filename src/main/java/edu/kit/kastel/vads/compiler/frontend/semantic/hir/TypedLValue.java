package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public sealed interface TypedLValue permits TypedVariable {
    HirType type();
    Span span();

    default TypedVariable asVariable() {
        throw new UnsupportedOperationException("Expression is not a variable");
    }
}
