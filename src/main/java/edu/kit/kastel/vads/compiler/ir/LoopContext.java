package edu.kit.kastel.vads.compiler.ir;

public final class LoopContext {
    private final IrBlock reevaluateConditionBlock;
    private final IrBlock exitLoopBlock;
    private boolean hasBreak;
    private boolean hasContinue;

    public LoopContext(
            IrBlock reevaluateConditionBlock,
            IrBlock exitLoopBlock) {
        this.reevaluateConditionBlock = reevaluateConditionBlock;
        this.exitLoopBlock = exitLoopBlock;
        this.hasBreak = false;
        this.hasContinue = false;
    }

    public IrBlock reevaluateConditionBlock() {
        return reevaluateConditionBlock;
    }

    public IrBlock exitLoopBlock() {
        return exitLoopBlock;
    }

    public boolean hasBreak() {
        return hasBreak;
    }

    public boolean hasContinue() {
        return hasContinue;
    }

    public void requireBreak() {
        hasBreak = true;
    }

    public void requireContinue() {
        hasContinue = true;
    }
}
