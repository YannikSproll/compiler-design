package edu.kit.kastel.vads.compiler.ir.data;

public record LoopContext(
        IrBlock reevaluateConditionBlock,
        IrBlock exitLoopBlock) {
}
