package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.List;
import java.util.Optional;

public record TypedBlock(
        List<TypedStatement> statements,
        Optional<Scope> declaredScope,
        Span span) implements TypedStatement {
    @Override
    public TypedBlock asBlock() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }
}
