package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

public sealed interface TypedNode extends TypedObject permits TypedFile, TypedFunction {

    default TypedFile asTypedFile() {
        throw new UnsupportedOperationException("Node is not a file");
    }

    default TypedFunction asTypedFunction() {
        throw new UnsupportedOperationException("Node is not a function");
    }

    <TContext> void accept(TypedVisitor<TContext> visitor, TContext context);
}
