package edu.kit.kastel.vads.compiler.ir;

public record LoopContext(
        IrBlock reevaluateConditionBlock,
        IrBlock exitLoopBlock) {
}
