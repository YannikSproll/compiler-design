package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.*;

public class SsaConstructionContext {
    private IrBlock currentBlock;
    private List<IrBlock> blocks;
    private Map<Symbol, List<SSAValue>> ssaValueMappings;

    public SsaConstructionContext() {
        this.currentBlock = null;
        this.blocks = new ArrayList<>();
        this.ssaValueMappings = new HashMap<>();
    }

    public IrBlock currentBlock() {
        return currentBlock;
    }

    public IrBlock newCurrentBlock() {
        blocks.add(currentBlock);
        currentBlock = new IrBlock();
        return currentBlock;
    }

    public List<IrBlock> blocks() {
        return blocks;
    }

    private int ssaValueNameCounter = 0;
    public SSAValue generateNewSSAValue() {
        return new SSAValue("%" + ssaValueNameCounter++);
    }

    public void introduceNewSSAValue(Symbol symbol, SSAValue ssaValue) {
        ssaValueMappings.computeIfAbsent(symbol, s -> new ArrayList<>()).add(ssaValue);
    }

    public SSAValue getLatestSSAValue(Symbol symbol) {
        return ssaValueMappings.get(symbol).getLast();
    }

    public List<SSAValue> getSSAValues(Symbol symbol) {
        return Collections.unmodifiableList(ssaValueMappings.get(symbol));
    }
}
