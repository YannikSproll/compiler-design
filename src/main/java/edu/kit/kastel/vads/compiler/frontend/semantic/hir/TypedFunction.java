package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

public record TypedFunction(Symbol symbol, TypedBlock body) implements TypedNode {

    @Override
    public TypedFunction asTypedFunction() {
        return this;
    }
}
