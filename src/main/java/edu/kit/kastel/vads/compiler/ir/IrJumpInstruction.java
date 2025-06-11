package edu.kit.kastel.vads.compiler.ir;

public record IrJumpInstruction(IrBlock jumpTarget) implements IrInstruction {
}
