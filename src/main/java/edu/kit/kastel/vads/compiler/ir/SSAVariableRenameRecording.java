package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.IrValueProducingInstruction;

import java.util.*;

public final class SSAVariableRenameRecording {
    private final Map<Symbol, List<SSAValue>> ssaValueMappings;
    private final Map<SSAValue, Symbol> invertedMappings;
    private final Map<IrBlock, List<SSAValue>> ssaValuesByDefiningBlocks;

    public SSAVariableRenameRecording() {
        this.ssaValueMappings = new HashMap<>();
        this.invertedMappings = new HashMap<>();
        this.ssaValuesByDefiningBlocks = new HashMap<>();
    }

    private SSAVariableRenameRecording(Map<Symbol, List<SSAValue>> ssaValueMappings, Map<IrBlock, List<SSAValue>> ssaValuesByDefiningBlocks) {
        this.ssaValueMappings = ssaValueMappings;

        Map<SSAValue, Symbol> invertedMappings = new HashMap<>();
        for (Map.Entry<Symbol, List<SSAValue>> mapping : ssaValueMappings.entrySet()) {
            for (SSAValue value : mapping.getValue()) {
                invertedMappings.put(value, mapping.getKey());
            }
        }

        this.invertedMappings = invertedMappings;
        this.ssaValuesByDefiningBlocks = ssaValuesByDefiningBlocks;
    }

    public void introduceNewSSAValue(SSAValue ssaValue, IrBlock definingBlock) {
        if (ssaValue.symbol().isPresent()) {
            Symbol symbol = ssaValue.symbol().get();
            ssaValueMappings.computeIfAbsent(symbol, _ -> new ArrayList<>()).add(ssaValue);
            invertedMappings.computeIfAbsent(ssaValue, _ -> symbol);
        }
        ssaValuesByDefiningBlocks.computeIfAbsent(definingBlock, _ -> new ArrayList<>()).add(ssaValue);
    }

    public Optional<SSAValue> getLatestSSAValue(Symbol symbol, IrBlock block) {
        List<SSAValue> definedInBlock = ssaValuesByDefiningBlocks.getOrDefault(block, List.of());
        if (definedInBlock.stream().anyMatch(x -> x.symbol().isPresent() && x.symbol().get().equals(symbol))) {
            for (IrInstruction instruction : block.getInstructions().reversed()) {
                if (instruction instanceof IrValueProducingInstruction valueProducingInstruction
                    && valueProducingInstruction.target().symbol().isPresent()
                    && valueProducingInstruction.target().symbol().get().equals(symbol)) {
                    return Optional.of(valueProducingInstruction.target());
                }
            }
        }
        return Optional.empty();
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
        invertedMappings.clear();
        ssaValuesByDefiningBlocks.clear();
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

        Map<IrBlock, List<SSAValue>> ssaValuesByDefiningBlocks = new HashMap<>();
        for (Map.Entry<IrBlock, List<SSAValue>> entry : this.ssaValuesByDefiningBlocks.entrySet()) {
            List<SSAValue> newValues = new ArrayList<>(entry.getValue());
            ssaValuesByDefiningBlocks.put(entry.getKey(), newValues);
        }

        return new SSAVariableRenameRecording(newMappings, ssaValuesByDefiningBlocks);
    }
}
