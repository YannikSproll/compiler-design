package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public sealed interface TypedStatement permits TypedAssignment, TypedBlock, TypedBreak, TypedContinue, TypedDeclaration, TypedIf, TypedLoop, TypedReturn {
    Span span();

    default TypedAssignment asAssignment() {
        throw new UnsupportedOperationException("Statement is not an assignment");
    }

    default TypedIf AsIf() {
        throw new UnsupportedOperationException("Statement is not an if");
    }

    default TypedReturn asReturn() {
        throw new UnsupportedOperationException("Statement is not a return statement");
    }

    default TypedLoop asLoop() {
        throw new UnsupportedOperationException("Statement is not a loop");
    }

    default TypedBlock asBlock() {
        throw new UnsupportedOperationException("Statement is not a block");
    }

    default TypedDeclaration asDeclaration() { throw new UnsupportedOperationException("Statement is not a declaration"); }

    default TypedBreak asBreak() { throw new UnsupportedOperationException("Statement is not a break"); }

    <TContext> void accept(TypedVisitor<TContext> visitor, TContext context);
}
