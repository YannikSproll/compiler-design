package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public final class Symbol {

    private final String name;
    private final HirType type;
    private final Span declaredAt;
    private Optional<TypedStatement> definingStatement;

    public Symbol(String name, HirType type, Span declaredAt) {
        this.name = name;
        this.type = type;
        this.declaredAt = declaredAt;
        this.definingStatement = Optional.empty();
    }

    public String name() {
        return name;
    }

    public HirType type() {
        return type;
    }

    public Span declaredAt() {
        return declaredAt;
    }

    public Optional<TypedStatement> definedAt() {
        return definingStatement;
    }

    public boolean isDefined() {
        return definingStatement.isPresent();
    }

    public void markAsDefined(TypedStatement definingStatement) {
        if (this.definingStatement.isPresent()) {
            throw new IllegalStateException("Can not mark variable " + name + " as assigned, because it already is assigned");
        }
        this.definingStatement = Optional.of(definingStatement);
    }
}
