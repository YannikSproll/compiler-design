package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;

import java.util.*;

public class SsaConstructionContext {
    private IrBlock currentBlock;
    private final List<IrBlock> blocks;
    private final SSAVariableRenameRecording globalVariableNameRecording;
    private Stack<LoopContext> loopContexts = new Stack<>();
    private String functionName;
    private int blockCounter = 0;
    private final SSAValueGenerator ssaValueGenerator;


    public SsaConstructionContext() {
        this.currentBlock = null;
        this.blocks = new ArrayList<>();
        this.globalVariableNameRecording = new SSAVariableRenameRecording();
        this.loopContexts = new Stack<>();
        this.ssaValueGenerator = new SSAValueGenerator();
        this.functionName = null;
    }

    public IrBlock beginFunction(String name) {
        this.functionName = "func_" + name;
        blockCounter = 0;
        IrBlock startBlock = createBlock("start");
        newCurrentBlock(startBlock);
        ssaValueGenerator.reset();
        globalVariableNameRecording.clear();
        return startBlock;
    }

    public IrBlock currentBlock() {
        return currentBlock;
    }

    public IrBlock createBlock(String role) {
        return new IrBlock(getBlockName(role));
    }

    private String getBlockName(String role) {
        return functionName + "_" + role + "_" + blockCounter++;
    }

    public void newCurrentBlock(IrBlock block) {
        blocks.add(block);
        currentBlock = block;
    }

    public List<IrBlock> blocks() {
        return blocks;
    }

    public SSAValue generateNewSSAValue(IrBlock definingBlock) {
        SSAValue newValue = ssaValueGenerator.generateNewSSAValue(Optional.empty());
        globalVariableNameRecording.introduceNewSSAValue(newValue, definingBlock);
        return newValue;
    }

    public SSAValue generateNewSSAValue(Symbol symbol, IrBlock definingBlock) {
        SSAValue newValue =  ssaValueGenerator.generateNewSSAValue(Optional.of(symbol));
        globalVariableNameRecording.introduceNewSSAValue(newValue, definingBlock);
        return newValue;
    }

    public SSAValueGenerator ssaValueGenerator() {
        return ssaValueGenerator;
    }

    public SSAValue getLatestSSAValue(Symbol symbol, IrBlock block) {
        HashSet<IrBlock> visitedSsaValues = new HashSet<>();
        Optional<SSAValue> ssaValue = getLatestSSAValue(symbol, block, visitedSsaValues);
        if (ssaValue.isEmpty()) {
            throw new IllegalStateException("No SSAValue found for symbol " + symbol);
        }

        return ssaValue.get();
    }

    private Optional<SSAValue> getLatestSSAValue(Symbol symbol, IrBlock currentBlock, HashSet<IrBlock> visitedBlocks) {
        if (!visitedBlocks.add(currentBlock)) {
            return Optional.empty();
        }

        Optional<SSAValue> foundSSAValue = globalVariableNameRecording.getLatestSSAValue(symbol, currentBlock);
        if (foundSSAValue.isPresent()) {
            return foundSSAValue;
        }

        for (IrBlock predecessor : currentBlock.getPredecessorBlocks()) {
            foundSSAValue = getLatestSSAValue(symbol, predecessor, visitedBlocks);
            if (foundSSAValue.isPresent()) {
                break;
            }
        }

        return foundSSAValue;
    }


    public SSAVariableRenameRecording getSSAVariables() {

        return globalVariableNameRecording.copy();
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
