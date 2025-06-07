package edu.kit.kastel.vads.compiler.ir.data;

public record IrJumpInstruction(IrBlock jumpTarget) implements IrInstruction {
}
