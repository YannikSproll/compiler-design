package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public record TypedDeclaration(
        Symbol symbol,
        HirType type,
        Optional<TypedExpression> initializer,
        Span span) implements TypedStatement {

    @Override
    public TypedDeclaration asDeclaration() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }
}
