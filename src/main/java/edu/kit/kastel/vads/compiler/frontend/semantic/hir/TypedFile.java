package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import java.util.List;

public record TypedFile(List<TypedFunction> functions, Scope scope) implements TypedNode {
    @Override
    public TypedFile asTypedFile() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }
}
