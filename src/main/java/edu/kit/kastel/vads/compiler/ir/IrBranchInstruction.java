package edu.kit.kastel.vads.compiler.ir;

public final class IrBranchInstruction implements IrInstruction {
    private SSAValue conditionValue;
    private final IrBlock trueTarget;
    private final IrBlock falseTarget;

    public IrBranchInstruction(SSAValue conditionValue, IrBlock trueTarget, IrBlock falseTarget) {
        this.conditionValue = conditionValue;
        this.trueTarget = trueTarget;
        this.falseTarget = falseTarget;
    }

    public SSAValue conditionValue() { return conditionValue; }
    public IrBlock trueTarget() { return trueTarget; }
    public IrBlock falseTarget() { return falseTarget; }

    public void replaceConditionValue(SSAValue newConditionValue) {
        this.conditionValue = newConditionValue;
    }
}
