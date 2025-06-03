package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.frontend.parser.symbol.Name;

import java.util.HashMap;
import java.util.Optional;

public class Scope {
    private final HashMap<String, Symbol> typesOfVariables;
    private final Optional<Scope> parent;

    public Scope() {
        this.typesOfVariables = new HashMap<>();
        this.parent = Optional.empty();
    }

    public Scope(Scope parent) {
        this.typesOfVariables = new HashMap<>();
        this.parent = Optional.of(parent);
    }
    public void putType(String name, Symbol symbol) {typesOfVariables.put(name, symbol);}


    public Symbol typeOf(String name) {
        boolean containsType = typesOfVariables.containsKey(name);
        if (!containsType) {
            if (parent.isEmpty()) {
                throw new IllegalStateException("Type not found during type checking analysis of " + name);
            }
            return parent.get().typeOf(name);
        }
        return typesOfVariables.get(name);
    }
}
