package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.*;

public final class SSAVariableRenameRecording {
    private final Map<Symbol, List<SSAValue>> ssaValueMappings;
    private final Map<SSAValue, Symbol> invertedMappings;

    public SSAVariableRenameRecording() {
        this.ssaValueMappings = new HashMap<>();
        this.invertedMappings = new HashMap<>();
    }

    private SSAVariableRenameRecording(Map<Symbol, List<SSAValue>> ssaValueMappings) {
        this.ssaValueMappings = ssaValueMappings;

        Map<SSAValue, Symbol> invertedMappings = new HashMap<>();
        for (Map.Entry<Symbol, List<SSAValue>> mapping : ssaValueMappings.entrySet()) {
            for (SSAValue value : mapping.getValue()) {
                invertedMappings.put(value, mapping.getKey());
            }
        }

        this.invertedMappings = invertedMappings;
    }

    public void introduceNewSSAValue(Symbol symbol, SSAValue ssaValue) {
        ssaValueMappings.computeIfAbsent(symbol, _ -> new ArrayList<>()).add(ssaValue);
        invertedMappings.computeIfAbsent(ssaValue, _ -> symbol);
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

    public Map<SSAValue, Symbol> getInvertedSSAValueMappings() { return Collections.unmodifiableMap(invertedMappings); }

    public SSAVariableRenameRecording copy() {
        Map<Symbol, List<SSAValue>> newMappings = new HashMap<>();

        for (Map.Entry<Symbol, List<SSAValue>> entry : ssaValueMappings.entrySet()) {
            List<SSAValue> newValues = new ArrayList<>(entry.getValue());
            newMappings.put(entry.getKey(), newValues);
        }

        return new SSAVariableRenameRecording(newMappings);
    }
}
