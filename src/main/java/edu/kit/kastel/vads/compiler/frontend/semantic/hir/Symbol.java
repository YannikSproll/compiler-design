package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

import java.util.Optional;

public final class Symbol {

    private final String name;
    private final HirType type;
    private final Span declaredAt;
    private Optional<Span> firstAssignedAt;

    public Symbol(String name, HirType type, Span declaredAt, Optional<Span> firstAssignedAt) {
        this.name = name;
        this.type = type;
        this.declaredAt = declaredAt;
        this.firstAssignedAt = firstAssignedAt;
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

    public Optional<Span> firstAssignedAt() {
        return firstAssignedAt;
    }

    public boolean isAssigned() {
        return firstAssignedAt.isPresent();
    }

    public void markAsAssigned(Span assignedAt) {
        if (firstAssignedAt.isPresent()) {
            throw new IllegalStateException("Can not mark variable " + name + " as assigned, because it already is assigned");
        }
        firstAssignedAt = Optional.of(assignedAt);
    }
}
