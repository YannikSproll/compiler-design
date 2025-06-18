package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.List;

public record TypedFunctionCall(
        TypedFunction calledFunction,
        List<TypedArgument> arguments,
        HirType type,
        Span span) implements TypedStatement, TypedExpression {

    @Override
    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext tContext) {
        visitor.visit(this, tContext);
    }

    @Override
    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext tContext) {
        return visitor.visit(this, tContext);
    }
}
