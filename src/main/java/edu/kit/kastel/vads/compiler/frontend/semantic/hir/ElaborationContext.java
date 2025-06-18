package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.frontend.semantic.SemanticException;

import java.util.HashMap;
import java.util.Map;

public final class ElaborationContext {
    private final SymbolTable symbolTable;
    private final Map<String, TypedFunction> functions;
    private int nestedLoopDepth;

    public ElaborationContext(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.functions = new HashMap<>();
        this.nestedLoopDepth = 0;
    }

    public SymbolTable symbolTable() {
        return symbolTable;
    }

    public void defineFunction(String functionName, TypedFunction function) {
        if (functions.containsKey(functionName)) {
            throw new SemanticException("Duplicate function name: " + functionName);
        }

        functions.put(functionName, function);
    }

    public TypedFunction getFunction(String functionName) {
        if (!functions.containsKey(functionName)) {
            throw new SemanticException("Function not found: " + functionName);
        }

        return functions.get(functionName);
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
