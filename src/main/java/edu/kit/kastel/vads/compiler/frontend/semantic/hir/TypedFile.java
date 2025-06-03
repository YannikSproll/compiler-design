package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import java.util.List;

public record TypedFile(List<TypedFunction> functions) implements TypedNode {
    @Override
    public TypedFile asTypedFile() {
        return this;
    }
}
