package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import java.util.Stack;

public class SymbolTable {
    private final Stack<Scope> scopes;

    public SymbolTable() {
        this.scopes = new Stack<>();
    }

    public Scope getCurrentScope() {
        return scopes.peek();
    }

    public void putType(String name, Symbol symbol) { getCurrentScope().putType(name, symbol); }

    public boolean isVariableDeclared(String name) {
        return getCurrentScope().isVariableDeclared(name);
    }

    public Symbol typeOf(String name) {
        return getCurrentScope().typeOf(name);
    }

    public void enterScope() {
        if (!scopes.isEmpty()) {
            scopes.push(new Scope(scopes.peek()));
        } else {
            scopes.push(new Scope());
        }
    }

    public void exitScope() {
        scopes.pop();
    }
}
