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

    public SSAValue generateNewSSAValue() {
        return ssaValueGenerator.generateNewSSAValue();
    }

    public SSAValueGenerator ssaValueGenerator() {
        return ssaValueGenerator;
    }

    public void introduceNewSSAValue(Symbol symbol, SSAValue ssaValue) {
        globalVariableNameRecording.introduceNewSSAValue(symbol, ssaValue);
    }

    public SSAValue getLatestSSAValue(Symbol symbol) {
        return globalVariableNameRecording.getLatestSSAValue(symbol);
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
