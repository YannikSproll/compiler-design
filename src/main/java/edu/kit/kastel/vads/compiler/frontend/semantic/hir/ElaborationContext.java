package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

public final class ElaborationContext {
    private final SymbolTable symbolTable;
    private int nestedLoopDepth;

    public ElaborationContext(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.nestedLoopDepth = 0;
    }

    public SymbolTable symbolTable() {
        return symbolTable;
    }

    public void incrementNestedLoopDepth() {
        nestedLoopDepth++;
    }

    public boolean isCurrentlyInLoop() {
        return nestedLoopDepth > 0;
    }

    public void decrementNestedLoopDepth() {
        nestedLoopDepth--;
    }
}
