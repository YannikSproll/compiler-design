package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.*;

public final class SSAVariableRenameRecording {
    private final Map<Symbol, List<SSAValue>> ssaValueMappings;

    public SSAVariableRenameRecording() {
        this.ssaValueMappings = new HashMap<>();
    }

    private SSAVariableRenameRecording(Map<Symbol, List<SSAValue>> ssaValueMappings) {
        this.ssaValueMappings = ssaValueMappings;
    }

    public void introduceNewSSAValue(Symbol symbol, SSAValue ssaValue) {
        ssaValueMappings.computeIfAbsent(symbol, s -> new ArrayList<>()).add(ssaValue);
    }

    public SSAValue getLatestSSAValue(Symbol symbol) {
        return ssaValueMappings.get(symbol).getLast();
    }

    public boolean containsSymbol(Symbol symbol) {
        return ssaValueMappings.containsKey(symbol);
    }

    public Map<Symbol, SSAValue> getLatestSSAValues() {
        Map<Symbol, SSAValue> result = new HashMap<>();
        for (Map.Entry<Symbol, List<SSAValue>> entry : ssaValueMappings.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getLast());
        }
        return result;
    }

    public void clear() {
        ssaValueMappings.clear();
    }

    public Map<Symbol, List<SSAValue>> getSSAValueMappings() {
        return Collections.unmodifiableMap(ssaValueMappings);
    }

    public SSAVariableRenameRecording copy() {
        Map<Symbol, List<SSAValue>> newMappings = new HashMap<>();

        for (Map.Entry<Symbol, List<SSAValue>> entry : ssaValueMappings.entrySet()) {
            List<SSAValue> newValues = new ArrayList<>(entry.getValue());
            newMappings.put(entry.getKey(), newValues);
        }

        return new SSAVariableRenameRecording(newMappings);
    }
}
