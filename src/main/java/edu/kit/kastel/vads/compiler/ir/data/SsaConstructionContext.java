package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.*;

public class SsaConstructionContext {
    private IrBlock currentBlock;
    private List<IrBlock> blocks;
    private SSAVariableRenameRecording globalVariableNameRecording;
    private Map<IrBlock, SSAVariableRenameRecording> recordingsByBlocks;
    private Stack<LoopContext> loopContexts = new Stack<>();
    private int ssaValueNameCounter = 0;

    public SsaConstructionContext() {
        this.currentBlock = null;
        this.blocks = new ArrayList<>();
        this.globalVariableNameRecording = new SSAVariableRenameRecording();
        this.loopContexts = new Stack<>();
        this.recordingsByBlocks = new HashMap<>();
    }

    public IrBlock beginFunction() {
        IrBlock startBlock = newCurrentBlock();
        ssaValueNameCounter = 0;
        globalVariableNameRecording.clear();
        recordingsByBlocks.clear();
        return startBlock;
    }

    public IrBlock currentBlock() {
        return currentBlock;
    }

    public IrBlock newCurrentBlock() {
        currentBlock = new IrBlock();
        blocks.add(currentBlock);
        return currentBlock;
    }

    public void newCurrentBlock(IrBlock block) {
        blocks.add(block);
        currentBlock = block;
    }

    public List<IrBlock> blocks() {
        return blocks;
    }

    public SSAValue generateNewSSAValue() {
        return new SSAValue("%" + ssaValueNameCounter++);
    }

    public void introduceNewSSAValue(Symbol symbol, SSAValue ssaValue) {
        globalVariableNameRecording.introduceNewSSAValue(symbol, ssaValue);

        recordingsByBlocks.computeIfAbsent(currentBlock, k -> new SSAVariableRenameRecording())
                .introduceNewSSAValue(symbol, ssaValue);
    }

    public SSAValue getLatestSSAValue(Symbol symbol) {
        return globalVariableNameRecording.getLatestSSAValue(symbol);
    }

    public Map<Symbol, SSAValue> getLatestSSAValues(IrBlock block) {
        if (!recordingsByBlocks.containsKey(block)) {
            return Collections.emptyMap();
        }

        return recordingsByBlocks.get(block).getLatestSSAValues();
    }

    public void enterLoop(LoopContext loopContext) {
        loopContexts.push(loopContext);
    }

    public LoopContext getLoopContext() {
        return loopContexts.peek();
    }

    public void exitLoop(LoopContext loopContext) {
        loopContexts.pop();
    }
}
