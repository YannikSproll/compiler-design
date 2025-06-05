package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

public record TypedFunction(Symbol symbol, TypedBlock body, Scope declaringScope) implements TypedNode {

    @Override
    public TypedFunction asTypedFunction() {
        return this;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }

    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context) {
        return visitor.visit(this, context);
    }
}
